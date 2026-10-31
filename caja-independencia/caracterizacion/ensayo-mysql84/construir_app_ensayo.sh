#!/bin/bash
# Construye una COPIA de la aplicación que apunta a la instancia de ensayo y que no puede enviar
# correos reales. El proyecto original no se modifica: se trabaja sobre una copia en $DESTINO.
#
# Cambios que se aplican SOLO en la copia:
#   - hibernate.cfg.xml: host = IP de la instancia de ensayo, esquema `sindicato`, contraseña de ensayo.
#   - EnviaCorreo.java: servidor SMTP = buzón falso local (Mailpit), en vez de mail.cajaindependencia.com.
# Antes de compilar se verifica que la copia NO mencione la IP de producción ni el servidor de correo real.
set -euo pipefail

PROYECTO="$(cd "$(dirname "$0")/../.." && pwd)"
DESTINO="${DESTINO:-$HOME/.cache/caja-independencia/app-ensayo84}"
CRED_ENSAYO="$HOME/.config/caja-independencia/ensayo.env"
IP_PRODUCCION=35.225.67.182
ENSAYO=evoti-sindicato-ensayo84

IP_ENSAYO=$(gcloud sql instances describe "$ENSAYO" --configuration=caja-lectura --project=sindicatoindependencia --format="value(ipAddresses[0].ipAddress)")
if [ -z "$IP_ENSAYO" ] || [ "$IP_ENSAYO" = "$IP_PRODUCCION" ]; then
  echo "BLOQUEADO: IP de ensayo inválida ('$IP_ENSAYO')."; exit 1
fi
# shellcheck disable=SC1090
source "$CRED_ENSAYO"

echo "Copiando el proyecto a $DESTINO"
mkdir -p "$DESTINO"
rsync -a --delete --exclude target --exclude .git --exclude caracterizacion "$PROYECTO/" "$DESTINO/"

CFG="$DESTINO/src/main/resources/hibernate.cfg.xml"
CFG="$CFG" IP_ENSAYO="$IP_ENSAYO" PW="$ENSAYO_APP_PASSWORD" python3 - <<'PY'
import os, re
p = os.environ["CFG"]
s = open(p, encoding="utf-8").read()
url = ("jdbc:mysql://%s:3306/sindicato?zeroDateTimeBehavior=convertToNull&amp;autoReconnect=true"
       "&amp;allowPublicKeyRetrieval=true&amp;useSSL=false") % os.environ["IP_ENSAYO"]
s, n1 = re.subn(r'(<property name="hibernate\.connection\.url">)\s*[^<]*?\s*(</property>)', lambda m: m.group(1) + url + m.group(2), s)
s, n2 = re.subn(r'(<property name="hibernate\.connection\.password">)[^<]*(</property>)', lambda m: m.group(1) + os.environ["PW"] + m.group(2), s)
assert n1 == 1 and n2 == 1, (n1, n2)
open(p, "w", encoding="utf-8").write(s)
PY

CORREO="$DESTINO/src/main/java/mx/com/evoti/bo/util/EnviaCorreo.java"
# Se reemplazan TODAS las apariciones del servidor real: además de mail.smtp.host, el código llama a
# t.connect("mail.cajaindependencia.com", ...), que ignora la configuración de la sesión.
sed -i '' -e 's|"mail.cajaindependencia.com"|"correo-ensayo"|g' \
          -e 's|"mail.smtp.port", "26"|"mail.smtp.port", "1025"|g' "$CORREO"

echo "Verificaciones de seguridad sobre la copia:"
if grep -rq "$IP_PRODUCCION" "$DESTINO/src/main/resources" "$DESTINO/src/main/java"; then
  echo "  FALLA: la copia todavía menciona la IP de producción."; exit 1
fi
if grep -rq 'mail\.cajaindependencia\.com' "$DESTINO/src/main/java"; then
  echo "  FALLA: la copia todavía apunta al servidor de correo real."; exit 1
fi
grep -q "$IP_ENSAYO" "$CFG" && echo "  ok: la base apunta a la instancia de ensayo ($IP_ENSAYO)"
grep -q '"correo-ensayo"' "$CORREO" && echo "  ok: el correo va al buzón falso local"

echo "Compilando la copia"
(cd "$DESTINO" && mvn -o -q clean package -DskipTests)
if unzip -p "$DESTINO/target/Sindicato.war" 'WEB-INF/classes/hibernate.cfg.xml' | grep -q "$IP_PRODUCCION"; then
  echo "  FALLA: el WAR contiene la IP de producción."; exit 1
fi
if unzip -p "$DESTINO/target/Sindicato.war" 'WEB-INF/classes/mx/com/evoti/bo/util/EnviaCorreo.class' | LC_ALL=C grep -a -q 'mail\.cajaindependencia\.com'; then
  echo "  FALLA: la clase de correo compilada todavía contiene el servidor real."; exit 1
fi
echo "  ok: el WAR no contiene la IP de producción ni el servidor de correo real"
echo "Listo: $DESTINO/target/Sindicato.war"

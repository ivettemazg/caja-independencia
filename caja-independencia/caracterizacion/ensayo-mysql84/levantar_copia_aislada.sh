#!/bin/bash
# Levanta una copia de la aplicación SIN SALIDA A INTERNET para probar un WAR antes de producción.
#   - Red docker "caja-aislada" creada con --internal: los contenedores no pueden salir a internet,
#     así que es imposible que la copia llegue a la base de producción.
#   - Base: el contenedor de control local caja-mysql80-control (MySQL 8.0.31), alias mysql-control.
#   - Correo: Mailpit con el alias mail.cajaindependencia.com en el puerto 26, el mismo host y puerto
#     que usa EnviaCorreo, así que los correos llegan al buzón falso y no salen.
#   - El WAR se copia y solo se reemplaza WEB-INF/classes/hibernate.cfg.xml (url, usuario y contraseña).
#   - Un proxy nginx publica la app y el buzón solo en 127.0.0.1:
#       Aplicación: http://127.0.0.1:8090/Sindicato/    Correos: http://127.0.0.1:8026
# Uso: ./levantar_copia_aislada.sh subir <ruta_war> | bajar
set -euo pipefail
AQUI="$(cd "$(dirname "$0")" && pwd)"
DIR="$HOME/.cache/caja-independencia/copia-aislada"
RED=caja-aislada
PROD_IP=35.225.67.182

case "${1:-}" in
  subir)
    WAR_ORIGEN="${2:?Falta la ruta del WAR}"
    mkdir -p "$DIR/cfg/WEB-INF/classes"
    cp "$WAR_ORIGEN" "$DIR/Sindicato.war"
    unzip -p "$DIR/Sindicato.war" WEB-INF/classes/hibernate.cfg.xml > "$DIR/cfg-original.xml"
    PW_CONTROL="$(sed -n 's/^MYSQL_ROOT_PASSWORD=//p' "$HOME/.config/caja-independencia/mysql-control.env")"
    PW_CONTROL="$PW_CONTROL" python3 -I - "$DIR/cfg-original.xml" "$DIR/cfg/WEB-INF/classes/hibernate.cfg.xml" <<'PY'
import os, re, sys
txt = open(sys.argv[1], encoding="utf-8").read()
def pon(nombre, valor, txt):
    patron = re.compile(r'(<property name="%s">)(.*?)(</property>)' % re.escape(nombre), re.S)
    nuevo, n = patron.subn(lambda m: m.group(1) + valor + m.group(3), txt)
    if n != 1: sys.exit("no se encontró exactamente una propiedad " + nombre)
    return nuevo
txt = pon("hibernate.connection.url", "jdbc:mysql://mysql-control:3306/sindicato?zeroDateTimeBehavior=convertToNull&amp;autoReconnect=true&amp;allowPublicKeyRetrieval=true&amp;useSSL=false", txt)
txt = pon("hibernate.connection.username", "root", txt)
txt = pon("hibernate.connection.password", os.environ["PW_CONTROL"], txt)
open(sys.argv[2], "w", encoding="utf-8").write(txt)
PY
    if grep -q "$PROD_IP" "$DIR/cfg/WEB-INF/classes/hibernate.cfg.xml"; then echo "BLOQUEADO: la configuración nueva aún menciona producción"; exit 2; fi
    (cd "$DIR/cfg" && zip -q "$DIR/Sindicato.war" WEB-INF/classes/hibernate.cfg.xml)
    if unzip -p "$DIR/Sindicato.war" WEB-INF/classes/hibernate.cfg.xml | grep -q "$PROD_IP"; then echo "BLOQUEADO: el WAR copiado aún apunta a producción"; exit 2; fi
    rm -f "$DIR/cfg-original.xml"
    # Todo lo demás del WAR debe quedar idéntico al original (CRC de cada archivo).
    diff <(unzip -v "$WAR_ORIGEN" | awk 'NF>=8 && $7 ~ /^[0-9a-f]{8}$/ {print $7, $8}' | grep -v ' WEB-INF/classes/hibernate.cfg.xml$' | sort -k2) \
         <(unzip -v "$DIR/Sindicato.war" | awk 'NF>=8 && $7 ~ /^[0-9a-f]{8}$/ {print $7, $8}' | grep -v ' WEB-INF/classes/hibernate.cfg.xml$' | sort -k2) >/dev/null \
      || { echo "BLOQUEADO: el WAR copiado difiere del original en algo más que la configuración"; exit 2; }
    echo "WAR copiado: solo cambia hibernate.cfg.xml y ya no apunta a producción"

    docker network inspect "$RED" >/dev/null 2>&1 || docker network create --internal "$RED" >/dev/null
    [ "$(docker network inspect -f '{{.Internal}}' "$RED")" = "true" ] || { echo "BLOQUEADO: la red $RED no es interna"; exit 2; }
    docker inspect -f '{{json .NetworkSettings.Networks}}' caja-mysql80-control | grep -q "\"$RED\"" \
      || docker network connect --alias mysql-control "$RED" caja-mysql80-control
    docker rm -f app-aislada correo-aislado proxy-aislado >/dev/null 2>&1 || true

    # Comprobación de aislamiento con un contenedor desechable en la misma red.
    if docker run --rm --network "$RED" tomcat:8.5-jdk11 bash -c "timeout 5 bash -c 'echo > /dev/tcp/$PROD_IP/3306'" >/dev/null 2>&1; then
      echo "BLOQUEADO: desde la red $RED se alcanza producción"; exit 2
    fi
    echo "Aislamiento verificado: desde la red $RED no se alcanza $PROD_IP"

    docker run -d --name correo-aislado --network "$RED" --network-alias mail.cajaindependencia.com \
      axllent/mailpit --smtp 0.0.0.0:26 --smtp-auth-accept-any --smtp-auth-allow-insecure >/dev/null
    SERVER_XML="$HOME/.cache/caja-independencia/server-ensayo84.xml"
    grep -q 'maxParameterCount="10000"' "$SERVER_XML" || { echo "Falta $SERVER_XML (lo genera levantar_app_ensayo.sh)"; exit 1; }
    docker run -d --name app-aislada --network "$RED" -e TZ=America/Mexico_City \
      -v "$DIR/Sindicato.war:/usr/local/tomcat/webapps/Sindicato.war:ro" \
      -v "$SERVER_XML:/usr/local/tomcat/conf/server.xml:ro" \
      tomcat:8.5-jdk11 >/dev/null

    cat > "$DIR/nginx.conf" <<'NGX'
server { listen 8090; client_max_body_size 50m;
  location / { proxy_pass http://app-aislada:8080; proxy_set_header Host $host:$server_port; proxy_read_timeout 600s; } }
server { listen 8026;
  location / { proxy_pass http://correo-aislado:8025; proxy_http_version 1.1; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; } }
NGX
    docker create --name proxy-aislado -p 127.0.0.1:8090:8090 -p 127.0.0.1:8026:8026 \
      -v "$DIR/nginx.conf:/etc/nginx/conf.d/default.conf:ro" nginx:1.27-alpine >/dev/null
    docker network connect "$RED" proxy-aislado
    docker start proxy-aislado >/dev/null
    echo "Aplicación: http://127.0.0.1:8090/Sindicato/   Correos de prueba: http://127.0.0.1:8026"
    ;;
  bajar)
    docker rm -f app-aislada correo-aislado proxy-aislado >/dev/null 2>&1 || true
    docker network disconnect "$RED" caja-mysql80-control >/dev/null 2>&1 || true
    docker network rm "$RED" >/dev/null 2>&1 || true
    echo "Copia aislada detenida y eliminada."
    ;;
  *) echo "Uso: $0 subir <ruta_war> | bajar"; exit 1 ;;
esac

#!/bin/bash
# Copia tablas del esquema `pruebas` (nube) al MySQL local `caja_local`.
# Del lado de la nube solo LEE (mysqldump --single-transaction, sin bloqueos). El esquema de
# origen está fijo en `pruebas`: este script nunca toca `sindicato`.
# Las credenciales de la nube se leen de hibernate.cfg.xml en memoria; no se imprimen ni se guardan.
set -euo pipefail

CFG="$(cd "$(dirname "$0")/.." && pwd)/src/main/resources/hibernate.cfg.xml"
ENV_FILE="$HOME/.config/caja-independencia/mysql-local.env"
NOMBRE=caja-mysql-local
ORIGEN=pruebas
DESTINO=caja_local

if [ "$#" -eq 0 ]; then echo "Uso: $0 tabla1 [tabla2 ...]"; exit 1; fi

eval "$(python3 - "$CFG" <<'PY'
import re, shlex, sys
import xml.etree.ElementTree as ET
p = {e.get('name'): (e.text or '').strip() for e in ET.parse(sys.argv[1]).iter('property')}
m = re.search(r'jdbc:mysql://([^:/?]+)(?::(\d+))?', p['hibernate.connection.url'])
print('NUBE_HOST=' + shlex.quote(m.group(1)))
print('NUBE_PORT=' + shlex.quote(m.group(2) or '3306'))
print('NUBE_USER=' + shlex.quote(p['hibernate.connection.username']))
print('NUBE_PW=' + shlex.quote(p['hibernate.connection.password']))
PY
)"
LOCAL_PW=$(grep '^MYSQL_ROOT_PASSWORD=' "$ENV_FILE" | cut -d= -f2-)

echo "Origen: $NUBE_HOST / $ORIGEN (solo lectura)  ->  destino: local / $DESTINO"
echo "Tablas: $*"
INICIO=$(date +%s)

MYSQL_PWD="$NUBE_PW" docker exec -e MYSQL_PWD "$NOMBRE" mysqldump \
    -h "$NUBE_HOST" -P "$NUBE_PORT" -u "$NUBE_USER" \
    --single-transaction --quick --skip-lock-tables --no-tablespaces --set-gtid-purged=OFF \
    --skip-triggers --default-character-set=utf8mb4 "$ORIGEN" "$@" \
  | MYSQL_PWD="$LOCAL_PW" docker exec -i -e MYSQL_PWD "$NOMBRE" mysql -h127.0.0.1 -uroot "$DESTINO"

echo "Copia terminada en $(( $(date +%s) - INICIO )) s. Filas por tabla en local:"
for t in "$@"; do
  n=$(MYSQL_PWD="$LOCAL_PW" docker exec -e MYSQL_PWD "$NOMBRE" mysql -h127.0.0.1 -uroot -N -e "select count(*) from \`$DESTINO\`.\`$t\`")
  echo "  $t: $n"
done

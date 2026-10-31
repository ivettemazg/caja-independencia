#!/bin/bash
# Base de CONTROL: MySQL 8.0.31 (la versión de producción) en Docker, solo en 127.0.0.1:3308,
# cargada con una copia del esquema `sindicato` de la instancia de ENSAYO (no de producción).
# Sirve para correr los mismos procesos en 8.0 y en 8.4 y comparar resultados.
set -euo pipefail

NOMBRE=caja-mysql80-control
ENV_FILE="$HOME/.config/caja-independencia/mysql-control.env"
CRED_ENSAYO="$HOME/.config/caja-independencia/ensayo.env"
IP_PRODUCCION=35.225.67.182

IP_ENSAYO=$(gcloud sql instances describe evoti-sindicato-ensayo84 --configuration=caja-lectura \
  --project=sindicatoindependencia --format="value(ipAddresses[0].ipAddress)")
if [ -z "$IP_ENSAYO" ] || [ "$IP_ENSAYO" = "$IP_PRODUCCION" ]; then echo "BLOQUEADO: IP de ensayo inválida."; exit 1; fi

if [ ! -f "$ENV_FILE" ]; then
  (umask 077; printf 'MYSQL_ROOT_PASSWORD=%s\n' "$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 24 || true)" > "$ENV_FILE")
fi
chmod 600 "$ENV_FILE"
LOCAL_PW=$(grep '^MYSQL_ROOT_PASSWORD=' "$ENV_FILE" | cut -d= -f2-)
# shellcheck disable=SC1090
source "$CRED_ENSAYO"

docker pull -q mysql:8.0.31 >/dev/null
docker rm -f "$NOMBRE" >/dev/null 2>&1 || true
docker run -d --name "$NOMBRE" -p 127.0.0.1:3308:3306 --env-file "$ENV_FILE" mysql:8.0.31 >/dev/null
for i in $(seq 1 120); do
  docker exec "$NOMBRE" sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqladmin ping -h127.0.0.1 -uroot --silent' >/dev/null 2>&1 && break
  sleep 2
done
echo "Control listo: $(docker exec "$NOMBRE" sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -h127.0.0.1 -uroot -N -e "select version()"')"

echo "Copiando sindicato desde la instancia de ENSAYO ($IP_ENSAYO) al control..."
INICIO=$(date +%s)
MYSQL_PWD="$ENSAYO_APP_PASSWORD" docker exec -e MYSQL_PWD "$NOMBRE" mysqldump -h "$IP_ENSAYO" -u "$ENSAYO_APP_USER" \
    --single-transaction --quick --routines --events --triggers --set-gtid-purged=OFF --no-tablespaces \
    --default-character-set=utf8mb4 --databases sindicato \
  | MYSQL_PWD="$LOCAL_PW" docker exec -i -e MYSQL_PWD "$NOMBRE" mysql -h127.0.0.1 -uroot
echo "Copia terminada en $(( $(date +%s) - INICIO )) s"
docker exec "$NOMBRE" sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -h127.0.0.1 -uroot -N -e "select count(*) from sindicato.usuarios; select count(*) from information_schema.tables where table_schema=\"sindicato\"; select event_name, status from information_schema.events where event_schema=\"sindicato\""'

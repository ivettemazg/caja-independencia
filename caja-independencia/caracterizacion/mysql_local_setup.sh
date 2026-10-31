#!/bin/bash
# Levanta un MySQL 8.0 local en Docker, solo accesible desde esta máquina (127.0.0.1:3307).
# La contraseña de root se genera al azar y queda en ~/.config/caja-independencia/mysql-local.env
set -euo pipefail

CONF_DIR="$HOME/.config/caja-independencia"
ENV_FILE="$CONF_DIR/mysql-local.env"
NOMBRE=caja-mysql-local

mkdir -p "$CONF_DIR"
if [ ! -f "$ENV_FILE" ]; then
  umask 077
  PW=$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 24 || true)
  printf 'MYSQL_ROOT_PASSWORD=%s\nMYSQL_DATABASE=caja_local\n' "$PW" > "$ENV_FILE"
  echo "Archivo de credenciales locales creado"
fi
chmod 600 "$ENV_FILE"

docker pull -q mysql:8.0

if docker ps -a --format '{{.Names}}' | grep -qx "$NOMBRE"; then
  docker start "$NOMBRE" >/dev/null
else
  docker run -d --name "$NOMBRE" -p 127.0.0.1:3307:3306 -v caja_mysql_local:/var/lib/mysql \
    --env-file "$ENV_FILE" mysql:8.0 \
    --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci >/dev/null
fi

# Espera a que el servidor definitivo acepte conexiones TCP (el temporal de inicialización no lo hace).
for i in $(seq 1 120); do
  if docker exec "$NOMBRE" sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqladmin ping -h127.0.0.1 -P3306 -uroot --silent' >/dev/null 2>&1; then
    echo "MySQL local listo tras $i intentos"
    break
  fi
  sleep 2
done

docker exec "$NOMBRE" sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -h127.0.0.1 -uroot -N -e "select version(), @@character_set_server, @@time_zone, @@lower_case_table_names; show databases like \"caja_local\""'
docker ps --filter "name=$NOMBRE" --format '{{.Names}} | {{.Image}} | {{.Status}} | {{.Ports}}'

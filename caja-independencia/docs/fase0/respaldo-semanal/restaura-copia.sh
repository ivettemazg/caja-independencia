#!/usr/bin/env bash
# Descifra una copia semanal del servidor Windows, comprueba sus huellas y la carga en un MySQL 8.4 local
# y desechable (contenedor caja-copia-semanal, solo en 127.0.0.1:3309). Nunca se conecta a la nube.
#
# Uso:  ./restaura-copia.sh ~/Downloads/caja-2026-11-16_0300.tgz.age
#
# - La llave privada se lee de ~/.config/caja-independencia/respaldo-age.key (o de $LLAVE).
# - Cada copia se carga en su propia base: copia_AAAAMMDD_HHMM.
# - Deja el manifiesto, los Excel y el conteo de filas en
#   ~/Documents/caja-independencia/copias-semanales/<nombre>/ y borra el .sql descifrado al terminar
#   (la fuente sigue siendo el archivo .age).
set -euo pipefail

LLAVE="${LLAVE:-$HOME/.config/caja-independencia/respaldo-age.key}"
AGE="${AGE:-age}"
CARPETA="${CARPETA:-$HOME/Documents/caja-independencia/copias-semanales}"
CONTENEDOR="${CONTENEDOR:-caja-copia-semanal}"
PUERTO="${PUERTO:-3309}"
ENV_LOCAL="${ENV_LOCAL:-$HOME/.config/caja-independencia/copia-semanal.env}"

[ $# -eq 1 ] || { echo "Uso: $0 <archivo .tgz.age>"; exit 1; }
archivo="$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
[ -f "$archivo" ] || { echo "No existe $archivo"; exit 1; }
[ -f "$LLAVE" ] || { echo "No encuentro la llave privada en $LLAVE"; exit 1; }
nombre="$(basename "$archivo" .tgz.age)"          # caja-2026-11-16_0300
fecha="${nombre#caja-}"
base="copia_${fecha//-/}"                          # copia_20261116_0300
destino="$CARPETA/$nombre"

umask 077
echo "== 1. Descifrar y desempacar en $destino"
mkdir -p "$destino"
"$AGE" -d -i "$LLAVE" "$archivo" | tar -xzf - -C "$destino"
sql="$destino/sindicato.sql"
trap 'rm -f "$sql"' EXIT
cat "$destino/manifiesto.txt"

echo "== 2. Comprobar huellas contra el manifiesto"
esperada=$(sed -n 's/^copia: sindicato.sql .*sha256=\([0-9a-f]*\).*/\1/p' "$destino/manifiesto.txt")
real=$(shasum -a 256 "$sql" | cut -d' ' -f1)
[ "$esperada" = "$real" ] || { echo "La huella de sindicato.sql NO coincide con el manifiesto"; exit 1; }
echo "sindicato.sql: OK"
while IFS= read -r linea; do
    x=$(printf '%s\n' "$linea" | sed 's/^excel: \(.*\)  bytes=.*/\1/')
    h=$(printf '%s\n' "$linea" | sed 's/.*sha256=\([0-9a-f]*\).*/\1/')
    [ "$(shasum -a 256 "$destino/excel/$x" | cut -d' ' -f1)" = "$h" ] || { echo "La huella de $x NO coincide"; exit 1; }
    echo "excel/$x: OK"
done < <(grep '^excel: ' "$destino/manifiesto.txt" || true)

echo "== 3. MySQL 8.4 local ($CONTENEDOR en 127.0.0.1:$PUERTO)"
if [ ! -f "$ENV_LOCAL" ]; then
    mkdir -p "$(dirname "$ENV_LOCAL")"
    printf 'MYSQL_ROOT_PASSWORD=%s\n' "$(openssl rand -hex 16)" > "$ENV_LOCAL"
    chmod 600 "$ENV_LOCAL"
fi
if ! docker ps -a --format '{{.Names}}' | grep -qx "$CONTENEDOR"; then
    docker run -d --name "$CONTENEDOR" -p "127.0.0.1:$PUERTO:3306" --env-file "$ENV_LOCAL" \
        -v "${CONTENEDOR//-/_}:/var/lib/mysql" mysql:8.4 >/dev/null
fi
docker start "$CONTENEDOR" >/dev/null
sql_local() { docker exec -i "$CONTENEDOR" sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -h127.0.0.1 "$@"' -- "$@"; }
for _ in $(seq 1 60); do sql_local -e 'SELECT 1' >/dev/null 2>&1 && break; sleep 2; done
sql_local -e 'SELECT 1' >/dev/null

echo "== 4. Cargar en la base $base"
if [ -n "$(sql_local -N -e "SHOW DATABASES LIKE '$base'")" ]; then
    echo "La base $base ya existe; bórrala primero si quieres recargarla:"
    echo "  docker exec -it $CONTENEDOR sh -c 'MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot -e \"DROP DATABASE $base\"'"
    exit 1
fi
sql_local -e "CREATE DATABASE $base"
inicio=$(date +%s)
sql_local "$base" < "$sql"
echo "Cargada en $(( $(date +%s) - inicio )) s"

echo "== 5. Conteo de filas por tabla (queda en $destino/conteos.txt)"
consulta=$(sql_local -N -e "SELECT GROUP_CONCAT(CONCAT('SELECT ''', TABLE_NAME, ''' tabla, COUNT(*) filas FROM \`$base\`.\`', TABLE_NAME, '\`') ORDER BY TABLE_NAME SEPARATOR ' UNION ALL ')
    FROM information_schema.TABLES WHERE TABLE_SCHEMA='$base' AND TABLE_TYPE='BASE TABLE'" --init-command='SET SESSION group_concat_max_len=1000000')
{
    sql_local -N -e "$consulta"
    sql_local -N -e "SELECT CONCAT('procedimiento ', ROUTINE_NAME) FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA='$base'"
} | tee "$destino/conteos.txt" | column -t
echo
echo "Listo. Base $base en 127.0.0.1:$PUERTO (usuario root; contraseña en $ENV_LOCAL)."

#!/bin/bash
# Una "fase" de la prueba de perfil y logs en la copia AISLADA con el WAR indicado:
# restaura las filas de prueba, levanta la copia, corre los 3 escenarios de perfil, el recorrido de contraseña
# y el de las 333 páginas, y guarda filas, log y resultados en <dir_salida>.
# Uso: prueba_perfil_fase.sh <war> <dir_salida>
set -uo pipefail
AQUI="$(cd "$(dirname "$0")" && pwd)"
WAR="$1"; O="$2"; mkdir -p "$O"
V="$HOME/.cache/caja-independencia/venv-navegador"
WEBAPP="$(cd "$AQUI/../../src/main/webapp" && pwd)"
SQL() { (echo 'set session group_concat_max_len = 1000000;'; cat) | docker exec -i caja-mysql80-control sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -h127.0.0.1 -uroot -N sindicato'; }
COLS=$(echo "select group_concat(concat('u.', column_name, ' = b.', column_name)) from information_schema.columns where table_schema = 'sindicato' and table_name = 'usuarios' and column_name <> 'usu_id';" | SQL)
HUELLA=$(echo "select concat('md5(concat_ws(\"|\",', group_concat(concat('ifnull(', column_name, ',\"(null)\")') order by ordinal_position), '))') from information_schema.columns where table_schema = 'sindicato' and table_name = 'usuarios';" | SQL)

# 1. restaurar las filas de prueba al estado guardado (respaldo creado una sola vez)
echo "create table if not exists zz_respaldo_usuarios_prueba as select * from usuarios where usu_id in (12548, 12555);" | SQL
echo "update usuarios u join zz_respaldo_usuarios_prueba b on b.usu_id = u.usu_id set $COLS;" | SQL
echo "select usu_id, $HUELLA from usuarios where usu_id in (12548, 12555) order by usu_id;" | SQL > "$O/filas_antes.txt"

# 2. levantar la copia con el WAR
cd "$AQUI" && ./levantar_copia_aislada.sh bajar >/dev/null 2>&1
./levantar_copia_aislada.sh subir "$WAR" 2>&1 | grep -vE "WARNING|Aplicación"
for i in $(seq 1 60); do curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8090/Sindicato/login.xhtml | grep -q 200 && break; sleep 3; done

# 3. escenarios de perfil
for e in admin_edita_socio socio_propio admin_propio; do
  "$V/bin/python" prueba_perfil.py $e 10002685 TECH SocioReal2026 2>&1 | grep -vE "^\s+at " | tee -a "$O/escenarios.txt"
done
echo "select usu_id, $HUELLA from usuarios where usu_id in (12548, 12555) order by usu_id;" | SQL > "$O/filas_despues.txt"

# 4. recorrido de contraseña (el restablecimiento escribe la contraseña) y revisión del log
python3 -I prueba_war_http.py 990901 NuevaWar2026 "2,TECH" admin-prueba@ensayo.local 24/12/26 > "$O/recorrido.txt" 2>&1
{ docker exec app-aislada sh -c 'cat /usr/local/tomcat/logs/sindicato.log* /usr/local/tomcat/logs/catalina.*.log'; docker logs app-aislada 2>&1; } > "$O/sindicato.log" 2>/dev/null
echo "contraseña de prueba en el log: $(grep -c 'NuevaWar2026\|SocioReal2026' "$O/sindicato.log") líneas" | tee "$O/log.txt"
echo "sentencias SQL impresas por show_sql: $(grep -c '^Hibernate: ' "$O/sindicato.log") líneas" | tee -a "$O/log.txt"
echo "valores ligados en el log (BasicBinder): $(grep -c 'BasicBinder' "$O/sindicato.log") líneas" | tee -a "$O/log.txt"

# 5. recorrido de las 333 páginas
python3 -I recorre_paginas.py "$WEBAPP" "$O/paginas.json" | tail -4
echo "excepciones en el log de Tomcat: $(docker logs app-aislada 2>&1 | grep -cE 'SEVERE|Exception')" | tee -a "$O/log.txt"

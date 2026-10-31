#!/bin/bash
# Compara las bases de CONTROL (8.0.31 local) y ENSAYO (8.4 en la nube) después de correr
# PruebaProcesos en ambas. Solo lee.
set -uo pipefail
AQUI="$(cd "$(dirname "$0")" && pwd)"
B="$HOME/.cache/caja-independencia/pruebas84"
J="$HOME/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
IP_ENSAYO=34.121.1.96
TABLAS=$(grep -o "sindicato\.[a-z_0-9]*" "$AQUI/huellas.sql" | paste -sd, -)

control() { docker exec caja-mysql80-control sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -h127.0.0.1 -uroot -N sindicato -e \"$1\"" 2>/dev/null; }
ensayo() { printf 'use sindicato;\n%s;\n' "$1" > "$B/q.sql"; java -cp "$J" "$AQUI/SqlEnsayo.java" "$IP_ENSAYO" app "$B/q.sql" 2>/dev/null \
             | grep -vE '^(Conectado|=== |> |\([0-9]+ filas\)|\(ok, .*\))' | grep -v '^\s*$' | sed 's/ | /\t/g'; }

echo "== 1. Checksum de las 43 tablas después de las pruebas =="
ensayo "checksum table $TABLAS" | awk '{print $1, $2}' | sort > "$B/final_ensayo.txt"
control "checksum table $TABLAS" | awk '{print $1, $2}' | sort > "$B/final_control.txt"
if diff "$B/final_control.txt" "$B/final_ensayo.txt" > "$B/diff_tablas.txt"; then
  echo "IDÉNTICAS: $(wc -l < "$B/final_control.txt" | tr -d ' ') tablas con el mismo contenido en 8.0 y en 8.4"
else
  echo "DIFERENCIAS en: $(grep '^<' "$B/diff_tablas.txt" | awk '{print $2}' | paste -sd' ' -)"
fi
echo "   tablas que cambiaron respecto al punto de partida: $(diff "$B/base_control.txt" "$B/final_control.txt" | grep '^>' | awk '{print $2}' | paste -sd' ' -)"

echo "== 2. Resultado del algoritmo sobre el archivo de prueba (estatus de pago, pagos, acumulado) =="
Q="select p.pag_estatus, count(*), round(sum(p.pag_acumulado),2) from pagos p join archivos_historial h on h.arh_id = p.pag_arh_id where h.arh_nombre_archivo like 'ENSAYO 2026-10-29%' group by 1 order by 1"
echo "-- control 8.0"; control "$Q"
echo "-- ensayo 8.4"; ensayo "$Q"
Q2="select a.amo_estatus_int, count(*) from amortizacion a join pagos p on p.pag_id = a.amo_pago_id join archivos_historial h on h.arh_id = p.pag_arh_id where h.arh_nombre_archivo like 'ENSAYO 2026-10-29%' group by 1 order by 1"
echo "-- renglones de amortización ligados (estatus, cantidad): control"; control "$Q2"
echo "-- ensayo"; ensayo "$Q2"

echo "== 3. Datos de los documentos (100 documentos de 20 solicitudes) =="
if diff -q "$B/docs-control/datos-documentos.txt" "$B/docs-ensayo/datos-documentos.txt" >/dev/null; then
  echo "IDÉNTICOS: $(grep -c '^## ' "$B/docs-control/datos-documentos.txt") documentos con exactamente los mismos datos"
else
  echo "DIFERENCIAS:"; diff "$B/docs-control/datos-documentos.txt" "$B/docs-ensayo/datos-documentos.txt" | head -20
fi
echo "   documentos con error: control $(grep -c ' ERROR ' "$B/docs-control/datos-documentos.txt") / ensayo $(grep -c ' ERROR ' "$B/docs-ensayo/datos-documentos.txt")"
echo "   PDF generados: control $(ls "$B/docs-control/pdf" | wc -l | tr -d ' ') / ensayo $(ls "$B/docs-ensayo/pdf" | wc -l | tr -d ' ')"

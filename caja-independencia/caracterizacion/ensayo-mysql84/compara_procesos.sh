#!/bin/bash
# Compara rendimiento mensual y pagos a capital entre CONTROL (8.0) y ENSAYO (8.4). Solo lee.
set -uo pipefail
AQUI="$(cd "$(dirname "$0")" && pwd)"
B="$HOME/.cache/caja-independencia/pruebas84"
J="$HOME/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
IP_ENSAYO=34.121.1.96
TABLAS=$(grep -o "sindicato\.[a-z_0-9]*" "$AQUI/huellas.sql" | paste -sd, -)
control() { docker exec caja-mysql80-control sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -h127.0.0.1 -uroot -N sindicato -e \"$1\"" 2>/dev/null; }
ensayo() { printf 'use sindicato;\n%s;\n' "$1" > "$B/q.sql"; java -cp "$J" "$AQUI/SqlEnsayo.java" "$IP_ENSAYO" app "$B/q.sql" 2>/dev/null \
             | grep -vE '^(Conectado|=== |> |\([0-9]+ filas\)|\(ok, .*\))' | grep -v '^\s*$' | sed 's/ | /\t/g'; }

echo "== 1. Rendimiento: valores calculados (pagos del mes, ahorro acumulado, factor, reserva...) =="
if diff "$B/ren-control/valores.txt" "$B/ren-ensayo/valores.txt"; then echo "IDÉNTICOS, bit a bit: $(wc -l < "$B/ren-control/valores.txt" | tr -d ' ') valores"; fi
echo "== 2. Rendimiento: movimiento calculado para cada socio y producto =="
N=$(wc -l < "$B/ren-control/movimientos.txt" | tr -d ' ')
if diff -q "$B/ren-control/movimientos.txt" "$B/ren-ensayo/movimientos.txt" >/dev/null; then
  echo "IDÉNTICOS, bit a bit: $((N + 1)) movimientos (socio, producto, aportación de origen y monto)"
else
  paste -d'|' "$B/ren-control/movimientos.txt" "$B/ren-ensayo/movimientos.txt" | awk -F'|' '
    { if ($1!=$5 || $2!=$6 || $3!=$7) llave++; d=$4-$8; if (d<0) d=-d; if (d>0) dist++; if (d>=0.005) cent++ }
    END { printf "renglones con llave distinta: %d | montos distintos en cualquier decimal: %d | distintos al centavo: %d\n", llave, dist, cent }'
fi
echo "== 3. Rendimiento: lo guardado en la base (fila del mes y movimientos) =="
Q="select ren_id, ren_estatus, ren_interes, ren_acumulado, ren_factor, ren_reserva, ren_ganancia_neta from rendimiento where ren_fecha = '2026-09-30'"
echo "-- control"; control "$Q"; echo "-- ensayo"; ensayo "$Q"
Q="select count(*), round(sum(mov_deposito), 6) from movimientos where mov_tipo = 'RENDIMIENTO' and mov_fecha = '2026-09-30'"
echo "-- movimientos guardados (cantidad, suma) control"; control "$Q"; echo "-- ensayo"; ensayo "$Q"
echo "== 4. Pagos a capital: tablas de amortización y créditos resultantes =="
if diff "$B/cap-control/pagos-capital.txt" "$B/cap-ensayo/pagos-capital.txt" > "$B/diff-capital.txt"; then
  echo "IDÉNTICOS: $(grep -c '^credito ' "$B/cap-control/pagos-capital.txt") créditos y $(grep -c '^  [0-9]' "$B/cap-control/pagos-capital.txt") renglones de amortización"
else
  echo "DIFERENCIAS:"; head -20 "$B/diff-capital.txt"
fi
echo "== 5. Checksum de las 43 tablas =="
ensayo "checksum table $TABLAS" | awk '{print $1, $2}' | sort > "$B/final2_ensayo.txt"
control "checksum table $TABLAS" | awk '{print $1, $2}' | sort > "$B/final2_control.txt"
if diff "$B/final2_control.txt" "$B/final2_ensayo.txt" > "$B/diff2.txt"; then
  echo "IDÉNTICAS: 43 tablas"
else
  echo "Tablas con checksum distinto: $(grep '^<' "$B/diff2.txt" | awk '{print $2}' | paste -sd' ' -)"
fi

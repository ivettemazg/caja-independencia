#!/bin/bash
# Recalcula la tabla de amortización de cada crédito con el código actual (TablaAmortizacionBo, sin
# modificar) y la compara contra los renglones guardados en el MySQL local. Solo lectura.
#
# Requisitos:
#   1. Proyecto compilado:            mvn clean package
#   2. MySQL local arriba:            ./mysql_local_setup.sh   (contenedor caja-mysql-local, 127.0.0.1:3307)
#   3. Copia local cargada:           ./copia_pruebas_a_local.sh creditos_final amortizacion solicitudes
set -euo pipefail
AQUI="$(cd "$(dirname "$0")" && pwd)"
PROYECTO="$(cd "$AQUI/.." && pwd)"
CP_FILE="$(mktemp)"
trap 'rm -f "$CP_FILE"' EXIT
(cd "$PROYECTO" && mvn -o -q dependency:build-classpath -Dmdep.outputFile="$CP_FILE")
java -cp "$AQUI:$PROYECTO/target/classes:$(cat "$CP_FILE")" "$AQUI/ComparaAmortizacion.java"

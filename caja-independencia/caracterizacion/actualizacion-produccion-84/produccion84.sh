#!/bin/bash
# Actualización de PRODUCCIÓN (evoti-sindicato) de MySQL 8.0 a 8.4, paso por paso. Ver PLAN.md.
# Nada se encadena solo: cada paso se ejecuta a mano y los que modifican algo exigen un texto de
# confirmación exacto como argumento. Los pasos de lectura usan la configuración caja-lectura y el
# usuario MySQL claude_lectura; solo "respaldo" y "actualizar" usan caja-admin.
#
#   ./produccion84.sh estado                              (lectura) versión, estado, flags, respaldos, operaciones
#   ./produccion84.sh huellas antes|despues               (lectura) checksum de las 43 tablas, evento, rutina
#   ./produccion84.sh comparar                            (lectura) compara las huellas antes/después
#   ./produccion84.sh respaldo CONFIRMO-RESPALDO          respaldo manual de la instancia
#   ./produccion84.sh usuario ver                         (lectura) método de contraseña del usuario de la app
#   ./produccion84.sh usuario cambiar|revertir CONFIRMO-35.225.67.182
#   ./produccion84.sh actualizar CONFIRMO-ACTUALIZAR-evoti-sindicato
#   ./produccion84.sh operacion <id>                      (lectura) espera a que termine una operación
set -uo pipefail
AQUI="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$AQUI/../../.." && pwd)"
PROYECTO=sindicatoindependencia
INSTANCIA=evoti-sindicato
FECHA_PLANEADA=20261114
WAR="${WAR:-$HOME/Documents/caja-independencia/war-fix-cambio-empresa/Sindicato.war}"
SALIDA="$HOME/.cache/caja-independencia/produccion84"
J="$HOME/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
API="https://sqladmin.googleapis.com/v1/projects/$PROYECTO"
LECTURA=(--configuration=caja-lectura --project="$PROYECTO")
mkdir -p "$SALIDA"

confirma() { # $1 = texto esperado, $2 = texto recibido
  if [ "${2:-}" != "$1" ]; then echo "NO SE HIZO NADA. Para este paso escribe como argumento: $1"; exit 3; fi
}
token_admin() { gcloud auth print-access-token --configuration=caja-admin 2>/dev/null || { echo "Sin sesión en caja-admin: gcloud auth login con esa configuración"; exit 1; }; }
token_lectura() { gcloud auth print-access-token --configuration=caja-lectura 2>/dev/null; }
resume_op() { python3 -I -c 'import sys,json; d=json.load(sys.stdin); e=d.get("error"); print("operación:", d.get("name"), "| tipo:", d.get("operationType"), "| estado:", d.get("status"), "| error:", e if e else "ninguno")'; }

case "${1:-}" in
  estado)
    gcloud sql instances describe "$INSTANCIA" "${LECTURA[@]}" \
      --format='table[box](name,databaseVersion,state,settings.tier,settings.dataDiskSizeGb,settings.backupConfiguration.enabled)'
    echo "Flags de la base:"; gcloud sql instances describe "$INSTANCIA" "${LECTURA[@]}" --format='value(settings.databaseFlags)' | tr ';' '\n'
    echo "Últimos respaldos:"; gcloud sql backups list --instance="$INSTANCIA" "${LECTURA[@]}" --limit=4 --format='table(id,windowStartTime,type,status,description)'
    echo "Últimas operaciones:"; gcloud sql operations list --instance="$INSTANCIA" "${LECTURA[@]}" --limit=4 --format='table(name,operationType,status,startTime,endTime)'
    ;;
  huellas)
    case "${2:-}" in antes|despues) ;; *) echo "Uso: $0 huellas antes|despues"; exit 1 ;; esac
    (cd "$REPO" && java -cp "$J" "$AQUI/HuellasBase.java" produccion "$SALIDA/huellas_$2.txt")
    echo "Guardada en $SALIDA/huellas_$2.txt"
    ;;
  comparar)
    A="$SALIDA/huellas_antes.txt"; D="$SALIDA/huellas_despues.txt"
    [ -f "$A" ] && [ -f "$D" ] || { echo "Faltan huellas antes/después"; exit 1; }
    echo "Versión: $(grep ^version "$A" | cut -f2) -> $(grep ^version "$D" | cut -f2)"
    if diff <(grep -E '^(checksum|filas|tablas|evento|rutina|event_scheduler)' "$A") <(grep -E '^(checksum|filas|tablas|evento|rutina|event_scheduler)' "$D"); then
      echo "IDÉNTICAS: mismas tablas, mismos datos, mismo evento y rutina."
    else
      echo "HAY DIFERENCIAS (arriba). No arranques Tomcat hasta revisarlas."; exit 4
    fi
    ;;
  respaldo)
    confirma "CONFIRMO-RESPALDO" "${2:-}"
    curl -s -X POST -H "Authorization: Bearer $(token_admin)" -H "Content-Type: application/json" \
      -d '{"description": "Antes de actualizar a MySQL 8.4"}' "$API/instances/$INSTANCIA/backupRuns" | resume_op
    ;;
  usuario)
    (cd "$REPO" && java -cp "$J" "$AQUI/UsuarioAppSha2.java" "$WAR" "${2:-ver}" ${3:+"$3"})
    ;;
  actualizar)
    confirma "CONFIRMO-ACTUALIZAR-$INSTANCIA" "${2:-}"
    if [ "$(date +%Y%m%d)" -lt "$FECHA_PLANEADA" ] && [ "${ADELANTAR:-}" != "si" ]; then
      echo "NO SE HIZO NADA: la fecha planeada es $FECHA_PLANEADA. Si se decidió adelantarla, usa ADELANTAR=si."; exit 3
    fi
    curl -s -X PATCH -H "Authorization: Bearer $(token_admin)" -H "Content-Type: application/json" \
      -d '{"databaseVersion": "MYSQL_8_4"}' "$API/instances/$INSTANCIA" | resume_op
    echo "Sigue con: $0 operacion <id de arriba>"
    ;;
  operacion)
    OP="${2:?Falta el id de la operación}"
    for i in $(seq 1 120); do
      R=$(curl -s -H "Authorization: Bearer $(token_lectura)" "$API/operations/$OP")
      echo "$R" | resume_op
      echo "$R" | grep -q '"status": "DONE"' && break
      sleep 30
    done
    ;;
  *) sed -n '2,16p' "$0"; exit 1 ;;
esac

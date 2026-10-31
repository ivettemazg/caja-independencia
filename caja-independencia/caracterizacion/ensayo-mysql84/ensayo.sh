#!/bin/bash
# Ensayo de la actualización a MySQL 8.4 sobre una COPIA de producción.
#
# Regla de este script: ningún comando modifica la instancia de producción (evoti-sindicato).
# Producción solo aparece como ORIGEN de lectura en `crear` (Cloud SQL documenta que clonar no
# afecta a la instancia de origen). Todo lo que cambia algo pasa por `solo_ensayo`, que se niega
# a operar sobre cualquier instancia que no sea la de ensayo.
#
# Uso: ./ensayo.sh crear | endurecer | contrasenas | estado | actualizar | borrar
set -euo pipefail

PROYECTO=sindicatoindependencia
PROD=evoti-sindicato
ENSAYO=evoti-sindicato-ensayo84
ADMIN=(--configuration=caja-admin --project="$PROYECTO")
LECTURA=(--configuration=caja-lectura --project="$PROYECTO")
CRED_ENSAYO="$HOME/.config/caja-independencia/ensayo.env"

solo_ensayo() {
  local destino="$1"
  if [ "$destino" != "$ENSAYO" ] || [ "$destino" = "$PROD" ]; then
    echo "BLOQUEADO: '$destino' no es la instancia de ensayo ($ENSAYO). No se ejecutó nada."
    exit 1
  fi
}

case "${1:-}" in
  crear)
    echo "Crear $ENSAYO como copia de $PROD. Producción es solo el origen y no se modifica."
    gcloud sql instances clone "$PROD" "$ENSAYO" "${ADMIN[@]}" --async
    ;;
  endurecer)
    solo_ensayo "$ENSAYO"
    MI_IP=$(curl -s --max-time 10 https://ipinfo.io/ip)
    echo "En $ENSAYO: redes autorizadas = solo $MI_IP; sin protección contra borrado (respaldos y registro binario quedan como en producción)."
    gcloud sql instances patch "$ENSAYO" "${ADMIN[@]}" --authorized-networks="$MI_IP/32" \
      --no-deletion-protection --quiet
    ;;
  contrasenas)
    # La copia no trae contraseñas. Se ponen contraseñas NUEVAS y distintas a las de producción,
    # de modo que nada configurado para la copia pueda autenticarse contra producción.
    solo_ensayo "$ENSAYO"
    if [ ! -f "$CRED_ENSAYO" ]; then
      (umask 077
       printf 'ENSAYO_APP_USER=sindicatoindependencia\nENSAYO_APP_PASSWORD=%s\nENSAYO_LECTURA_USER=claude_lectura\nENSAYO_LECTURA_PASSWORD=%s\n' \
         "$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 32 || true)" \
         "$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 32 || true)" > "$CRED_ENSAYO")
    fi
    chmod 600 "$CRED_ENSAYO"
    # shellcheck disable=SC1090
    source "$CRED_ENSAYO"
    echo "En $ENSAYO: contraseña nueva para sindicatoindependencia y claude_lectura (guardadas en $CRED_ENSAYO)."
    gcloud sql users set-password sindicatoindependencia --host=% --instance="$ENSAYO" "${ADMIN[@]}" --password="$ENSAYO_APP_PASSWORD" --quiet
    gcloud sql users set-password claude_lectura --host=% --instance="$ENSAYO" "${ADMIN[@]}" --password="$ENSAYO_LECTURA_PASSWORD" --quiet
    ;;
  estado)
    gcloud sql instances describe "$ENSAYO" "${LECTURA[@]}" \
      --format="table(name,state,databaseVersion,ipAddresses[0].ipAddress,settings.tier,settings.ipConfiguration.authorizedNetworks[].value.join(','),settings.backupConfiguration.enabled)"
    ;;
  actualizar)
    solo_ensayo "$ENSAYO"
    echo "Actualizar $ENSAYO a MySQL 8.4 (Cloud SQL toma respaldos antes y después)."
    # gcloud 466 no conoce MYSQL_8_4: se usa la API de Cloud SQL directamente, sobre $ENSAYO.
    TOKEN=$(gcloud auth print-access-token --configuration=caja-admin)
    curl -s -X PATCH -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
      -d '{"databaseVersion": "MYSQL_8_4"}' \
      "https://sqladmin.googleapis.com/v1/projects/$PROYECTO/instances/$ENSAYO"
    ;;
  borrar)
    solo_ensayo "$ENSAYO"
    echo "Borrar la instancia de ensayo $ENSAYO."
    # gcloud 466 manda parámetros de "respaldo final" que la API rechaza: se usa la API directamente.
    TOKEN=$(gcloud auth print-access-token --configuration=caja-admin)
    curl -s -X DELETE -H "Authorization: Bearer $TOKEN" \
      "https://sqladmin.googleapis.com/v1/projects/$PROYECTO/instances/$ENSAYO"
    ;;
  *)
    echo "Uso: $0 crear | endurecer | contrasenas | estado | actualizar | borrar"
    exit 1
    ;;
esac

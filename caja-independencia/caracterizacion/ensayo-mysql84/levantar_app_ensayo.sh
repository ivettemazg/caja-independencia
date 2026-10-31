#!/bin/bash
# Levanta en Docker la copia de la aplicación (Tomcat 8.5 + Java 11) y un buzón falso de correo.
#   Aplicación:      http://127.0.0.1:8089/Sindicato/
#   Correos de prueba: http://127.0.0.1:8025   (ningún correo sale a internet)
# Uso: ./levantar_app_ensayo.sh [subir | bajar]
set -euo pipefail
DESTINO="${DESTINO:-$HOME/.cache/caja-independencia/app-ensayo84}"
RED=ensayo84

case "${1:-subir}" in
  subir)
    docker network inspect "$RED" >/dev/null 2>&1 || docker network create "$RED" >/dev/null
    docker rm -f correo-ensayo app-ensayo84 >/dev/null 2>&1 || true
    docker run -d --name correo-ensayo --network "$RED" -p 127.0.0.1:8025:8025 \
      axllent/mailpit --smtp-auth-accept-any --smtp-auth-allow-insecure >/dev/null
    # Tomcat 8.5.85+ limita a 1,000 los parámetros por petición; la pantalla de ajuste de bancos
    # envía más con rangos amplios y la vista se pierde. Se usa 10,000, el límite de versiones
    # anteriores, para reproducir las condiciones de producción.
    SERVER_XML="$(dirname "$DESTINO")/server-ensayo84.xml"
    docker run --rm tomcat:8.5-jdk11 cat /usr/local/tomcat/conf/server.xml \
      | sed 's/maxParameterCount="1000"/maxParameterCount="10000"/' > "$SERVER_XML"
    grep -q 'maxParameterCount="10000"' "$SERVER_XML" || { echo "No se pudo ajustar maxParameterCount"; exit 1; }
    docker run -d --name app-ensayo84 --network "$RED" -p 127.0.0.1:8089:8080 \
      -v "$DESTINO/target/Sindicato.war:/usr/local/tomcat/webapps/Sindicato.war:ro" \
      -v "$SERVER_XML:/usr/local/tomcat/conf/server.xml:ro" \
      tomcat:8.5-jdk11 >/dev/null
    echo "Aplicación: http://127.0.0.1:8089/Sindicato/   Correos de prueba: http://127.0.0.1:8025"
    ;;
  bajar)
    docker rm -f correo-ensayo app-ensayo84 >/dev/null 2>&1 || true
    echo "Contenedores de ensayo detenidos y eliminados."
    ;;
esac

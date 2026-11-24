#!/bin/bash
# Actualización de PRODUCCIÓN (evoti-sindicato) de MySQL 8.0 a 8.4, paso por paso. Ver PLAN.md.
# Nada se encadena solo: cada paso se ejecuta a mano y los que modifican algo exigen un texto de
# confirmación exacto como argumento. Los pasos de lectura usan la configuración caja-lectura y el
# usuario MySQL claude_lectura; solo "respaldo" y "actualizar" usan caja-admin.
#
#   ./produccion84.sh estado                              (lectura) versión, estado, flags, respaldos, operaciones
#   ./produccion84.sh huellas antes|despues               (lectura) checksum de las 43 tablas, evento, rutina
#   ./produccion84.sh objetos antes|despues               (lectura) evento, rutina y disparadores con el usuario de la app
#   ./produccion84.sh variables antes|despues             (lectura) configuración del servidor (SHOW GLOBAL VARIABLES)
#   ./produccion84.sh hbm2ddl antes|despues               (lectura) qué ALTER/CREATE intentaría Hibernate al arrancar Tomcat
#   ./produccion84.sh planes antes|despues                (lectura) EXPLAIN de las consultas cuyo resultado depende del orden
#   ./produccion84.sh comparar                            (lectura) compara las huellas y los objetos antes/después
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
WAR="${WAR:-$HOME/Documents/caja-independencia/war-sesion-perfil/Sindicato.war}"
SALIDA="$HOME/.cache/caja-independencia/produccion84"
J="$HOME/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
API="https://sqladmin.googleapis.com/v1/projects/$PROYECTO"
LECTURA=(--configuration=caja-lectura --project="$PROYECTO")
mkdir -p "$SALIDA"

app_desempacada() { # clases y librerías del WAR de producción, para las herramientas que usan sus mappings
  if [ ! -f "$SALIDA/app/WEB-INF/classes/hibernate.cfg.xml" ]; then
    mkdir -p "$SALIDA/app" && chmod 700 "$SALIDA/app" && (cd "$SALIDA/app" && unzip -q -o "$WAR" 'WEB-INF/classes/*' 'WEB-INF/lib/*')
  fi
  [ -f "$SALIDA/app/log4j-silencio.properties" ] || printf 'log4j.rootLogger=ERROR, c\nlog4j.appender.c=org.apache.log4j.ConsoleAppender\nlog4j.appender.c.Target=System.err\nlog4j.appender.c.layout=org.apache.log4j.PatternLayout\nlog4j.appender.c.layout.ConversionPattern=%%p %%c{1} - %%m%%n\n' > "$SALIDA/app/log4j-silencio.properties"
}
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
  objetos)
    case "${2:-}" in antes|despues) ;; *) echo "Uso: $0 objetos antes|despues"; exit 1 ;; esac
    (cd "$REPO" && java -cp "$J" "$AQUI/ObjetosApp.java" "$WAR" "$SALIDA/objetos_$2.txt")
    echo "Guardada en $SALIDA/objetos_$2.txt"
    ;;
  variables)
    case "${2:-}" in antes|despues) ;; *) echo "Uso: $0 variables antes|despues"; exit 1 ;; esac
    app_desempacada
    : > "$SALIDA/variables.sql"
    for l in a b c d e f g h i j k l m n o p q r s t u v w x y z; do printf "show global variables like '%s%%';\n" "$l" >> "$SALIDA/variables.sql"; done
    (cd "$REPO" && java -cp "$J" "$AQUI/../ConsultaSoloLectura.java" "$SALIDA/app/WEB-INF/classes/hibernate.cfg.xml" "$SALIDA/variables.sql") > "$SALIDA/variables_$2.raw" 2>&1
    grep -E '^[a-z_0-9]+ \| ' "$SALIDA/variables_$2.raw" | grep -v '^Variable_name' | sort -u > "$SALIDA/variables_$2.txt"
    echo "Variables guardadas: $(wc -l < "$SALIDA/variables_$2.txt") en $SALIDA/variables_$2.txt"
    ;;
  hbm2ddl)
    case "${2:-}" in antes|despues) ;; *) echo "Uso: $0 hbm2ddl antes|despues"; exit 1 ;; esac
    app_desempacada
    (cd "$SALIDA" && java -Dlog4j.configuration="file:$SALIDA/app/log4j-silencio.properties" -Dorg.jboss.logging.provider=log4j \
      -cp "$SALIDA/app/WEB-INF/classes:$SALIDA/app/WEB-INF/lib/*" "$AQUI/SimulaHbm2ddl.java" produccion "$SALIDA/hbm2ddl_$2.txt" 2>&1 | grep -v -i 'illegal\|^WARNING')
    if [ -s "$SALIDA/hbm2ddl_$2.txt" ]; then echo "HIBERNATE CAMBIARÍA EL ESQUEMA AL ARRANCAR (arriba). No arranques Tomcat hasta revisarlo."; exit 4; fi
    echo "Sin cambios: al arrancar Tomcat, Hibernate no alterará el esquema."
    ;;
  planes)
    case "${2:-}" in antes|despues) ;; *) echo "Uso: $0 planes antes|despues"; exit 1 ;; esac
    (cd "$REPO" && java -cp "$J" "$AQUI/PlanesSql.java" produccion "$AQUI/planes.sql" "$SALIDA/planes_$2")
    ;;
  comparar)
    PA="$SALIDA/planes_antes.esqueleto"; PD="$SALIDA/planes_despues.esqueleto"
    if [ -f "$PA" ] && [ -f "$PD" ]; then
      python3 -I - "$PA" "$PD" <<'PY'
import sys
def lee(f):
    d, k = {}, None
    for l in open(f, encoding='utf-8'):
        l = l.rstrip('\n')
        if l.startswith('== '): k = l[3:]; d[k] = []
        elif k: d[k].append(l)
    return d
a, b = lee(sys.argv[1]), lee(sys.argv[2])
cambian = [k for k in a if a[k] != b.get(k)]
print(f"Planes de las consultas sensibles al orden: {len(a) - len(cambian)} de {len(a)} iguales (informativo).")
for k in cambian: print("  CAMBIÓ:", k)
if cambian: print("  Ver diferencias: diff planes_antes.txt planes_despues.txt; vigilar esos procesos (ANALISIS-IMPACTO.md).")
PY
    fi
    VA="$SALIDA/variables_antes.txt"; VD="$SALIDA/variables_despues.txt"
    if [ -f "$VA" ] && [ -f "$VD" ]; then
      echo "Configuración del servidor que cambió (informativo; lo esperado por la versión no detiene nada):"
      join -t'|' -j1 <(sed 's/ | /|/' "$VA" | sort -t'|' -k1,1) <(sed 's/ | /|/' "$VD" | sort -t'|' -k1,1) | awk -F'|' '$2!=$3' \
        | grep -E '^(sql_mode|time_zone|system_time_zone|character_set_server|collation_server|transaction_isolation|explicit_defaults_for_timestamp|lower_case_table_names|group_concat_max_len|max_allowed_packet|wait_timeout|max_connections|event_scheduler|innodb_adaptive_hash_index|innodb_buffer_pool_size|innodb_change_buffering|temptable_max_ram|tmp_table_size|sort_buffer_size|join_buffer_size|require_secure_transport|tls_version|div_precision_increment|local_infile|innodb_io_capacity|innodb_log_buffer_size|internal_tmp_mem_storage_engine)\|' \
        | awk -F'|' '{printf "  %-34s %s  ->  %s\n", $1, substr($2,1,60), substr($3,1,60)}'
      echo "  (variables nuevas en 8.4: $(comm -13 <(cut -d' ' -f1 "$VA" | sort) <(cut -d' ' -f1 "$VD" | sort) | wc -l | tr -d ' '); quitadas: $(comm -23 <(cut -d' ' -f1 "$VA" | sort) <(cut -d' ' -f1 "$VD" | sort) | wc -l | tr -d ' '))"
      for v in sql_mode time_zone lower_case_table_names event_scheduler; do
        if [ "$(grep "^$v | " "$VA")" != "$(grep "^$v | " "$VD")" ]; then echo "CAMBIÓ $v: revisar antes de arrancar Tomcat."; exit 4; fi
      done
    fi
    OA="$SALIDA/objetos_antes.txt"; OD="$SALIDA/objetos_despues.txt"
    [ -f "$OA" ] && [ -f "$OD" ] || { echo "Faltan objetos antes/después"; exit 1; }
    if ! diff <(grep -Ev '^(version|ultima_ejecucion)' "$OA") <(grep -Ev '^(version|ultima_ejecucion)' "$OD"); then
      echo "EL EVENTO, LA RUTINA O LOS DISPARADORES CAMBIARON (arriba). No arranques Tomcat hasta revisarlo."; exit 4
    fi
    A="$SALIDA/huellas_antes.txt"; D="$SALIDA/huellas_despues.txt"
    [ -f "$A" ] && [ -f "$D" ] || { echo "Faltan huellas antes/después"; exit 1; }
    echo "Versión: $(grep ^version "$A" | cut -f2) -> $(grep ^version "$D" | cut -f2)"
    if diff <(grep -E '^(checksum|filas|tablas|evento|rutina|event_scheduler)' "$A") <(grep -E '^(checksum|filas|tablas|evento|rutina|event_scheduler)' "$D"); then
      echo "IDÉNTICAS: mismas tablas, mismos datos, mismo evento, rutina y disparadores."
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
  *) sed -n '2,18p' "$0"; exit 1 ;;
esac

# Plan: arreglo de cambios de empresa y actualización de producción a MySQL 8.4

Instancia de producción: `evoti-sindicato` (proyecto `sindicatoindependencia`), hoy MySQL 8.0.31.
Fecha acordada para 8.4: **sábado 14 de noviembre de 2026, por la noche** (después de la catorcena
del 12 de noviembre; antes del cobro por soporte extendido, que empieza el 1 de enero de 2027).
**Se movió al domingo 15 de noviembre de 2026, de día** (Ivette no pudo el sábado). Terminar antes de las
20:30: el evento de borrado corre a las 21:00 y cambiaría datos entre la huella de antes y la de después.

Los comandos se corren desde esta carpeta: `caracterizacion/actualizacion-produccion-84/`.

---

## Parte 1. Esta semana (antes de cargar la catorcena del 29 de octubre)

> **YA HECHA (octubre).** Se conserva como registro; no se vuelve a ejecutar.

### 1.1 Subir el WAR con el arreglo
WAR: `~/Documents/caja-independencia/war-fix-cambio-empresa/Sindicato.war`. Es el de producción más las 2
clases corregidas de cambios de empresa; trae el CSS de tablas, el login y la recuperación de contraseña.

1. En el servidor, copiar a otro lado el `Sindicato.war` actual (es el regreso si algo falla).
2. Detener el servicio de Tomcat.
3. Borrar `webapps\Sindicato\` (la carpeta desempacada) y `work\Catalina\localhost\Sindicato\`.
4. Copiar el WAR nuevo a `webapps\`.
5. Iniciar Tomcat y entrar al sistema.

No cambia mappings: al arrancar no se altera la base.

### 1.2 Aplicar los cambios de empresa pendientes
En **Aplicar altas y cambios de empresa**:

| Catorcena | Resultado esperado (simulado en producción y probado en la copia) |
|---|---|
| 01/10/2026 | "No hay cambios de empresa en esta catorcena." |
| 15/10/2026 | "Se procesaron 1cambios de empresa." → socio 8986, SEGURA MONTIEL EDGARDO ADI, pasa de CAR (clave 349817) a AMX (clave 158522) |

Si aparece algo distinto, detenerse y revisarlo antes de seguir.

### 1.3 Quitar la baja al socio 8986 (decisión: opción a)
**Reporte de aportaciones** → Catorcena 15/10/2026, Empresa AMX → Consultar → pestaña **Usuarios con Baja** →
marcar **solo** a SEGURA MONTIEL EDGARDO ADI (en esa tabla aparecen otras 11 cuentas con baja; no tocarlas) →
clic derecho → **Reactivar usuarios** → **Sí**.

Queda activo, sin fecha de baja. Su proceso de baja (6,833.61 en "ahorros por devolver") ya no aparecerá en
esa lista, porque la lista solo muestra socios con fecha de baja: los ahorros se quedan en su cuenta.

Los demás pendientes de esas catorcenas (11 del 01/10 y 37 del 15/10) son personas nuevas: se dan de alta
como siempre en la pantalla de altas.

---

## Parte 2. Días antes del 14 de noviembre

> **Revisada el domingo 15 de noviembre** (lo marcado con [x]).

- [x] Avisar a la caja y a Jorge (Ivette, el 15 de noviembre): el domingo 15 desde las 6:00 pm el sistema estará fuera alrededor de una hora.
- [ ] No programar carga de archivos ni cálculo de rendimiento para ese día.
- [x] Confirmar que nadie se conecta a la base con el usuario `sindicatoindependencia` desde un programa
      viejo (MySQL Workbench anterior a 8, conectores ODBC antiguos). Tomcat no tiene problema.
- [x] Revisar que la sesión de administración funciona: `gcloud auth print-access-token --configuration=caja-admin`.
      Si falla: `gcloud config configurations activate caja-admin && gcloud auth login`, y después volver a
      `gcloud config configurations activate caja-lectura`.
- [x] `./produccion84.sh estado` → debe decir `MYSQL_8_0_31` y `RUNNABLE`.

---

## Parte 3. La noche del 14 de noviembre (se hace el domingo 15, desde las 6:00 pm)

> **ESTA ES LA PARTE QUE SE EJECUTA.** Del paso 0 al 11, en orden.

| # | Quién | Qué | Qué debe pasar |
|---|---|---|---|
| 0 | Ivette | `./produccion84.sh estado` | `MYSQL_8_0_31`, `RUNNABLE` |
| 1 | Ivette | **Detener Tomcat** en el servidor | Nadie puede escribir en la base durante el cambio |
| 2 | — | `./produccion84.sh huellas antes`, `objetos antes`, `variables antes`, `hbm2ddl antes` y `planes antes` | 43 tablas; el evento `ev_eliminar_solicitudes_antiguas` ENABLED, su rutina y 0 disparadores (`objetos` usa el usuario de la app porque `claude_lectura` no los ve; solo lee); 744 variables; Hibernate: 0 sentencias; 18 planes |
| 3 | — | `./produccion84.sh respaldo CONFIRMO-RESPALDO` | Operación `BACKUP_VOLUME`. Cloud SQL además hace uno automático antes y otro después |
| 4 | — | `./produccion84.sh usuario cambiar CONFIRMO-35.225.67.182` | "Método nuevo: caching_sha2_password". Misma contraseña; es lo que se probó en el ensayo. Si este paso marca error de permisos, se omite: 8.4 sigue aceptando el método anterior |
| 5 | — | `./produccion84.sh actualizar CONFIRMO-ACTUALIZAR-evoti-sindicato` | Devuelve el id de la operación, estado `PENDING` o `RUNNING` |
| 6 | — | `./produccion84.sh operacion <id>` | Espera hasta `DONE` sin error (en el ensayo: unos 12 minutos) |
| 7 | — | `./produccion84.sh estado` | `MYSQL_8_4`, `RUNNABLE` |
| 8 | — | `./produccion84.sh huellas despues`, `objetos despues`, `variables despues` y `planes despues` | Igual que en el paso 2 |
| 9 | — | `./produccion84.sh comparar` | Cuántos de los 18 planes quedaron iguales (informativo; en la vista previa local, 17 de 18 y el distinto no altera resultados), lista informativa de la configuración que cambió y "IDÉNTICAS" (tablas y objetos programados). Si no, o si cambió `sql_mode`, `time_zone`, `lower_case_table_names` o `event_scheduler`, **no arrancar Tomcat** y revisar |
| 9b | — | `./produccion84.sh hbm2ddl despues` | "Sin cambios": al arrancar, Hibernate no alterará el esquema (`hbm2ddl.auto=update`). Si propone algún ALTER/CREATE, **no arrancar Tomcat** y revisar |
| 10 | Ivette | **Iniciar Tomcat** y probar: login, una consulta de créditos, reporte de aportaciones, recuperar contraseña | Todo funciona, log sin errores. Qué revisar en el log y qué observar los días siguientes: `ANALISIS-IMPACTO.md` |
| 11 | Ivette | Avisar a la caja | — |

Los pasos 3, 4 y 5 modifican producción. Los corre Ivette, o Claude solo después de que ella lo confirme en
ese momento. El script se niega a correr `actualizar` antes del 14 de noviembre (si se decide adelantar:
`ADELANTAR=si`).

---

## Si algo sale mal

- **Falla la actualización (paso 6 con error):** Cloud SQL regresa sola la instancia a 8.0. Arrancar Tomcat y
  revisar el error con calma.
- **La app no conecta después del paso 4:** `./produccion84.sh usuario revertir CONFIRMO-35.225.67.182`
  (regresa al método anterior con la misma contraseña).
- **La actualización termina pero algo no funciona después:** no se puede regresar la misma instancia a 8.0.
  La salida es restaurar el respaldo previo a la actualización en una **instancia nueva** con 8.0, que tiene
  otra IP, y subir un WAR apuntando a ella. Como Tomcat estuvo detenido, no se pierde ningún dato. Es la razón
  del ensayo: en la copia todos los procesos dieron exactamente lo mismo en 8.0 y 8.4.

## Qué pasa con la versión 8.0 después

- La actualización es **en el mismo lugar**: la misma instancia `evoti-sindicato` (mismo nombre, IP, usuarios,
  contraseñas y datos) pasa a 8.4. No queda una segunda base con 8.0 funcionando ni hay nada que borrar.
- Lo que queda de 8.0 son **respaldos**:
  - el respaldo manual del paso 3: se conserva hasta que se borre a mano;
  - el que Cloud SQL hace solo justo antes de actualizar (confirmar con `estado` después del paso 6);
  - los automáticos diarios: la instancia guarda solo los 3 últimos, así que hacia el miércoles 18 de
    noviembre ya serán todos de 8.4.
- Para qué sirven: si algo saliera mal después, se restaura uno en una **instancia nueva** con 8.0 (ver arriba).
  Todo lo capturado después del respaldo se perdería, así que su valor baja con cada día que pasa.
- Cuándo borrar el respaldo manual: cuando 8.4 haya pasado por la carga de la catorcena del 26 de noviembre y
  por el rendimiento de noviembre (principios de diciembre). Lo borra Ivette o Claude con su confirmación.

## Costo
Nada adicional: solo el espacio de los respaldos manuales y de actualización (unos pocos GB). Se pueden
borrar unas semanas después.

-- Usuario de solo lectura para el respaldo semanal (GUIA.md, paso 6).
--
-- MODIFICA PRODUCCION: crea un usuario de la base; no toca ningun dato.
-- Se ejecuta una sola vez, despues de la actualizacion a MySQL 8.4 y con la confirmacion de Ivette.
-- El paso 6 de la guia sustituye <IP_SERVIDOR> y <CONTRASENA> en memoria; no edites este archivo.
-- (Sin acentos a proposito: Windows lo lee como ANSI.)

-- 1. Antes: que objetos programados hay. Se espera 1 procedimiento, 1 evento y 0 disparadores.
SELECT ROUTINE_TYPE, ROUTINE_NAME FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA = 'sindicato';
SELECT EVENT_NAME, STATUS FROM information_schema.EVENTS WHERE EVENT_SCHEMA = 'sindicato';
SELECT COUNT(*) AS disparadores FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA = 'sindicato';

-- 2. El usuario: solo desde la IP del servidor, solo con conexion cifrada y solo lectura.
CREATE USER 'respaldo_semanal'@'<IP_SERVIDOR>'
    IDENTIFIED WITH caching_sha2_password BY '<CONTRASENA>'
    REQUIRE SSL;
GRANT SELECT, SHOW VIEW ON sindicato.* TO 'respaldo_semanal'@'<IP_SERVIDOR>';

-- Permite leer la definicion del procedimiento sp_eliminar_solicitudes_antiguas para incluirlo en la copia.
-- No permite ejecutarlo ni cambiarlo. Si Cloud SQL rechaza esta linea, no pasa nada: el procedimiento
-- queda documentado en objetos-programados.sql.
GRANT SHOW_ROUTINE ON *.* TO 'respaldo_semanal'@'<IP_SERVIDOR>';

-- 3. Verificacion: deben salir USAGE, SELECT y SHOW VIEW sobre sindicato, y SHOW_ROUTINE.
SHOW GRANTS FOR 'respaldo_semanal'@'<IP_SERVIDOR>';

-- Para revertir, si algun dia se deja de usar:
--   DROP USER 'respaldo_semanal'@'<IP_SERVIDOR>';

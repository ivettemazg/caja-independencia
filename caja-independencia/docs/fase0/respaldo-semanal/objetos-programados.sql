-- Objetos programados de la base `sindicato` que la copia semanal NO incluye completos.
--
-- Por que no van en la copia:
--   * El evento: para copiarlo, el usuario de respaldo necesitaria el permiso EVENT, que tambien permite
--     crear, cambiar y borrar eventos. Ademas, en la replica semanal el borrado NO debe correr solo
--     (se aplica como parche con sus ids, ver diseno-bitacora.md seccion 8).
--   * El procedimiento si va en la copia si Cloud SQL acepta el permiso SHOW_ROUTINE; si no, aqui esta.
--
-- Origen: copia de control del 2026-10-21 (clon de produccion). Los creo alguien mas el 2026-03-10.
-- En produccion el evento esta habilitado; corre a las 03:00 UTC (21:00 en la Ciudad de Mexico) y borra
-- las solicitudes en estatus 1 con mas de 2 meses, con sus avales e imagenes.
--
-- Solo para reconstruir la base en una recuperacion de desastre, con el usuario de la aplicacion (queda como
-- DEFINER). NO ejecutar en una copia de trabajo.

DELIMITER ;;

CREATE PROCEDURE `sp_eliminar_solicitudes_antiguas`()
BEGIN
    START TRANSACTION;

    DELETE sa
    FROM solicitud_avales sa
    INNER JOIN solicitudes s
        ON s.sol_id = sa.sol_ava_solicitud
    WHERE s.sol_estatus = 1
      AND s.sol_fecha_creacion <= DATE_SUB(NOW(), INTERVAL 2 MONTH);

    DELETE i
    FROM imagenes i
    INNER JOIN solicitudes s
        ON s.sol_id = i.ima_solicitud
    WHERE s.sol_estatus = 1
      AND s.sol_fecha_creacion <= DATE_SUB(NOW(), INTERVAL 2 MONTH);

    DELETE s
    FROM solicitudes s
    WHERE s.sol_estatus = 1
      AND s.sol_fecha_creacion <= DATE_SUB(NOW(), INTERVAL 2 MONTH);

    COMMIT;
END ;;

CREATE EVENT `ev_eliminar_solicitudes_antiguas`
    ON SCHEDULE EVERY 1 DAY STARTS '2026-03-11 03:00:00'
    ON COMPLETION NOT PRESERVE ENABLE
    DO BEGIN
        CALL sp_eliminar_solicitudes_antiguas();
    END ;;

DELIMITER ;

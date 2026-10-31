-- Créditos PAGADOS cuyo último renglón quedó pendiente y cuyo pago de esa catorcena se fue a
-- acumulado (pag_estatus 7, "sin amortización"). Son los casos que hoy se corrigen a mano.
-- Solo lectura. Correr con:
--   java -cp ~/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar \
--        caracterizacion/ConsultaSoloLectura.java src/main/resources/hibernate.cfg.xml \
--        caracterizacion/consultas/pagados_con_ultimo_pago_en_acumulado.sql

-- Pagados con el último pago en acumulado (sin datos personales)
select c.cre_id, c.cre_catorcenas as plazo, u.amo_numero_pago as ultimo_renglon, u.amo_fecha_pago as catorcena,
       u.amo_monto_pago as monto_renglon, p.pag_id, p.pag_deposito, p.pag_acumulado, p.pag_arh_id as archivo
from sindicato.creditos_final c
join sindicato.amortizacion u on u.amo_credito = c.cre_id
 and u.amo_id = (select x.amo_id from sindicato.amortizacion x where x.amo_credito = c.cre_id order by x.amo_numero_pago desc, x.amo_id desc limit 1)
join sindicato.pagos p on p.pag_usu_id = c.cre_usu_id and p.pag_fecha = u.amo_fecha_pago and p.pag_estatus = 7
where c.cre_estatus = 2 and c.cre_producto in (6,7) and u.amo_estatus_int = 1
order by u.amo_fecha_pago desc;

-- Desactivar el borrado programado SOLO en la copia (queda definido; se revisa que sobreviva la actualizacion)
alter event sindicato.ev_eliminar_solicitudes_antiguas disable;

-- Cambiar el usuario de la aplicacion al metodo de autenticacion nuevo (misma contrasena de ensayo)
alter user 'sindicatoindependencia'@'%' identified with caching_sha2_password by '{{PW_APP}}';

-- Verificacion
select user, host, plugin from mysql.user where user in ('sindicatoindependencia','claude_lectura','root') and host = '%';
select event_name, status from information_schema.events where event_schema = 'sindicato';

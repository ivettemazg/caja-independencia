-- Usuarios y metodo de autenticacion (sin hashes)
select user, host, plugin from mysql.user where user not like 'mysql.%' order by user;

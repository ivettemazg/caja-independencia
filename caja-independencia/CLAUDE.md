# Caja Independencia — Contexto de trabajo

Sistema de **caja de ahorro y préstamos** para el Sindicato Nacional de Trabajadores de la Industria
Aeronáutica. Aplicación Java EE legacy (JSF 2.3 + Hibernate 4.3 + MySQL) empaquetada como
`Sindicato.war` y desplegada en Tomcat 8 bajo el contexto `/Sindicato`.

## Documentación detallada (leer según la tarea)

| Archivo | Cuándo leerlo |
| --- | --- |
| [.claude/docs/01-arquitectura.md](.claude/docs/01-arquitectura.md) | Stack, capas, build, deploy, arranque, configuración |
| [.claude/docs/02-dominio-y-reglas.md](.claude/docs/02-dominio-y-reglas.md) | Reglas de negocio, productos de crédito, tasas, catálogos de estatus |
| [.claude/docs/03-base-de-datos.md](.claude/docs/03-base-de-datos.md) | Esquema completo, relaciones, columnas, catálogos, cómo conectarse |
| [.claude/docs/04-modulos-y-navegacion.md](.claude/docs/04-modulos-y-navegacion.md) | Mapa vista XHTML → bean → BO → DAO de cada módulo |
| [.claude/docs/05-flujos-criticos.md](.claude/docs/05-flujos-criticos.md) | Flujos paso a paso: solicitud→fondeo→pagos→finiquito, rendimiento, conciliación |
| [.claude/docs/06-convenciones-y-riesgos.md](.claude/docs/06-convenciones-y-riesgos.md) | Patrones de código, trampas conocidas, deuda técnica |
| [.claude/docs/07-recetario.md](.claude/docs/07-recetario.md) | Recetas concretas: agregar pantalla, campo, estatus, reporte |

`docs/AI_CONTEXT.md` es un resumen anterior, más superficial; esta carpeta lo reemplaza.

## Reglas de oro (leer siempre antes de tocar código)

1. **Los mappings Hibernate efectivos viven en `src/main/resources/mx/com/evoti/hibernate/pojos/`.**
   Existe una copia obsoleta e idéntica en `src/main/java/mx/com/evoti/hibernate/pojos/` que
   **no se empaqueta**. Editar la copia de `java/` no tiene ningún efecto en runtime.
   Ver [06-convenciones-y-riesgos.md](.claude/docs/06-convenciones-y-riesgos.md#mappings-duplicados).

2. **`hbm2ddl.auto=update` está activo.** Cualquier cambio a un `.hbm.xml` puede alterar el esquema
   de producción al arrancar Tomcat. Nunca agregues/renombres columnas en un mapping sin decidir
   antes el `ALTER TABLE` explícito.

3. **La mayoría del acceso a datos es SQL nativo construido con `String.format`.** Los alias SQL
   deben coincidir exactamente con las propiedades del DTO (`Transformers.aliasToBean`). Si
   renombras una columna o propiedad, busca con
   `grep -rn "nombre_columna" src/main/java` antes de dar por terminado el cambio.

4. **Las contraseñas se guardan y comparan en texto plano** (`usuarios.usu_password` vs
   `LoginBean.validaLogin`). No es un descuido puntual: el flujo de recuperación
   (`PasswordRecoveryBo`) y el reset del panel admin también escriben texto plano. Si vas a
   introducir hashing, hay que migrar los tres puntos a la vez.

5. **`LoginDao.login()` interpola el usuario directamente en el SQL** → inyección SQL en el punto
   de entrada sin autenticar. Mismo patrón en decenas de DAOs.

6. **No hay pruebas automatizadas.** `src/test/java` existe pero está vacío. La verificación es
   manual: `mvn clean package` + despliegue.

7. **Credenciales versionadas.** `src/main/resources/hibernate.cfg.xml` contiene host, usuario y
   contraseña de la BD de producción; `EnviaCorreo` contiene la contraseña SMTP. No los repliques
   en commits, issues, ni en documentación nueva.

## Comandos

```bash
mvn clean package                 # compilar + generar target/Sindicato.war
mvn clean install -U              # cuando cambia el pom.xml
mvn tomcat7:redeploy              # redesplegar en Tomcat

# Despliegue local "a mano" (el más confiable según el Readme)
rm -rf /opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/webapps/Sindicato*
mvn clean package -DskipTests
cp target/Sindicato.war /opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/webapps/
```

Ver `Readme.md` para arranque/paro de Tomcat, debug remoto (puerto 5005) y logs.

## Convenciones al escribir código aquí

- Sigue el patrón local: `Bean` (JSF) → `Bo` (negocio) → `Dao` (datos), dependencias construidas
  con `new`, sin CDI ni Spring. No introduzcas un framework nuevo en un cambio puntual.
- Beans de pantalla extienden `BaseBean` y llaman `super.validateUser()` al inicio.
- Los DAOs extienden `ManagerDB`; usa `beginTransaction()`/`endTransaction()` en lecturas y los
  helpers `savePojo`/`updatePojo`/`executeUpdateSql` en escrituras (ya manejan commit/rollback).
- Estatus, productos y roles se declaran en `mx.com.evoti.util.Constantes`. Revísalo antes de
  inventar un valor nuevo.
- El código está en español (nombres, comentarios, mensajes). Mantén ese idioma.
- Existe un paquete `org.primefaces.spark` vendorizado (tema/plantilla, 150 clases). No lo toques
  salvo que el cambio sea específicamente de UI/tema.

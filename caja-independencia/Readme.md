# 🧹 Flujo de Trabajo Maven + Tomcat

## ✅ 1. Después de hacer cambios en el `pom.xml`

Cada vez que cambies dependencias, plugins o configuraciones del `pom.xml`, ejecuta:

```bash
mvn clean install -U
```

---

## ✅ 2. Para compilar y generar el WAR

Si **solo hiciste cambios en código** (no en el `pom.xml`), puedes usar:

```bash
mvn clean package
```

---

## ✅ 3. Para desplegar el WAR en Tomcat

Si ya configuraste el plugin `tomcat7-maven-plugin` en el `pom.xml`, usa:

```bash
mvn tomcat7:deploy
```

🔹 Esto:

* Sube el WAR a Tomcat en el path configurado (ej. `/Sindicato`).
* Usa las credenciales de `~/.m2/settings.xml` y la URL del plugin.

---

## ✅ 4. ¿Y si el WAR ya está desplegado en Tomcat?

Si el WAR ya está desplegado y solo quieres actualizarlo, usa:

```bash
mvn tomcat7:redeploy
```

🔹 Esto elimina el WAR anterior y lo vuelve a subir en el mismo path.

---

## ✅ 5. Para eliminar el WAR del servidor Tomcat

Puedes quitar la aplicación desplegada desde Maven con:

```bash
mvn tomcat7:undeploy
```

🔹 Esto elimina la app en el path definido (ej. `/Sindicato`), pero **no borra nada localmente**.

---

## 📋 Resumen de Comandos Maven para tu flujo

| Etapa                          | Comando                |
| ------------------------------ | ---------------------- |
| Cambios en el `pom.xml`        | `mvn clean install -U` |
| Compilar y generar el WAR      | `mvn clean package`    |
| Desplegar WAR en Tomcat        | `mvn tomcat7:deploy`   |
| Volver a desplegar (ya existe) | `mvn tomcat7:redeploy` |
| Eliminar WAR de Tomcat         | `mvn tomcat7:undeploy` |

---

## 🛠️ Parar e Iniciar Tomcat

```bash
/opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/bin/shutdown.sh

brew services stop tomcat@8      
brew services start tomcat@8

/opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/bin/startup.sh
```

---

## 🌐 Agregar IP Pública a la Whitelist de MySQL (Google Cloud)

Para obtener la IP pública en macOS:

```bash
curl https://ipinfo.io/ip
```

Pega esta IP en la lista de IPs permitidas en Google Cloud SQL.

---

## 🐞 Para Hacer Debug en Tomcat

Desde la ubicación de `CATALINA_HOME/bin`:

```bash
cd /opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/bin

export JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
./startup.sh
```

Luego, inicia la depuración desde tu IDE.

---

## ❌ Para matar Tomcat

Comando rápido con alias:

```bash
alias tomcat-stop="/opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/bin/shutdown.sh"
```

Si Tomcat se queda colgado:

```bash
ps aux | grep tomcat
kill -9 <pid>
```


Para eliminar y volver a cargar todo cuando tenemos duda si no se desplegaron los ultimos cambios
rm -rf /opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/webapps/Sindicato*
mvn clean package -DskipTests
cp target/Sindicato.war /opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/webapps/
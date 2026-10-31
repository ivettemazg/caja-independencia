"""Recorre por HTTP la copia AISLADA de la aplicación (levantar_copia_aislada.sh), como lo haría un
navegador: login de administrador, pantalla "Aplicar altas y cambios de empresa", recuperación y
restablecimiento de contraseña con el correo que llega al buzón falso, y login con la contraseña nueva.
Solo habla con 127.0.0.1:8090 (app) y 127.0.0.1:8026 (buzón); cualquier otra URL se rechaza.
Uso: python3 -I prueba_war_http.py <clave_admin> <password_admin> <empresa_admin> <correo_admin> <catorcena MM/dd/yyyy>
"""
import http.cookiejar, json, re, sys, time, urllib.parse, urllib.request, html

APP = "http://127.0.0.1:8090/Sindicato"
BUZON = "http://127.0.0.1:8026"


def seguro(url):
    if not (url.startswith("http://127.0.0.1:8090/") or url.startswith("http://127.0.0.1:8026/")):
        sys.exit("BLOQUEADO: URL fuera de la copia aislada: " + url)
    return url


class Navegador:
    def __init__(self):
        self.op = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))

    def get(self, url):
        with self.op.open(seguro(url), timeout=120) as r:
            return r.status, r.geturl(), r.read().decode("utf-8", "replace")

    def ajax(self, pagina, html_pagina, boton, campos, form):
        datos = {"javax.faces.partial.ajax": "true", "javax.faces.source": boton,
                 "javax.faces.partial.execute": "@all", "javax.faces.partial.render": form,
                 boton: boton, form: form, "javax.faces.ViewState": viewstate(html_pagina)}
        datos.update(campos)
        req = urllib.request.Request(seguro(pagina), data=urllib.parse.urlencode(datos).encode(),
                                     headers={"Faces-Request": "partial/ajax",
                                              "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"})
        t = time.time()
        with self.op.open(req, timeout=600) as r:
            return r.read().decode("utf-8", "replace"), time.time() - t


def viewstate(h):
    m = re.search(r'name="javax\.faces\.ViewState"[^>]*value="([^"]+)"', h) or re.search(r'value="([^"]+)"[^>]*name="javax\.faces\.ViewState"', h)
    return html.unescape(m.group(1))


def inputs(h, form):
    """Nombres de inputs (texto/password) del formulario, en orden de aparición."""
    bloque = h[h.find('id="%s"' % form):]
    return re.findall(r'<input[^>]*name="(%s:[^"]+)"[^>]*type="(?:text|password)"' % re.escape(form), bloque) + \
        re.findall(r'<input[^>]*type="(?:text|password)"[^>]*name="(%s:[^"]+)"' % re.escape(form), bloque)


def boton(h, form, texto):
    for m in re.finditer(r'<button[^>]*id="(%s:[^"]+)"[^>]*>(.*?)</button>' % re.escape(form), h, re.S):
        if texto in m.group(2):
            return m.group(1)
    sys.exit("no se encontró el botón '%s' en %s" % (texto, form))


def mensajes(xml):
    return [html.unescape(x) for x in re.findall(r'ui-messages-\w+-summary">([^<]*)<', xml)] or \
           [html.unescape(x) for x in re.findall(r'"summary":"([^"]*)"', xml)]


def login(nav, clave, pw):
    _, _, h = nav.get(APP + "/login.xhtml")
    nombres = sorted(set(inputs(h, "frmLogin")), key=h.find)
    xml, t = nav.ajax(APP + "/login.xhtml", h, boton(h, "frmLogin", "Entrar"), {nombres[0]: clave, nombres[1]: pw}, "frmLogin")
    red = re.search(r'<redirect url="([^"]+)"', xml)
    return (red.group(1) if red else None), mensajes(xml), t


def main():
    clave, pw, empresa, correo, catorcena = sys.argv[1:6]
    res = []
    nav = Navegador()

    # 1. login de administrador
    red, msj, t = login(nav, clave, pw)
    res.append("login admin: redirige a %s (%.1f s) %s" % (red, t, msj))
    st, url, h = nav.get("http://127.0.0.1:8090" + red if red and red.startswith("/") else APP + "/dashboard.xhtml")
    res.append("dashboard: HTTP %s, %d bytes, error en página: %s" % (st, len(h), bool(re.search(r"Exception|HTTP Status 500", h))))

    # 2. pantalla de cambios de empresa: dos clics seguidos
    pag = APP + "/navegacion/cargapagosaportaciones/cambios-empresa.xhtml"
    for intento in (1, 2):
        st, url, h = nav.get(pag)
        b = boton(h, "frmAltasCambios", "Aplicar altas y cambios de empresa")
        xml, t = nav.ajax(pag, h, b, {"frmAltasCambios:fechaCatorc_input": catorcena}, "frmAltasCambios")
        res.append("aplicar cambios, clic %d: %.1f s, mensajes %s" % (intento, t, mensajes(xml)))
        # Justo después, otra página que consulta la base: si el pool se agotara, aquí se colgaría.
        t0 = time.time()
        st, url, h2 = nav.get(APP + "/dashboard.xhtml")
        res.append("  dashboard inmediatamente después: HTTP %s en %.1f s" % (st, time.time() - t0))

    # 3. recuperación de contraseña (sesión nueva)
    nav2 = Navegador()
    pag = APP + "/recuperar-password.xhtml"
    st, url, h = nav2.get(pag)
    try:
        urllib.request.urlopen(urllib.request.Request(seguro(BUZON + "/api/v1/messages"), method="DELETE"), timeout=30)
    except Exception:
        pass
    sel = re.search(r'<select[^>]*name="(frmRecuperaPassword:empresa_input)"', h).group(1)
    xml, t = nav2.ajax(pag, h, boton(h, "frmRecuperaPassword", "Enviar correo"),
                       {"frmRecuperaPassword:claveEmpleado": clave, sel: empresa, "frmRecuperaPassword:correo": correo}, "frmRecuperaPassword")
    res.append("recuperar password: %.1f s, mensajes %s" % (t, mensajes(xml)))
    time.sleep(2)
    with urllib.request.urlopen(seguro(BUZON + "/api/v1/messages"), timeout=30) as r:
        lista = json.load(r)
    res.append("correos en el buzón falso: %d" % lista.get("total", 0))
    if not lista.get("messages"):
        print("\n".join(res)); sys.exit("no llegó el correo de recuperación")
    m = lista["messages"][0]
    with urllib.request.urlopen(seguro(BUZON + "/api/v1/message/" + m["ID"]), timeout=30) as r:
        cuerpo = json.load(r)
    para = [x["Address"] for x in m.get("To", [])]
    texto = (cuerpo.get("HTML") or "") + (cuerpo.get("Text") or "")
    liga = html.unescape(re.search(r'(https?://[^"\'<>\s]+restablecer-password\.xhtml\?token=[^"\'<>\s]+)', texto).group(1))
    res.append("correo para %s, asunto '%s', liga a %s" % (para, m.get("Subject"), liga.split("?")[0]))

    # 4. restablecer con la liga del correo (se fuerza el host de la copia aislada)
    liga_local = APP + "/restablecer-password.xhtml?" + liga.split("?", 1)[1]
    st, url, h = nav2.get(liga_local)
    nuevo = "NuevaWar2026"
    pag = url
    xml, t = nav2.ajax(pag, h, boton(h, "frmRestablecePassword", "Actualizar password"),
                       {"frmRestablecePassword:nuevoPassword": nuevo, "frmRestablecePassword:confirmaPassword": nuevo}, "frmRestablecePassword")
    res.append("restablecer password: %.1f s, mensajes %s" % (t, mensajes(xml)))

    # 5. login con la contraseña nueva y con la anterior
    red, msj, t = login(Navegador(), clave, nuevo)
    res.append("login con password nuevo: redirige a %s %s" % (red, msj))
    red, msj, t = login(Navegador(), clave, pw)
    res.append("login con password anterior: redirige a %s %s" % (red, msj))
    print("\n".join(res))


main()

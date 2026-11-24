"""Recorre TODAS las páginas .xhtml del sistema en la copia AISLADA (127.0.0.1:8090) con cuatro identidades
(sin sesión, socio, auxiliar, administrador) y guarda, por página, la respuesta sin seguir redirecciones:
código HTTP, destino de la redirección y una firma de la estructura (título, formularios y botones).
Sirve para comparar dos WAR y ver exactamente qué cambia en el acceso.
Uso: python3 -I recorre_paginas.py <webapp_dir> <salida.json>
"""
import http.cookiejar, json, os, re, sys, time, urllib.error, urllib.parse, urllib.request

APP = "http://127.0.0.1:8090/Sindicato"
IDENTIDADES = {"anonimo": None, "socio": ("990902", "SocioPrueba1"),
               "auxiliar": ("990903", "AuxPrueba1"), "admin": ("990901", "NuevaWar2026")}


class SinRedireccion(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *a, **k):
        return None


def abre(jar, seguir):
    h = [urllib.request.HTTPCookieProcessor(jar)] + ([] if seguir else [SinRedireccion()])
    return urllib.request.build_opener(*h)


def login(jar, clave, pw):
    op = abre(jar, True)
    h = op.open(APP + "/login.xhtml", timeout=60).read().decode("utf-8", "replace")
    vs = re.search(r'name="javax\.faces\.ViewState"[^>]*value="([^"]+)"', h).group(1)
    campos = sorted(set(re.findall(r'<input[^>]*name="(frmLogin:[^"]+)"[^>]*type="(?:text|password)"', h) +
                        re.findall(r'<input[^>]*type="(?:text|password)"[^>]*name="(frmLogin:[^"]+)"', h)), key=h.find)
    btn = re.search(r'<button[^>]*id="(frmLogin:[^"]+)"[^>]*>.*?Entrar', h, re.S).group(1)
    datos = {"javax.faces.partial.ajax": "true", "javax.faces.source": btn, "javax.faces.partial.execute": "@all",
             "javax.faces.partial.render": "frmLogin", btn: btn, "frmLogin": "frmLogin", campos[0]: clave, campos[1]: pw,
             "javax.faces.ViewState": vs}
    r = op.open(urllib.request.Request(APP + "/login.xhtml", data=urllib.parse.urlencode(datos).encode(),
                headers={"Faces-Request": "partial/ajax"}), timeout=60).read().decode()
    if "dashboard.xhtml" not in r:
        sys.exit("no se pudo iniciar sesión con " + clave)


def firma(html):
    t = re.search(r"<title>(.*?)</title>", html, re.S)
    return {"titulo": (t.group(1).strip() if t else ""), "formularios": len(re.findall(r"<form ", html)),
            "botones": len(re.findall(r"<button ", html))}


def main():
    webapp, salida = sys.argv[1], sys.argv[2]
    paginas = []
    for raiz, dirs, archivos in os.walk(webapp):
        rel = os.path.relpath(raiz, webapp)
        if rel.startswith("WEB-INF") or rel.startswith("resources") or rel.startswith("META-INF"):
            continue
        for a in archivos:
            if a.endswith(".xhtml"):
                paginas.append(("/" + os.path.join(rel, a)).replace("/./", "/"))
    paginas.sort()
    res = {}
    for ident, cred in IDENTIDADES.items():
        jar = http.cookiejar.CookieJar()
        if cred:
            login(jar, *cred)
        op = abre(jar, False)
        t0 = time.time()
        for p in paginas:
            try:
                r = op.open(APP + p, timeout=180)
                cuerpo = r.read().decode("utf-8", "replace")
                res.setdefault(p, {})[ident] = dict(estado=r.status, destino=None, **firma(cuerpo))
            except urllib.error.HTTPError as e:
                res.setdefault(p, {})[ident] = {"estado": e.code, "destino": e.headers.get("Location")}
            except Exception as e:
                res.setdefault(p, {})[ident] = {"estado": "ERROR", "destino": str(e)[:120]}
        print("%-9s %d páginas en %.0f s" % (ident, len(paginas), time.time() - t0))
    json.dump(res, open(salida, "w"), indent=1, ensure_ascii=False)


main()

"""Prueba con un navegador real (Chromium headless) sobre la copia AISLADA (127.0.0.1:8090).
  carga   <archivo.xlsx>  : el administrador sube un archivo de aportaciones en "Carga de archivos".
  socio                   : el socio entra, abre sus pantallas e intenta abrir una administrativa.
Solo navega en 127.0.0.1; cualquier otra petición se bloquea.
Uso: <venv>/bin/python prueba_navegador.py carga <archivo.xlsx> | socio
"""
import sys
from playwright.sync_api import sync_playwright

APP = "http://127.0.0.1:8090/Sindicato"


def solo_local(route):
    if route.request.url.startswith("http://127.0.0.1:8090/"):
        route.continue_()
    else:
        route.abort()


def entrar(page, clave, pw):
    page.goto(APP + "/login.xhtml")
    page.fill("input[placeholder='Clave empleado']", clave)
    page.fill("input[placeholder='Contraseña']", pw)
    page.click("button:has-text('Entrar')")
    page.wait_for_url("**/dashboard.xhtml", timeout=30000)


def carga(page, ruta):
    entrar(page, "990901", "NuevaWar2026")
    page.goto(APP + "/navegacion/cargapagosaportaciones/carga-archivo.xhtml")
    page.click("button:has-text('Cargar archivo')")
    page.wait_for_selector("label:has-text('Aportaciones')")
    page.click("label:has-text('Aportaciones')")
    # al elegir el tipo, la pantalla vuelve a dibujar el componente de carga ya habilitado
    page.wait_for_selector(".ui-fileupload input[type=file]:not([disabled])", state="attached", timeout=30000)
    page.wait_for_load_state("networkidle")
    page.set_input_files(".ui-fileupload input[type=file]", ruta)
    page.wait_for_selector(".ui-messages-info-summary, .ui-messages-error-summary, .ui-messages-warn-summary", timeout=120000)
    print("mensajes en pantalla:", [m.inner_text() for m in page.query_selector_all(".ui-messages-info-summary, .ui-messages-error-summary, .ui-messages-warn-summary")])


def socio(page):
    entrar(page, "990902", "SocioPrueba1")
    for p in ("mis-ahorros", "mis-creditos", "mis-solicitudes", "solicitud-credito", "mi-perfil"):
        r = page.goto(APP + "/navegacion/usuariocomun/%s.xhtml" % p)
        print("socio abre %-18s HTTP %s | queda en %s" % (p, r.status, page.url.replace(APP, "")))
    page.goto(APP + "/navegacion/administracion/panel.xhtml")
    print("socio intenta el panel de administración | queda en", page.url.replace(APP, ""))


with sync_playwright() as p:
    nav = p.chromium.launch(headless=True)
    ctx = nav.new_context()
    ctx.route("**/*", solo_local)
    page = ctx.new_page()
    errores = []
    page.on("pageerror", lambda e: errores.append(str(e)[:150]))
    if sys.argv[1] == "carga":
        carga(page, sys.argv[2])
    else:
        socio(page)
    print("errores de JavaScript en las páginas:", errores or "ninguno")
    nav.close()

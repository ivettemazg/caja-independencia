"""Escenarios de "guardar perfil" con navegador real sobre la copia AISLADA (127.0.0.1:8090):
  admin_edita_socio : el administrador busca un socio en el panel, guarda su perfil y luego abre pantallas administrativas.
  socio_propio      : el socio guarda su propio perfil (Mi Perfil) y sigue navegando en sus pantallas.
  admin_propio      : el administrador guarda su propio perfil (Mi Perfil) y luego abre pantallas administrativas.
Uso: <venv>/bin/python prueba_perfil.py <escenario> [clave_socio empresa password_socio]
"""
import sys
from playwright.sync_api import sync_playwright

APP = "http://127.0.0.1:8090/Sindicato"
ADMIN = ("990901", "NuevaWar2026")


def solo_local(route):
    route.continue_() if route.request.url.startswith("http://127.0.0.1:8090/") else route.abort()


def entrar(page, clave, pw):
    page.goto(APP + "/login.xhtml")
    page.fill("input[placeholder='Clave empleado']", clave)
    page.fill("input[placeholder='Contraseña']", pw)
    page.click("button:has-text('Entrar')")
    page.wait_for_url("**/dashboard.xhtml", timeout=30000)


def mensajes(page):
    return [m.inner_text().strip() for m in page.query_selector_all(
        ".ui-messages-info-summary, .ui-messages-error-summary, .ui-messages-warn-summary, .ui-growl-title")]


def actualizar(page):
    page.click("button:has-text('Actualizar')")
    page.wait_for_load_state("networkidle")
    page.wait_for_timeout(800)
    return mensajes(page)


def destino(page, ruta):
    r = page.goto(APP + ruta)
    return "%s -> %s (HTTP %s)" % (ruta, page.url.replace(APP, ""), r.status if r else "?")


def admin_edita_socio(page, clave, empresa):
    entrar(page, *ADMIN)
    page.goto(APP + "/navegacion/administracion/panel.xhtml")
    page.click(".ui-tabs-nav li:has-text('Empleados')")
    frm = page.locator("[id='adminTabs:frmBusquedaEmp']")
    frm.locator("input.ui-inputtext:visible").first.fill(clave)
    frm.locator(".ui-selectonemenu").first.click()
    page.locator(".ui-selectonemenu-panel:visible li[data-label='%s']" % empresa).first.click()
    frm.locator("button:has-text('Buscar')").click()
    page.wait_for_selector("button:has-text('Actualizar')", timeout=30000)
    print("  guardar perfil del socio desde el panel:", actualizar(page))
    print("  luego:", destino(page, "/navegacion/cargapagosaportaciones/cambios-empresa.xhtml"))
    print("  luego:", destino(page, "/navegacion/administracion/panel.xhtml"))


def socio_propio(page, clave, pw):
    entrar(page, clave, pw)
    page.goto(APP + "/navegacion/usuariocomun/mi-perfil.xhtml")
    print("  guardar Mi Perfil:", actualizar(page))
    print("  luego:", destino(page, "/navegacion/usuariocomun/mis-ahorros.xhtml"))
    print("  luego:", destino(page, "/dashboard.xhtml"))


def admin_propio(page):
    entrar(page, *ADMIN)
    page.goto(APP + "/navegacion/usuariocomun/mi-perfil.xhtml")
    print("  guardar Mi Perfil del administrador:", actualizar(page))
    print("  luego:", destino(page, "/navegacion/cargapagosaportaciones/cambios-empresa.xhtml"))


with sync_playwright() as p:
    nav = p.chromium.launch(headless=True)
    ctx = nav.new_context()
    ctx.route("**/*", solo_local)
    page = ctx.new_page()
    errores = []
    page.on("pageerror", lambda e: errores.append(str(e)[:150]))
    esc = sys.argv[1]
    print("==", esc)
    if esc == "admin_edita_socio":
        admin_edita_socio(page, sys.argv[2], sys.argv[3])
    elif esc == "socio_propio":
        socio_propio(page, sys.argv[2], sys.argv[4])
    else:
        admin_propio(page)
    print("  errores de JavaScript:", errores or "ninguno")
    nav.close()

"""Sube un archivo de aportaciones por la pantalla "Carga de archivos" de la copia AISLADA, como el navegador:
"Cargar archivo" -> tipo Aportaciones -> archivo (multipart). Uso: python3 -I prueba_carga_http.py <ruta.xlsx>"""
import os, re, sys, uuid, urllib.parse, urllib.request
src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "prueba_war_http.py"), encoding="utf-8").read().replace("\nmain()\n", "\n")
exec(src)
ruta = sys.argv[1]; nombre = os.path.basename(ruta); contenido = open(ruta, "rb").read()
nav = Navegador(); login(nav, "990901", "NuevaWar2026")
pag = APP + "/navegacion/cargapagosaportaciones/carga-archivo.xhtml"
st, url, h = nav.get(pag); vs = viewstate(h); F = "cargaArchivosFrm"
def boton_por_texto(doc, texto):
    for m in re.finditer(r'<button id="([^"]+)"[^>]*>(.*?)</button>', doc, re.S):
        if ('>%s<' % texto) in m.group(2): return m.group(1)
def ajax(source, extra=None, execute="@all", evento=None):
    d = {"javax.faces.partial.ajax": "true", "javax.faces.source": source, "javax.faces.partial.execute": execute,
         "javax.faces.partial.render": F, F: F, "javax.faces.ViewState": vs}
    if evento: d.update({"javax.faces.behavior.event": evento, "javax.faces.partial.event": evento})
    else: d[source] = source
    d.update(extra or {})
    req = urllib.request.Request(seguro(pag), data=urllib.parse.urlencode(d).encode(), headers={"Faces-Request": "partial/ajax"})
    with nav.op.open(req, timeout=120) as r: return r.read().decode()
x = ajax(boton_por_texto(h, "Cargar archivo")); print("1. 'Cargar archivo':", "panel abierto" if "sorPagApo" in x else x[:200])
x = ajax(F + ":sorPagApo", {F + ":sorPagApo": "2"}, execute=F + ":sorPagApo", evento="change"); print("2. tipo = Aportaciones:", "ok" if "<error>" not in x else x[:300])
fu = re.search(r'<div id="([^"]+)" class="ui-fileupload', x).group(1)
limite = "----Prueba" + uuid.uuid4().hex; partes = []
for k, v in {"javax.faces.partial.ajax": "true", "javax.faces.partial.execute": fu, "javax.faces.source": fu,
             "javax.faces.partial.render": "messages", F: F, "javax.faces.ViewState": vs}.items():
    partes.append(('--%s\r\nContent-Disposition: form-data; name="%s"\r\n\r\n%s\r\n' % (limite, k, v)).encode())
partes.append(('--%s\r\nContent-Disposition: form-data; name="%s"; filename="%s"\r\nContent-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n' % (limite, fu, nombre)).encode() + contenido + b"\r\n")
partes.append(("--%s--\r\n" % limite).encode())
req = urllib.request.Request(seguro(pag), data=b"".join(partes), headers={"Faces-Request": "partial/ajax", "Content-Type": "multipart/form-data; boundary=" + limite})
with nav.op.open(req, timeout=300) as r: x = r.read().decode()
red = re.search(r'<redirect url="([^"]+)"', x)
print("3. subir el archivo:", ("REDIRECCIÓN " + red.group(1)) if red else (mensajes(x) or ("respuesta sin mensajes: " + x[:400])))

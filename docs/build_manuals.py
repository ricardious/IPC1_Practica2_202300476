"""Regenerate the two Spanish UDrive manuals from the current source code.

Run ``python3 docs/build_manuals.py`` with ReportLab and DejaVu fonts installed.
"""

from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    Flowable, Image, KeepTogether, PageBreak, Paragraph, Preformatted,
    SimpleDocTemplate, Spacer, Table, TableStyle,
)


ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
FONT_DIR = Path("/usr/share/fonts/dejavu-sans-fonts")
pdfmetrics.registerFont(TTFont("DejaVu", str(FONT_DIR / "DejaVuSans.ttf")))
pdfmetrics.registerFont(TTFont("DejaVu-Bold", str(FONT_DIR / "DejaVuSans-Bold.ttf")))
MONO_FONT = next(path for path in [
    FONT_DIR / "DejaVuSansMono.ttf",
    Path.home() / ".fonts/TTF/DejaVuSansMono.ttf",
] if path.exists())
pdfmetrics.registerFont(TTFont("DejaVu-Mono", str(MONO_FONT)))

NAVY = colors.HexColor("#1D3448")
BLUE = colors.HexColor("#12618D")
CYAN = colors.HexColor("#0BAED5")
PALE = colors.HexColor("#EAF4F8")
LIGHT = colors.HexColor("#F6F9FB")
LINE = colors.HexColor("#B9CDD8")
GREEN = colors.HexColor("#E8F4E9")
RED = colors.HexColor("#FDEEEE")

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="Cover", fontName="DejaVu-Bold", fontSize=24,
                          leading=30, textColor=NAVY, spaceAfter=12))
styles.add(ParagraphStyle(name="Deck", fontName="DejaVu", fontSize=11,
                          leading=17, textColor=BLUE, spaceAfter=16))
styles.add(ParagraphStyle(name="H1U", fontName="DejaVu-Bold", fontSize=13,
                          leading=19, textColor=BLUE, spaceBefore=8, spaceAfter=9))
styles.add(ParagraphStyle(name="H2U", fontName="DejaVu-Bold", fontSize=10.2,
                          leading=14, textColor=NAVY, spaceBefore=10, spaceAfter=5))
styles.add(ParagraphStyle(name="BodyU", fontName="DejaVu", fontSize=8.8,
                          leading=14.3, textColor=NAVY, spaceAfter=8))
styles.add(ParagraphStyle(name="SmallU", fontName="DejaVu", fontSize=8.2,
                          leading=12.0, textColor=NAVY, spaceAfter=3))
styles.add(ParagraphStyle(name="CaptionU", fontName="DejaVu", fontSize=7.6,
                          leading=11, textColor=BLUE, alignment=TA_CENTER,
                          spaceBefore=3, spaceAfter=8))
styles.add(ParagraphStyle(name="CodeU", fontName="DejaVu-Mono", fontSize=7.8,
                          leading=12.5, textColor=NAVY))


def p(text, style="BodyU"):
    return Paragraph(text, styles[style])


def heading(text):
    return p(text, "H1U")


def subheading(text):
    return p(text, "H2U")


def bullet(text):
    return p("• " + text)


def table(headers, rows, widths, compact=False):
    cell_style = "SmallU" if compact else "BodyU"
    content = [[p(value, "SmallU") for value in headers]]
    content.extend([[p(str(value), cell_style) for value in row] for row in rows])
    result = Table(content, colWidths=widths, repeatRows=1, hAlign="LEFT")
    result.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), PALE),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, LIGHT]),
        ("GRID", (0, 0), (-1, -1), 0.4, LINE),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 7),
        ("RIGHTPADDING", (0, 0), (-1, -1), 7),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
    ]))
    return result


def callout(label, text):
    result = Table([[p(label, "H2U"), p(text, "BodyU")]], colWidths=[2.9 * cm, 14.4 * cm])
    result.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), PALE),
        ("BOX", (0, 0), (-1, -1), 0.6, CYAN),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("LEFTPADDING", (0, 0), (-1, -1), 9),
        ("RIGHTPADDING", (0, 0), (-1, -1), 9),
        ("TOPPADDING", (0, 0), (-1, -1), 9),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 9),
    ]))
    return result


def code(lines):
    result = Table([[Preformatted(lines, styles["CodeU"])]], colWidths=[17.3 * cm])
    result.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), LIGHT),
        ("LINEBEFORE", (0, 0), (0, -1), 2, CYAN),
        ("LEFTPADDING", (0, 0), (-1, -1), 10),
        ("TOPPADDING", (0, 0), (-1, -1), 8),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
    ]))
    return result


def screenshot(filename, caption, width=15.0 * cm):
    image = Image(str(DOCS / "screenshots" / filename), width=width,
                  height=width * 0.7)
    return KeepTogether([image, p(caption, "CaptionU")])


class FlowChart(Flowable):
    """Compact vertical flow with optional error/alternate branches."""

    def __init__(self, rows, loop_from=None, loop_to=None):
        super().__init__()
        self.rows = rows
        self.loop_from = loop_from
        self.loop_to = loop_to
        self.width = 490
        self.height = len(rows) * 55 + 9

    def draw(self):
        canvas = self.canv
        center_x, center_w = 50, 285
        branch_x, branch_w = 355, 130
        for index, (label, branch) in enumerate(self.rows):
            y = self.height - 48 - index * 55
            canvas.setFillColor(PALE if index < len(self.rows) - 1 else GREEN)
            canvas.setStrokeColor(BLUE)
            canvas.roundRect(center_x, y, center_w, 40, 6, fill=1, stroke=1)
            self._center(label, center_x + center_w / 2, y + 23)
            if index < len(self.rows) - 1:
                self._arrow(center_x + center_w / 2, y, center_x + center_w / 2, y - 15)
            if branch:
                canvas.setFillColor(RED)
                canvas.setStrokeColor(colors.HexColor("#B85C5C"))
                branch_text, branch_label = branch
                canvas.roundRect(branch_x, y, branch_w, 40, 6, fill=1, stroke=1)
                self._center(branch_text, branch_x + branch_w / 2, y + 23)
                self._arrow(center_x + center_w, y + 20, branch_x, y + 20)
                canvas.setFont("DejaVu", 6.8)
                canvas.setFillColor(NAVY)
                canvas.drawCentredString(345, y + 27, branch_label)
        if self.loop_from is not None:
            from_y = self.height - 48 - self.loop_from * 55 + 20
            to_y = self.height - 48 - self.loop_to * 55 + 20
            canvas.setStrokeColor(BLUE)
            canvas.line(50, from_y, 23, from_y)
            canvas.line(23, from_y, 23, to_y)
            self._arrow(23, to_y, 50, to_y)
            canvas.setFont("DejaVu", 6.8)
            canvas.setFillColor(NAVY)
            canvas.drawString(24, from_y + 5, "sí")

    def _center(self, text, x, y):
        self.canv.setFillColor(NAVY)
        self.canv.setFont("DejaVu", 8.1)
        lines = text.split("|")
        for index, line in enumerate(lines):
            self.canv.drawCentredString(x, y - index * 11, line)

    def _arrow(self, x1, y1, x2, y2):
        canvas = self.canv
        canvas.setStrokeColor(BLUE)
        canvas.setFillColor(BLUE)
        canvas.line(x1, y1, x2, y2)
        if abs(x2 - x1) > abs(y2 - y1):
            canvas.line(x2 - 4, y2 + 3, x2, y2)
            canvas.line(x2 - 4, y2 - 3, x2, y2)
        else:
            canvas.line(x2 - 3, y2 + 4, x2, y2)
            canvas.line(x2 + 3, y2 + 4, x2, y2)


class ClassDiagram(Flowable):
    def __init__(self):
        super().__init__()
        self.width, self.height = 490, 284

    def draw(self):
        c = self.canv
        c.setStrokeColor(LINE)
        for x1, y1, x2, y2 in [
            (245, 221, 245, 192), (327, 248, 365, 248),
            (160, 164, 130, 164), (327, 164, 360, 164),
            (245, 136, 245, 106), (65, 136, 65, 106),
            (164, 78, 130, 78),
        ]:
            c.line(x1, y1, x2, y2)
        self._node(160, 221, 168, 55, "MainFrame", "Swing / formularios .form")
        self._node(365, 221, 120, 55, "Distance", "edita la ruta por ID")
        self._node(160, 136, 168, 56, "TripManager", "coordina transiciones")
        self._node(0, 136, 130, 56, "AppState", "listas e IDs")
        self._node(360, 136, 125, 56, "PersistenceService", "binario")
        self._node(160, 50, 168, 56, "Journey", "Thread / progreso")
        self._node(0, 50, 130, 56, "Route · Trip", "Vehicle · Driver")
        c.setFont("DejaVu", 7)
        c.setFillColor(BLUE)
        c.drawString(178, 199, "llama")
        c.drawString(211, 113, "lanza")

    def _node(self, x, y, width, height, name, detail):
        c = self.canv
        c.setFillColor(PALE)
        c.setStrokeColor(BLUE)
        c.roundRect(x, y, width, height, 5, fill=1, stroke=1)
        c.setFillColor(NAVY)
        c.setFont("DejaVu-Bold", 8.7)
        c.drawCentredString(x + width / 2, y + height - 20, name)
        c.setStrokeColor(LINE)
        c.line(x + 7, y + height - 27, x + width - 7, y + height - 27)
        c.setFont("DejaVu", 7.1)
        c.drawCentredString(x + width / 2, y + 11, detail)


def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(LINE)
    canvas.line(1.8 * cm, 1.55 * cm, 19.2 * cm, 1.55 * cm)
    canvas.setFont("DejaVu", 7)
    canvas.setFillColor(NAVY)
    canvas.drawString(1.8 * cm, 1.16 * cm, "UDrive · IPC1 Práctica 2 · 202300476")
    canvas.drawRightString(19.2 * cm, 1.16 * cm, str(doc.page))
    canvas.restoreState()


def build(filename, story):
    doc = SimpleDocTemplate(str(DOCS / filename), pagesize=(21 * cm, 29.7 * cm),
                            leftMargin=1.8 * cm, rightMargin=1.8 * cm,
                            topMargin=1.75 * cm, bottomMargin=1.85 * cm,
                            title=filename.removesuffix(".pdf"),
                            author="Alex Ricardo Castañeda Rodríguez")
    doc.build(story, onFirstPage=footer, onLaterPages=footer)


def user_manual():
    s = [
        p("UDRIVE · MANUAL DE USUARIO", "Cover"),
        p("Guía práctica de la aplicación terminada sobre la interfaz original de NetBeans", "Deck"),
        p("Alex Ricardo Castañeda Rodríguez · Carné 202300476 · Sección B"),
        callout("Propósito", "Cargar rutas, preparar hasta tres viajes al mismo tiempo, seguir su progreso, recargar combustible, decidir el retorno y consultar el historial guardado."),
        Spacer(1, 12),
        heading("1. Antes de empezar"),
        table(["Requisito", "Detalle"], [
            ["Sistema", "Windows, Linux o macOS con entorno gráfico; resolución recomendada de 1000 × 700 píxeles o mayor."],
            ["Java", "JDK 21 o posterior para compilar. Para ejecutar el JAR ya generado, Java 21 o posterior."],
            ["Archivos", "Conserve el repositorio y ejecute los comandos desde su carpeta raíz. El estado se guarda en data/udrive-state.bin."],
        ], [3.5 * cm, 13.8 * cm], compact=True),
        subheading("Inicio rápido"),
        code("./mvnw clean package       # Linux / macOS\njava -jar target/UDrive.jar"),
        p("En Windows, use <b>mvnw.cmd clean package</b> antes del mismo comando <b>java -jar</b>. El menú lateral permite cambiar entre Load routes, Generate trip, Trip start y Trip history; Logout cierra la aplicación guardando el estado."),
        subheading("Recorrido recomendado"),
        p("1. Cargue un CSV → 2. Genere un viaje → 3. Inícielo → 4. Recargue si hace falta → 5. Regrese o finalice en destino → 6. Consulte el historial."),
        subheading("Qué encontrará en el menú"),
        table(["Pantalla", "Acción principal"], [
            ["Load routes", "Importar CSV y corregir la distancia de una ruta por ID."],
            ["Generate trip", "Elegir origen, destino y vehículo libre."],
            ["Trip start", "Iniciar, recargar, regresar o terminar hasta tres viajes."],
            ["Trip history", "Revisar los viajes terminados y su consumo."],
        ], [4.1 * cm, 13.2 * cm], compact=True),
        PageBreak(),
        heading("2. Cargar y corregir rutas"),
        p("Abra <b>Load routes</b> y pulse <b>Load Routes (.csv)</b>. Seleccione el archivo en el explorador. La tabla mostrará ID, Start, End y Distance. Al terminar, un mensaje indica cuántas rutas se agregaron y cuántas duplicadas se omitieron."),
        subheading("Formato del archivo"),
        code("Inicio,Fin,Distancia\nCiudad de Guatemala,Antigua Guatemala,45\nAntigua Guatemala,Escuintla,68"),
        bullet("El encabezado es opcional. Cada fila debe tener exactamente tres columnas y una distancia entera mayor que cero."),
        bullet("El origen y el destino no pueden quedar vacíos ni ser iguales. La misma ruta puede recorrerse en ambos sentidos."),
        bullet("Si una fila es inválida, se muestra el número de línea. Corrija el CSV y vuelva a cargarlo."),
        p("<b>Consejo:</b> el archivo <b>examples/rutas.csv</b> sirve para probar el flujo completo. Una ruta ya cargada se reconoce por sus dos extremos, incluso si están en orden inverso; la comparación de nombres respeta mayúsculas y minúsculas."),
        subheading("Editar una distancia"),
        p("Pulse <b>Edit Distance</b>, escriba el ID de la tabla y la nueva distancia en kilómetros, y pulse <b>Accept</b>. El ID debe existir y la distancia debe ser un entero positivo. <b>Cancel</b> cierra el diálogo sin cambiar datos."),
        screenshot("01-routes.png", "Figura 1. Tabla de rutas de la interfaz original."),
        PageBreak(),
        heading("3. Preparar un viaje"),
        p("En <b>Generate trip</b>, elija un punto inicial, un punto final diferente y un tipo de transporte en los tres desplegables. Sus ubicaciones provienen de las rutas cargadas; no se escriben manualmente."),
        p("Pulse <b>Generate trip</b>. Se asigna el primer piloto libre y el vehículo seleccionado queda reservado. La aplicación abre la pantalla <b>Trip start</b> con el viaje preparado."),
        p("Antes de iniciar puede preparar más viajes, hasta ocupar los tres pilotos. El viaje preparado ya reserva sus recursos, aunque todavía no avance por la pista."),
        table(["Límite", "Qué verá"], [
            ["3 pilotos", "Solo pueden existir tres viajes activos a la vez. Si no hay pilotos libres, se deshabilita Generate trip y aparece un aviso rojo."],
            ["9 vehículos", "Tres motocicletas, tres vehículos estándar y tres premium. Los que están ocupados dejan de estar disponibles."],
            ["Ruta válida", "El origen y destino deben corresponder a una ruta cargada; puede elegirse el sentido inverso."],
        ], [3.4 * cm, 13.9 * cm], compact=True),
        screenshot("02-generate-trip.png", "Figura 2. Selección de origen, destino y vehículo."),
        PageBreak(),
        heading("4. Iniciar, seguir y recargar"),
        p("En <b>Trip start</b>, pulse <b>Start</b> junto a un viaje preparado o <b>Start all</b> para iniciar todos los preparados. El icono avanza por la pista y se actualizan distancia y combustible. Los viajes pueden avanzar a la vez."),
        p("La línea de información de cada pista muestra el número de viaje, vehículo, estado, distancia recorrida en la etapa actual y galones disponibles. Si la etapa se detiene, la posición no se pierde."),
        table(["Control / estado", "Significado"], [
            ["Start", "Inicia solo el viaje preparado de esa fila."],
            ["Start all", "Inicia todos los viajes que todavía están preparados."],
            ["Sin combustible", "El viaje se pausa cuando el tanque no alcanza para el siguiente tramo."],
            ["Refuel", "Llena el tanque del vehículo pausado y permite que continúe la misma etapa."],
        ], [4.0 * cm, 13.3 * cm], compact=True),
        subheading("Consumo por kilómetro"),
        table(["Vehículo", "Consumo", "Tanque lleno"], [
            ["Motocicleta", "0.10 gal/km", "6 gal"],
            ["Estándar", "0.30 gal/km", "10 gal"],
            ["Premium", "0.45 gal/km", "12 gal"],
        ], [6.0 * cm, 5.5 * cm, 5.8 * cm], compact=True),
        screenshot("04-trip-running.png", "Figura 3. Pista, progreso y combustible de un viaje en curso."),
        PageBreak(),
        heading("5. Llegada, retorno e historial"),
        p("Al llegar al destino, el viaje espera una decisión. Pulse <b>Return</b> para iniciar manualmente el regreso o <b>Finish here</b> para terminar en el destino. En el regreso el vehículo aparece orientado hacia la izquierda. Si vuelve a faltar combustible, use <b>Refuel</b>."),
        p("Al completar el viaje se liberan piloto y vehículo. Abra <b>Trip history</b> para ver: ID de ruta, fecha/hora de inicio y fin, kilómetros de la ruta, vehículo, kilómetros realmente recorridos y galones consumidos. Un viaje con retorno recorre aproximadamente el doble de su distancia de ruta."),
        p("<b>Ejemplo:</b> una ruta de 45 km finalizada en destino registra 45 km de trayectoria; si vuelve al origen, registra 90 km. El historial solo incluye viajes completados."),
        screenshot("05-trip-history.png", "Figura 4. Historial de viajes completados."),
        subheading("Guardado y solución de problemas"),
        table(["Situación", "Qué hacer"], [
            ["Al reiniciar no veo datos", "Ejecute siempre desde la misma carpeta: data/udrive-state.bin es relativo al directorio de trabajo."],
            ["CSV rechazado", "Revise la línea indicada: tres columnas, extremos diferentes y distancia entera positiva."],
            ["No puedo generar viaje", "Compruebe la ruta, el vehículo disponible y que exista un piloto libre."],
            ["Archivo de estado dañado", "Haga una copia del archivo antes de cambiarlo; la aplicación muestra una advertencia y comienza una sesión nueva."],
        ], [4.4 * cm, 12.9 * cm], compact=True),
    ]
    build("Manual de Usuario.pdf", s)


def technical_manual():
    s = [
        p("UDRIVE · MANUAL TÉCNICO", "Cover"),
        p("Arquitectura, métodos principales, diagramas de flujo y mantenimiento", "Deck"),
        p("Alex Ricardo Castañeda Rodríguez · Carné 202300476 · Sección B"),
        callout("Alcance", "Este documento describe la aplicación principal en src/main, que conserva MainFrame.form y Distance.form de NetBeans e integra la lógica pendiente. legacy/2024 es una copia histórica, no la aplicación activa."),
        Spacer(1, 10),
        heading("Contenido"),
        table(["Sección", "Qué documenta"], [
            ["1. Solución y arquitectura", "Capas, diagrama de clases y responsabilidades."],
            ["2. Modelo y reglas", "Entidades, estados y consumo de combustible."],
            ["3. Rutas", "CSV, validación, importación y edición de distancia."],
            ["4. Preparación e inicio", "Asignación de piloto/vehículo y creación del hilo."],
            ["5. Avance y retorno", "Métodos de Journey, recarga y finalización."],
            ["6. Persistencia y UI", "Serialización, restauración y sincronización con Swing."],
            ["7. Compilación y pruebas", "Estructura, comandos y cobertura."],
        ], [5.0 * cm, 12.3 * cm], compact=True),
        subheading("Correspondencia con el enunciado"),
        p("El enunciado solicita un manual técnico con descripción general, diagrama de clases y diagramas de flujo de los métodos principales. Las secciones siguientes cumplen esos tres puntos y añaden contratos y casos de error tomados del código actual."),
        subheading("Puntos de entrada para revisar el código"),
        table(["Archivo", "Dónde comenzar"], [
            ["src/main/Main.java", "Carga el estado y muestra MainFrame."],
            ["src/main/TripManager.java", "Concentra las operaciones del ciclo de viaje."],
            ["src/main/Journey.java", "Implementa el avance concurrente y el combustible."],
            ["src/main/PersistenceService.java", "Lee y escribe el archivo serializado."],
        ], [6.2 * cm, 11.1 * cm], compact=True),
        PageBreak(),
        heading("1. Solución y arquitectura"),
        p("La interfaz Swing original presenta cuatro vistas dentro de MainFrame: rutas, generación, inicio y seguimiento, e historial. TripManager concentra las transiciones de negocio. AppState guarda los objetos; Journey ejecuta cada viaje en un hilo; CsvRouteImporter y PersistenceService gestionan entrada y salida."),
        subheading("Diagrama de clases y colaboraciones"),
        ClassDiagram(),
        p("Las líneas indican uso o propiedad entre componentes; no representan herencia. Journey sí hereda de Thread. Los formularios .form se conservan editables en NetBeans y se compilan con AbsoluteLayout."),
        table(["Capa", "Clases", "Responsabilidad"], [
            ["Presentación", "Main, MainFrame, Distance", "Arranque, eventos Swing, tablas y diálogo."],
            ["Aplicación", "TripManager, Journey", "Reglas de transición, hilos y callbacks."],
            ["Dominio", "AppState, Route, Trip, Driver, Vehicle, VehicleType, TripStatus", "Estado serializable y restricciones."],
            ["Entrada/salida", "CsvRouteImporter, PersistenceService", "CSV UTF-8 y archivo binario."],
        ], [3.2 * cm, 5.1 * cm, 9.0 * cm], compact=True),
        PageBreak(),
        heading("2. Modelo de datos y reglas de negocio"),
        table(["Clase", "Datos clave / contrato"], [
            ["Route", "id, start, end, distance. connects() acepta el mismo par de extremos en ambos sentidos."],
            ["Trip", "ID, ruta, origen/destino, vehículo/piloto, fechas, estado, progreso por etapa, distancia total y combustible total."],
            ["Vehicle", "Tipo, número de unidad, combustible restante y viaje asignado. consume() no permite saldo negativo."],
            ["Driver", "ID, nombre y viaje asignado; solo uno a la vez."],
            ["AppState", "Listas de entidades y contadores para nuevos IDs; inicia tres pilotos y nueve vehículos con tanque lleno."],
        ], [3.2 * cm, 14.1 * cm], compact=True),
        subheading("Máquina de estados de Trip"),
        code("PREPARED → OUTBOUND → WAITING_RETURN ──→ RETURNING → COMPLETED\n                         │                └── Finish here → COMPLETED\n                         └── OUT_OF_FUEL_OUTBOUND → Refuel → OUTBOUND\n                                          RETURNING → OUT_OF_FUEL_RETURN\n                                                      → Refuel → RETURNING"),
        p("<b>WAITING_RETURN</b> mantiene recursos reservados. <b>COMPLETED</b> libera piloto y vehículo. beginReturn() reinicia el progreso de la etapa, pero conserva la distancia total y el combustible acumulado."),
        subheading("Parámetros de combustible"),
        table(["Tipo", "gal/km", "Capacidad (gal)"], [
            ["Motocicleta", "0.10", "6"], ["Estándar", "0.30", "10"],
            ["Premium", "0.45", "12"],
        ], [6.1 * cm, 5.4 * cm, 5.8 * cm], compact=True),
        p("Por cada tramo, <b>consumo = distancia del paso × tasa del tipo</b>. Por ejemplo, 45 km en un vehículo estándar consumen 13.5 galones: se requerirá recarga antes de terminar con un tanque de 10 galones."),
        subheading("Invariantes que deben mantenerse"),
        table(["Regla", "Implementación"], [
            ["Identidad", "AppState incrementa nextRouteId y nextTripId; no reutiliza IDs al terminar viajes."],
            ["Exclusividad", "Un piloto y un vehículo tienen a lo sumo un assignedTripId activo."],
            ["Distancia", "Trip guarda una copia de routeDistanceKm al crearse; editar Route no cambia viajes ya creados."],
            ["Acumulados", "beginReturn() reinicia solo el progreso de etapa; totalDistanceKm y fuelConsumed continúan acumulando."],
        ], [3.5 * cm, 13.8 * cm], compact=True),
        PageBreak(),
        heading("3. Funciones principales: rutas"),
        table(["Método", "Entrada y resultado"], [
            ["CsvRouteImporter.read(Path)", "Lee UTF-8 y devuelve List&lt;RouteData&gt;. Lanza IOException con número de línea si el formato es inválido."],
            ["MainFrame.chooseCSVFile()", "Abre JFileChooser filtrado a .csv; entrega el archivo a importCsv()."],
            ["MainFrame.importCsv()", "Agrega rutas nuevas, omite duplicadas, guarda el estado y actualiza tabla/listas."],
            ["MainFrame.updateRouteDistance(id, km)", "Devuelve false si el ID no existe o km ≤ 0; de lo contrario edita, guarda y refresca."],
        ], [7.2 * cm, 10.1 * cm], compact=True),
        subheading("Diagrama de flujo: CsvRouteImporter.read + importCsv"),
        FlowChart([
            ("Elegir archivo .csv con JFileChooser", None),
            ("Leer filas UTF-8; omitir vacías|y encabezado opcional", None),
            ("Validar 3 columnas, extremos|distintos y km positivos", ("IOException|con línea", "no")),
            ("Consultar AppState.findRoute|en ambos sentidos", ("Duplicada:|omitir", "sí")),
            ("Agregar rutas, guardar y|actualizar controles", None),
        ]),
        p("La validación termina antes de agregar rutas porque read() devuelve la lista completa. El archivo de ejemplo está en <b>examples/rutas.csv</b>. La edición de distancia se hace por ID, no seleccionando una fila."),
        subheading("Casos límite de la importación"),
        table(["Entrada", "Respuesta"], [
            ["Fila vacía", "Se ignora."],
            ["Encabezado Inicio,Fin,Distancia", "Se omite solo si está en la primera línea; los nombres se comparan sin distinguir mayúsculas."],
            ["Ruta repetida", "No se agrega una segunda vez, incluso si los extremos vienen invertidos."],
            ["Archivo sin rutas válidas", "read() lanza IOException y la interfaz muestra un error."],
        ], [5.2 * cm, 12.1 * cm], compact=True),
        PageBreak(),
        heading("4. Funciones principales: preparar e iniciar"),
        table(["Método", "Precondición, efecto y salida"], [
            ["MainFrame.generateTripFromForm()", "Lee los desplegables, busca el vehículo disponible y solicita a TripManager la creación; muestra error o abre Trip start."],
            ["TripManager.createTrip(origin, destination, vehicle)", "Exige extremos distintos, ruta existente, vehículo libre y piloto libre. Crea Trip, guarda y notifica la UI."],
            ["AppState.createTrip(route, origin, destination, vehicle, driver)", "Genera ID, reserva ambos recursos y añade el viaje PREPARED a la lista."],
            ["TripManager.startTrip(trip)", "Solo desde PREPARED: marca hora de inicio, pasa a OUTBOUND y lanza Journey."],
            ["TripManager.startAll()", "Recorre los viajes y aplica startTrip a cada PREPARED."],
        ], [7.4 * cm, 9.9 * cm], compact=True),
        subheading("Diagrama de flujo: TripManager.createTrip"),
        FlowChart([
            ("Leer origen, destino y vehículo", None),
            ("¿Extremos diferentes y ruta|cargada en AppState?", ("Error de|selección", "no")),
            ("¿Vehículo disponible y|piloto libre?", ("No generar|viaje", "no")),
            ("Asignar ID, piloto y vehículo;|crear Trip PREPARED", None),
            ("Persistir y notificar|a MainFrame", None),
        ]),
        p("El límite efectivo de tres viajes activos proviene de los tres pilotos. El vehículo se reserva al preparar el viaje, no al pulsar Start."),
        subheading("Errores y postcondiciones"),
        p("createTrip() produce <b>IllegalArgumentException</b> si los extremos son iguales, falta la ruta o el vehículo no está libre; produce <b>IllegalStateException</b> cuando no hay piloto disponible. MainFrame captura esos errores y muestra un diálogo. Si la creación tiene éxito, el nuevo Trip está en PREPARED y el piloto y vehículo quedan reservados."),
        p("startTrip() no vuelve a arrancar un viaje que ya está en curso. startAll() aplica la misma operación a cada viaje preparado, por lo que el inicio individual y el conjunto comparten la misma regla de estado."),
        PageBreak(),
        heading("5. Funciones principales: avance, combustible y retorno"),
        table(["Método", "Comportamiento"], [
            ["Journey.run()", "Cada 150 ms intenta avanzar hasta 1 km; calcula consumo, actualiza Trip y avisa progreso."],
            ["Vehicle.consume(gallons)", "Devuelve false si el tanque no alcanza para el próximo paso; no descuenta combustible."],
            ["TripManager.refuel(trip)", "Solo en OUT_OF_FUEL_OUTBOUND/RETURN; llena el tanque y despierta al hilo con fuelAvailable()."],
            ["TripManager.startReturn(trip)", "Solo en WAITING_RETURN; reinicia progreso de etapa, cambia a RETURNING y lanza un Journey nuevo."],
            ["TripManager.finishAtDestination(trip)", "Si espera en destino, completa allí, sin recorrido de vuelta."],
            ["TripManager.complete(trip)", "Marca COMPLETED, libera recursos, elimina el hilo activo, guarda y notifica."],
        ], [7.0 * cm, 10.3 * cm], compact=True),
        subheading("Diagrama de flujo: Journey.run"),
        FlowChart([
            ("Calcular próximo paso y|galones necesarios", None),
            ("¿Vehicle.consume()|puede descontar?", ("Pausar y|esperar Refuel", "no")),
            ("Trip.advance(); notificar|progreso; dormir 150 ms", None),
            ("¿Queda distancia?|seguir si sí", None),
            ("Avisar llegada; esperar retorno|o completar", None),
        ], loop_from=3, loop_to=0),
        p("Si el tanque queda corto, el hilo espera en fuelMonitor. En ida pasa a OUT_OF_FUEL_OUTBOUND; en retorno a OUT_OF_FUEL_RETURN. La interfaz carga el GIF orientado al lado correspondiente."),
        subheading("Ejecución concurrente y callbacks"),
        p("Cada Journey es un hilo daemon identificado por viaje. Cuando avanza llama a <b>onProgress</b>; al faltar combustible a <b>onFuelEmpty</b>; al llegar a <b>onArrived</b>. TripManager traduce esos eventos en guardado, cambio de estado y actualización de la interfaz. El avance es de hasta 1 km por tick de 150 ms; es una simulación proporcional a la distancia, no tiempo real de carretera."),
        p("Ejemplo: una ruta de 2 km necesita dos pasos y consume 0.2 galones en motocicleta. Si se retorna, beginReturn() reinicia la etapa y la trayectoria total llega a 4 km; el consumo total será 0.4 galones si no hubo otras variaciones."),
        PageBreak(),
        heading("6. Persistencia y concurrencia"),
        table(["Método", "Responsabilidad"], [
            ["PersistenceService.load()", "Si el archivo no existe, crea AppState. Si existe, deserializa y valida que el objeto sea AppState."],
            ["PersistenceService.save(state)", "Escribe a .tmp y lo reemplaza con move atómico cuando está disponible; usa ObjectOutputStream."],
            ["TripManager.restoreRunningTrips()", "Al iniciar, relanza Journey para estados OUTBOUND, RETURNING y pausas de combustible."],
            ["TripManager.shutdown()", "Cancela hilos activos y guarda el estado final al cerrar."],
        ], [7.4 * cm, 9.9 * cm], compact=True),
        subheading("Diagrama de flujo: guardado y restauración"),
        FlowChart([
            ("Cambio de estado o avance|periódico del viaje", None),
            ("Serializar AppState en|data/udrive-state.bin.tmp", ("IOException|→ aviso UI", "error")),
            ("Reemplazar archivo binario|de forma atómica si es posible", None),
            ("En próximo arranque: deserializar|AppState o crear uno nuevo", ("Archivo inválido|→ advertir", "error")),
            ("Reiniciar Journey de viajes|que estaban en curso", None),
        ]),
        p("Los métodos que modifican el estado compartido están sincronizados en TripManager, AppState y entidades relevantes. runningJourneys es un ConcurrentHashMap por ID. Las notificaciones provenientes de Journey llegan a Swing mediante SwingUtilities.invokeLater para pintar en el hilo de eventos."),
        p("<b>Importante:</b> la ruta de datos es relativa al directorio desde el que se ejecuta Java. El CSV es solo entrada; el estado persistente solicitado por el enunciado es binario."),
        subheading("Cuándo se guarda"),
        table(["Evento", "Disparador"], [
            ["Rutas / preparación", "Tras importar, editar distancia o crear un viaje."],
            ["Transiciones", "Al iniciar, llegar, recargar, regresar, completar o cerrar."],
            ["Progreso", "Periódicamente durante Journey, aproximadamente cada 5 km recorridos en la etapa."],
            ["Error de escritura", "TripManager notifica a MainFrame; se muestra un diálogo y el estado en memoria continúa."],
        ], [4.2 * cm, 13.1 * cm], compact=True),
        PageBreak(),
        heading("7. Interfaz, compilación y verificación"),
        p("MainFrame.refreshRoutesTable() reconstruye la tabla de rutas; refreshSelections() actualiza orígenes, destinos, flota y pilotos disponibles; refreshTrips() sitúa los GIF y habilita Start, Refuel, Return y Finish here según TripStatus; refreshHistory() muestra solo viajes COMPLETED."),
        screenshot("03-trip-prepared.png", "Figura 1. Pantalla de viajes del formulario original; la lógica actual se conecta a estos controles.", width=12.7 * cm),
        subheading("Estructura y comandos"),
        code("src/main/        Java y formularios NetBeans\nsrc/vehicles/    imágenes y GIF de ida/retorno\ntest/main/       pruebas JUnit 5\ndocs/            enunciado, manuales y capturas\n./mvnw clean package\n./mvnw test\njava -jar target/UDrive.jar"),
        p("Maven usa Java 21, FlatLaf, NetBeans AbsoluteLayout y Shade para producir el JAR ejecutable. <b>target/</b> es generado y no debe subirse al repositorio. Si se modifica un formulario en NetBeans, mantenga sincronizados su <b>.java</b> y <b>.form</b>."),
        subheading("Pruebas automatizadas existentes"),
        table(["Clase de prueba", "Verifica"], [
            ["CsvRouteImporterTest", "Encabezado, filas incompletas y distancias no positivas."],
            ["AppStateTest", "Flota inicial, reserva/liberación y serialización."],
            ["JourneyTest", "Avance, consumo y pausa/reanudación por combustible."],
            ["TripManagerTest", "Completar en destino y retorno con recursos liberados."],
            ["VehicleIconTest", "GIF de retorno para las nueve unidades y tamaños de lienzo."],
        ], [5.3 * cm, 12.0 * cm], compact=True),
    ]
    build("Manual Técnico.pdf", s)


if __name__ == "__main__":
    user_manual()
    technical_manual()

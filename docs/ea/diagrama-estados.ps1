# Crea en Enterprise Architect el diagrama de estados de una sesión de estudio
# (máquina de estados "SesionEstudio") y lo exporta como PNG.
# Uso (desde la raíz del proyecto):  powershell -ExecutionPolicy Bypass -File docs\ea\diagrama-estados.ps1
# Se puede ejecutar varias veces: borra el paquete "Comportamiento" y lo vuelve a crear.

$ErrorActionPreference = "Stop"

# Rutas: el modelo y la imagen quedan junto a este script (docs/ea)
$carpeta = $PSScriptRoot
$modelo  = Join-Path $carpeta "AgendaEstudio.qea"
$imagen  = Join-Path $carpeta "estados.png"
$base    = "C:\Program Files\Sparx Systems\EA Trial\EABase.qea"

# ---------- Funciones de ayuda ----------

# Crea un elemento (estado, nodo inicial/final o nota) dentro de "padre"
function Nuevo-Elemento($padre, $nombre, $tipo, $subtipo = -1) {
    $e = $padre.Elements.AddNew($nombre, $tipo)
    if ($subtipo -ge 0) { $e.Subtype = $subtipo }   # 3 = inicial, 4 = final
    $e.Update() | Out-Null
    if ($nombre -eq "" -and $tipo -eq "StateNode") {
        $e.Name = " "        # sin esto EA muestra el texto "Pseudostate"
        $e.Update() | Out-Null
    }
    return $e
}

# Crea una transición de "origen" a "destino" con evento, guarda y efecto
function Nueva-Transicion($origen, $destino, $evento, $guarda = "", $efecto = "") {
    $c = $origen.Connectors.AddNew("", "StateFlow")
    $c.SupplierID = $destino.ElementID
    $c.TransitionEvent  = $evento
    $c.TransitionGuard  = $guarda
    $c.TransitionAction = $efecto
    $c.Update() | Out-Null
    return $c
}

# Pone un elemento en el diagrama: izquierda, arriba, ancho y alto en píxeles
function Poner($diagrama, $elemento, $x, $y, $ancho, $alto) {
    $pos = "l=$x;r=$($x + $ancho);t=$y;b=$($y + $alto);"
    $o = $diagrama.DiagramObjects.AddNew($pos, "")
    $o.ElementID = $elemento.ElementID
    $o.Update() | Out-Null
}

# Cambia por dónde sale y llega una línea (desplazamientos desde el centro)
# y dónde va su etiqueta (desplazamiento desde el medio de la línea)
function Ajustar-Linea($diagrama, $conector, $geometria, $quiebres = "") {
    $l = $diagrama.DiagramLinks.AddNew("", "")
    $l.ConnectorID = $conector.ConnectorID
    $l.Geometry = $geometria
    if ($quiebres -ne "") { $l.Path = $quiebres }   # puntos "x:y;" por donde dobla la línea
    $l.Update() | Out-Null
}

# ---------- 1. Abrir (o crear) el modelo ----------
if (-not (Test-Path $modelo)) {
    Copy-Item $base $modelo      # un .qea vacío a partir del modelo base de EA
}
$repo = New-Object -ComObject EA.Repository
try {
    if (-not $repo.OpenFile($modelo)) { throw "No se pudo abrir $modelo" }

    # Raíz del modelo
    if ($repo.Models.Count -eq 0) {
        $raiz = $repo.Models.AddNew("AgendaEstudio", "")
        $raiz.Update() | Out-Null
        $repo.Models.Refresh()
    }
    $raiz = $repo.Models.GetAt(0)

    # Borrar el paquete anterior para no duplicar elementos
    for ($i = $raiz.Packages.Count - 1; $i -ge 0; $i--) {
        if ($raiz.Packages.GetAt($i).Name -eq "Comportamiento") { $raiz.Packages.DeleteAt($i, $true) }
    }
    $raiz.Packages.Refresh()

    $paquete = $raiz.Packages.AddNew("Comportamiento", "Package")
    $paquete.Update() | Out-Null

    # ---------- 2. Máquina de estados y su diagrama ----------
    $maquina = Nuevo-Elemento $paquete "SesionEstudio" "StateMachine"
    $diag = $maquina.Diagrams.AddNew("SesionEstudio", "Statechart")
    $diag.Update() | Out-Null

    # ---------- 3. Estados ----------
    $inicio       = Nuevo-Elemento $maquina "" "StateNode" 3
    $pendiente    = Nuevo-Elemento $maquina "Pendiente" "State"
    $completada   = Nuevo-Elemento $maquina "Completada" "State"
    $reprogramada = Nuevo-Elemento $maquina "Reprogramada" "State"
    $fin          = Nuevo-Elemento $maquina "" "StateNode" 4

    # Subestados dentro de "Pendiente" (estado compuesto)
    $inicioP = Nuevo-Elemento $pendiente "" "StateNode" 3
    $fuera   = Nuevo-Elemento $pendiente "Fuera de horario" "State"
    $enCurso = Nuevo-Elemento $pendiente "En curso" "State"

    # Nota unida a "En curso"
    $nota = Nuevo-Elemento $maquina "" "Note"
    $nota.Notes = "«En curso» no se guarda en la tabla: lo calcula ControladorSesion.porcentajeEnCurso() y se actualiza cada minuto. Solo las sesiones «Completada» suman horas."
    $nota.Update() | Out-Null

    # ---------- 4. Transiciones ----------
    $t = @{}
    $t.registrar   = Nueva-Transicion $inicio $pendiente "registrar sesión"
    $t.inicioP     = Nueva-Transicion $inicioP $fuera ""
    $t.empieza     = Nueva-Transicion $fuera $enCurso "" "es hoy y la hora actual llega a hora_inicio"
    $t.termina     = Nueva-Transicion $enCurso $fuera "" "llega hora_fin sin terminarla"
    $t.terminarYa  = Nueva-Transicion $enCurso $completada "Terminar ahora" "pasó al menos 1 min" "hora_fin = ahora"
    $t.completarP  = Nueva-Transicion $pendiente $completada "editar y marcar Completada" "hora_fin ya pasó"
    $t.reprogramar = Nueva-Transicion $pendiente $reprogramada "editar y marcar Reprogramada"
    $t.volverP     = Nueva-Transicion $reprogramada $pendiente "editar fecha u hora y marcar Pendiente"
    $t.completarR  = Nueva-Transicion $reprogramada $completada "editar y marcar Completada" "hora_fin ya pasó"
    $t.corregir    = Nueva-Transicion $completada $pendiente "editar para corregir"
    $t.finP        = Nueva-Transicion $pendiente $fin "eliminar la sesión o su materia"
    $t.finC        = Nueva-Transicion $completada $fin "eliminar la sesión o su materia"
    $t.finR        = Nueva-Transicion $reprogramada $fin "eliminar la sesión o su materia"

    # Línea punteada de la nota a "En curso"
    $enlaceNota = $nota.Connectors.AddNew("", "NoteLink")
    $enlaceNota.SupplierID = $enCurso.ElementID
    $enlaceNota.Update() | Out-Null

    # ---------- 5. Posiciones (Pendiente izquierda, Completada derecha, Reprogramada abajo) ----------
    # Primero el estado compuesto y luego sus hijos, para que queden dentro
    Poner $diag $inicio        40   60   20  20
    Poner $diag $pendiente    100  140  640 300
    Poner $diag $inicioP      130  280   20  20
    Poner $diag $fuera        190  260  120  60
    Poner $diag $enCurso      610  260  100  60
    Poner $diag $completada  1020  250  160 190
    Poner $diag $reprogramada 280  580  280  60
    Poner $diag $fin         1085  640   30  30
    Poner $diag $nota         760   20  330  80

    # ---------- 6. Separar las líneas que unen los mismos estados ----------
    # SX/SY = desplazamiento de la salida, EX/EY = de la llegada (desde el centro)
    Ajustar-Linea $diag $t.empieza     "SX=0;SY=-15;EX=0;EY=-15;"
    Ajustar-Linea $diag $t.termina     "SX=0;SY=15;EX=0;EY=15;"
    Ajustar-Linea $diag $t.terminarYa  "SX=0;SY=0;EX=0;EY=55;"
    Ajustar-Linea $diag $t.completarP  "SX=0;SY=-60;EX=0;EY=-15;"
    Ajustar-Linea $diag $t.corregir    "SX=0;SY=-70;EX=0;EY=-110;"
    Ajustar-Linea $diag $t.reprogramar "SX=-110;SY=0;EX=-110;EY=0;"
    Ajustar-Linea $diag $t.volverP     "SX=110;SY=0;EX=110;EY=0;"
    # Reprogramada -> Completada pasa por debajo del estado final (puntos de quiebre)
    Ajustar-Linea $diag $t.completarR  "SX=-40;SY=-30;EX=40;EY=-95;" "500:-720;1140:-720;"
    # Pendiente -> final sale de la esquina inferior derecha de Pendiente
    Ajustar-Linea $diag $t.finP        "SX=250;SY=-150;EX=0;EY=0;"

    # ---------- 7. Guardar y exportar ----------
    $diag.Update() | Out-Null
    $repo.GetProjectInterface().PutDiagramImageToFile($diag.DiagramGUID, $imagen, 1) | Out-Null
    Write-Output "Listo: $modelo"
    Write-Output "Imagen: $imagen"
}
finally {
    $repo.CloseFile()
    $repo.Exit()
    [System.Runtime.InteropServices.Marshal]::ReleaseComObject($repo) | Out-Null
}

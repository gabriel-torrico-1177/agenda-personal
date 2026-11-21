# Crea en Enterprise Architect dos diagramas de secuencia de diseño
# ("Registrar sesión de estudio" y "Generar reporte semanal") y los exporta como PNG.
# Uso (desde la raíz del proyecto):  powershell -ExecutionPolicy Bypass -File docs\ea\diagramas-secuencia.ps1
# Se puede ejecutar varias veces: borra el paquete "Algoritmos" y lo vuelve a crear.

$ErrorActionPreference = "Stop"

$carpeta = $PSScriptRoot
$modelo  = Join-Path $carpeta "AgendaEstudio.qea"
$base    = "C:\Program Files\Sparx Systems\EA Trial\EABase.qea"

# ---------- Funciones de ayuda ----------

# Crea una lifeline y la pone en el diagrama.
# tipo: "Actor" o "Sequence"; estereotipo: boundary, control, entity, database o ""
# y = dónde empieza la cabeza (más abajo si el objeto se crea durante la secuencia)
function Nueva-Lifeline($nombre, $tipo, $estereotipo, $x, $y, $fin) {
    $e = $script:paquete.Elements.AddNew($nombre, $tipo)
    if ($estereotipo -ne "") { $e.Stereotype = $estereotipo }
    $e.Update() | Out-Null
    $o = $script:diag.DiagramObjects.AddNew("l=$x;r=$($x + 120);t=$y;b=$fin;", "")
    $o.ElementID = $e.ElementID
    $o.Update() | Out-Null
    return $e
}

# Crea un mensaje de "origen" a "destino" a la altura y.
# retorno = $true dibuja la flecha punteada de respuesta
function Mensaje($origen, $destino, $texto, $y, $retorno = $false, $estereotipo = "") {
    $script:orden++
    # Con SuppressBrackets EA borra los "()" vacíos: se pone un espacio invisible
    # (U+200B) entre los paréntesis para que "presionarGuardar()" los conserve
    $texto = $texto.Replace("()", "(" + [char]0x200B + ")")
    $c = $origen.Connectors.AddNew($texto, "Sequence")
    $c.SupplierID = $destino.ElementID
    $c.SequenceNo = $script:orden
    if ($estereotipo -ne "") { $c.Stereotype = $estereotipo }
    $c.DiagramID = $script:diag.DiagramID   # el mensaje pertenece a este diagrama
    $c.Update() | Out-Null
    if ($retorno) {
        $c.Properties.Item("IsReturn").Value = $true   # flecha punteada de respuesta
        $c.Update() | Out-Null
    }
    # EA dibuja cada mensaje 35 px debajo del anterior, menos el valor "SY".
    # Se calcula el SY que deja el mensaje a la altura y (se escribe al final, con EA cerrado)
    if ($script:orden -gt 1) {
        $sy = -($y - $script:yAnterior - 35)
        $script:ajustes += "UPDATE t_connector SET PDATA5='SX=0;SY=$sy;EX=0;EY=$sy;' WHERE Connector_ID=$($c.ConnectorID);"
    }
    $script:yAnterior = $y
}

# Crea un fragmento combinado. tipo: 0 = alt, 1 = opt, 4 = loop.
# operandos: lista de @(guarda, alto) de arriba hacia abajo
function Fragmento($tipo, $operandos, $x1, $x2, $y) {
    $f = $script:paquete.Elements.AddNew("", "InteractionFragment")
    $f.Subtype = $tipo
    $f.Update() | Out-Null
    $alto = 0
    foreach ($op in $operandos) {
        $p = $f.Partitions.AddNew($op[0], "")
        $p.Size = $op[1]
        $alto += $op[1]
    }
    $f.Update() | Out-Null
    $o = $script:diag.DiagramObjects.AddNew("l=$x1;r=$x2;t=$y;b=$($y + $alto);", "")
    $o.ElementID = $f.ElementID
    $o.Update() | Out-Null
}

# Crea una nota con su texto en el rectángulo indicado
function Nota($texto, $x1, $x2, $y1, $y2) {
    $n = $script:paquete.Elements.AddNew("", "Note")
    $n.Notes = $texto
    $n.Update() | Out-Null
    $o = $script:diag.DiagramObjects.AddNew("l=$x1;r=$x2;t=$y1;b=$y2;", "")
    $o.ElementID = $n.ElementID
    $o.Update() | Out-Null
}

# Crea un diagrama de secuencia vacío en el paquete
function Nuevo-Diagrama($nombre) {
    $d = $script:paquete.Diagrams.AddNew($nombre, "Sequence")
    $d.StyleEx = "SuppressBrackets=1;"   # que EA no agregue "()" al final de cada mensaje
    $d.Update() | Out-Null
    $script:orden = 0
    return $d
}

# Guarda el diagrama y anota su GUID y el PNG, para exportarlo al final
function Exportar($archivo) {
    $script:diag.Update() | Out-Null
    $script:imagenes[$script:diag.DiagramGUID] = Join-Path $carpeta $archivo
}

# Acceso mínimo a SQLite con la biblioteca que trae Windows (winsqlite3.dll).
# Solo se usa para guardar la altura de los mensajes, que la API de EA no deja cambiar
Add-Type -TypeDefinition @'
using System; using System.Runtime.InteropServices;
public static class Sqlite {
  [DllImport("winsqlite3.dll", CallingConvention=CallingConvention.StdCall)]
  static extern int sqlite3_open16([MarshalAs(UnmanagedType.LPWStr)] string archivo, out IntPtr db);
  [DllImport("winsqlite3.dll", CallingConvention=CallingConvention.StdCall)]
  static extern int sqlite3_exec(IntPtr db, byte[] sql, IntPtr cb, IntPtr arg, out IntPtr error);
  [DllImport("winsqlite3.dll", CallingConvention=CallingConvention.StdCall)]
  static extern int sqlite3_close(IntPtr db);
  // Ejecuta las sentencias SQL en el archivo; devuelve 0 si todo salió bien
  public static int Ejecutar(string archivo, string sql) {
    IntPtr db, error;
    int rc = sqlite3_open16(archivo, out db);
    if (rc != 0) return rc;
    rc = sqlite3_exec(db, System.Text.Encoding.UTF8.GetBytes(sql + "\0"), IntPtr.Zero, IntPtr.Zero, out error);
    sqlite3_close(db);
    return rc;
  }
}
'@

$ajustes  = @()   # sentencias UPDATE con la altura de cada mensaje
$imagenes = @{}   # GUID del diagrama -> ruta del PNG

# ---------- 1. Abrir el modelo y recrear el paquete "Algoritmos" ----------
if (-not (Test-Path $modelo)) { Copy-Item $base $modelo }
$repo = New-Object -ComObject EA.Repository
try {
    if (-not $repo.OpenFile($modelo)) { throw "No se pudo abrir $modelo" }
    if ($repo.Models.Count -eq 0) {
        $repo.Models.AddNew("AgendaEstudio", "").Update() | Out-Null
        $repo.Models.Refresh()
    }
    $raiz = $repo.Models.GetAt(0)
    for ($i = $raiz.Packages.Count - 1; $i -ge 0; $i--) {
        if ($raiz.Packages.GetAt($i).Name -eq "Algoritmos") { $raiz.Packages.DeleteAt($i, $true) }
    }
    $raiz.Packages.Refresh()
    $paquete = $raiz.Packages.AddNew("Algoritmos", "Package")
    $paquete.Update() | Out-Null

    # Ancho de los fragmentos (de la primera a la última lifeline)
    $fin1 = 1300

    # ================= DIAGRAMA 1: Registrar sesión de estudio =================
    $diag = Nuevo-Diagrama "Registrar sesión de estudio"
    # Regla para que nada se encime: el primer mensaje dentro de un fragmento va
    # 55-60 px debajo de su borde (ahí está la guarda) y el borde de abajo, 25-35 px después del último
    $est  = Nueva-Lifeline "Estudiante"          "Actor"    ""         40  50 1340
    $act  = Nueva-Lifeline ":SesionFormActivity" "Sequence" "boundary" 240 50 1340
    $ctrl = Nueva-Lifeline ":ControladorSesion"  "Sequence" "control"  470 50 1340
    $s    = Nueva-Lifeline "s:SesionEstudio"     "Sequence" "entity"   700 195 1340
    $dao  = Nueva-Lifeline ":SesionDAO"          "Sequence" ""         930 50 1340
    $db   = Nueva-Lifeline "agenda.db"           "Sequence" "database" 1160 50 1340

    Mensaje $est  $act  "1: presionarGuardar()" 140
    Mensaje $act  $act  "2: guardar(): leer los campos" 175
    Mensaje $act  $s    "3: new SesionEstudio(...)" 222 $false "create"
    Mensaje $act  $ctrl "4: registrar(s)" 290
    Mensaje $ctrl $ctrl "5: validar(s)" 325
    Mensaje $ctrl $s    "6: getMateriaId()" 380
    Mensaje $s    $ctrl "7: materiaId (debe ser mayor que 0)" 410 $true
    Mensaje $ctrl $s    "8: getDuracionMinutos()" 450
    Mensaje $s    $ctrl "9: fin - inicio en minutos (debe ser mayor que 0)" 480 $true
    Fragmento 1 @(,@('estado = "Completada"', 95)) 440 $fin1 505
    Mensaje $ctrl $ctrl "10: todaviaNoTermino(s)" 565
    Mensaje $ctrl $ctrl "11: seCruzaConOtra(s)" 640
    Mensaje $ctrl $dao  "12: buscarPorFecha(fecha)" 690
    Mensaje $dao  $db   "13: rawQuery(... WHERE s.fecha = ?)" 730
    Mensaje $db   $dao  "14: Cursor" 760 $true
    Mensaje $dao  $ctrl "15: List<SesionEstudio> del día" 790 $true
    Fragmento 4 @(,@('por cada sesión del día, menos ella misma', 95)) 440 $fin1 815
    Mensaje $ctrl $ctrl "16: inicioNueva < finOtra y finNueva > inicioOtra ?" 875
    Fragmento 0 @(@('hay un error', 120), @('else todo correcto', 260)) 20 $fin1 935
    Mensaje $ctrl $act  "17': mensaje de error" 990 $true
    Mensaje $act  $est  "18': mostrar el error con setError o Toast" 1030
    Mensaje $ctrl $dao  "17: insertar(s)" 1110
    Mensaje $dao  $db   "18: insert(`"sesion`", null, ContentValues)" 1150
    Mensaje $db   $dao  "19: id de la fila nueva" 1180 $true
    Mensaje $dao  $ctrl "20: id" 1210 $true
    Mensaje $ctrl $act  "21: `"Sesión guardada`"" 1245 $true
    Mensaje $act  $est  "22: Toast `"Sesión guardada`" y finish()" 1285

    Nota ("validar() revisa las reglas en orden y se detiene en el primer error. " +
          "Antes quita los espacios de los extremos, pone `"Pendiente`" si el estado no es válido " +
          "y revisa el formato de la fecha y las horas.") 1330 1620 290 380
    Nota ("Errores posibles:`r`n- Elija una materia`r`n- Elija la fecha y las horas`r`n" +
          "- La hora de fin debe ser posterior a la de inicio`r`n" +
          "- No puedes completar una sesión que todavía no terminó`r`n" +
          "- Ya tienes una sesión en ese horario") 1330 1620 935 1050
    Exportar "secuencia-registrar-sesion.png"

    # ================= DIAGRAMA 2: Generar reporte semanal =================
    $diag = Nuevo-Diagrama "Generar reporte semanal"
    $fin2 = 1640
    $est  = Nueva-Lifeline "Estudiante"           "Actor"    ""         40   50 1320
    $act  = Nueva-Lifeline ":ReporteActivity"     "Sequence" "boundary" 240  50 1320
    $ctrl = Nueva-Lifeline ":ControladorReporte"  "Sequence" "control"  470  50 1320
    $dao  = Nueva-Lifeline ":SesionDAO"           "Sequence" ""         700  50 1320
    $db   = Nueva-Lifeline "agenda.db"            "Sequence" "database" 1060 50 1320
    $s    = Nueva-Lifeline "s:SesionEstudio"      "Sequence" "entity"   1290 50 1320
    $f    = Nueva-Lifeline "f:FilaReporte"        "Sequence" "entity"   1500 470 1320

    Mensaje $est  $act  "1: abrir Reporte semanal o tocar < / >" 140
    Mensaje $act  $act  "2: mostrarReporte()" 175
    Mensaje $act  $ctrl "3: generarReporte(lunes)" 225
    Mensaje $ctrl $dao  "4: buscarPorSemana(lunes)" 265
    Mensaje $dao  $db   "5: rawQuery(... WHERE s.fecha BETWEEN ? AND date(?, '+6 days'))" 305
    Mensaje $db   $dao  "6: Cursor" 335 $true
    Mensaje $dao  $ctrl "7: List<SesionEstudio> de la semana" 365 $true
    # loop exterior con dos opt adentro (cada guarda con su propio espacio)
    Fragmento 4 @(,@('por cada sesión s de la semana', 380)) 440 $fin2 390
    Fragmento 1 @(,@('es la primera sesión de su materia', 90)) 460 ($fin2 - 20) 435
    Mensaje $ctrl $f    "8: new FilaReporte(s.getNombreMateria())" 495 $false "create"
    Mensaje $ctrl $s    "9: getDuracionMinutos()" 565
    Mensaje $s    $ctrl "10: minutos" 595 $true
    Mensaje $ctrl $f    "11: setMinutosPlanificados(planificados + minutos)" 635
    Fragmento 1 @(,@('s.getEstado() = "Completada"', 85)) 460 ($fin2 - 20) 660
    Mensaje $ctrl $f    "12: setMinutosCompletados(completados + minutos)" 720
    Fragmento 4 @(,@('por cada fila', 95)) 440 $fin2 795
    Mensaje $ctrl $ctrl "13: calcularPorcentaje(f)" 855
    Mensaje $ctrl $ctrl "14: ordenarPorNombre(filas) con Collator" 930
    Mensaje $ctrl $act  "15: List<FilaReporte>" 975 $true
    Fragmento 0 @(@('la lista está vacía', 80), @('else hay filas', 205)) 20 $fin2 1005
    Mensaje $act  $est  "16': mostrar `"No hay sesiones en esta semana`"" 1060
    Mensaje $act  $ctrl "16: calcularTotal(filas)" 1140
    Mensaje $ctrl $act  "17: fila `"Total`" con su porcentaje" 1175 $true
    Mensaje $act  $act  "18: agregarFila() por cada materia y el total" 1210
    Mensaje $act  $est  "19: mostrar la tabla con el total" 1260

    Nota "porcentaje = completados * 100 / planificados (0 si no hay plan)" 820 1060 825 880
    Exportar "secuencia-reporte-semanal.png"
}
finally {
    $repo.CloseFile()
    $repo.Exit()
    [System.Runtime.InteropServices.Marshal]::ReleaseComObject($repo) | Out-Null
}

# ---------- 2. Con EA cerrado: guardar la altura de los mensajes ----------
$rc = [Sqlite]::Ejecutar($modelo, ($ajustes -join "`n"))
if ($rc -ne 0) { throw "No se pudo actualizar la altura de los mensajes (código $rc)" }

# ---------- 3. Volver a abrir el modelo solo para exportar los PNG ----------
$repo = New-Object -ComObject EA.Repository
try {
    if (-not $repo.OpenFile($modelo)) { throw "No se pudo abrir $modelo" }
    foreach ($guid in $imagenes.Keys) {
        $repo.GetProjectInterface().PutDiagramImageToFile($guid, $imagenes[$guid], 1) | Out-Null
        Write-Output "Imagen: $($imagenes[$guid])"
    }
    Write-Output "Listo: $modelo"
}
finally {
    $repo.CloseFile()
    $repo.Exit()
    [System.Runtime.InteropServices.Marshal]::ReleaseComObject($repo) | Out-Null
}

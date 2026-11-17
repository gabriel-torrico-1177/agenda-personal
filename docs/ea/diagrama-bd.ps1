# Crea en Enterprise Architect el modelo de datos de agenda.db (versión 2, igual que BaseDatos.java)
# y lo exporta como PNG.
# Uso (desde la raíz del proyecto):  powershell -ExecutionPolicy Bypass -File docs\ea\diagrama-bd.ps1
# Se puede ejecutar varias veces: borra el paquete "Base de datos" y lo vuelve a crear.

$ErrorActionPreference = "Stop"

$carpeta = $PSScriptRoot
$modelo  = Join-Path $carpeta "AgendaEstudio.qea"
$imagen  = Join-Path $carpeta "base-de-datos.png"
$base    = "C:\Program Files\Sparx Systems\EA Trial\EABase.qea"

# ---------- Funciones de ayuda ----------

# Crea una tabla (clase con estereotipo «table» para SQLite)
function Nueva-Tabla($nombre) {
    $t = $script:paquete.Elements.AddNew($nombre, "Class")
    $t.StereotypeEx = "EAUML::table"
    $t.Gentype = "SQLite"
    $t.Update() | Out-Null
    return $t
}

# Agrega una columna. EA guarda: NOT NULL en AllowDuplicates, PK en IsOrdered,
# UNIQUE en IsStatic y FK en IsCollection
function Columna($tabla, $nombre, $tipo, $noNulo, $pk = $false, $unico = $false, $fk = $false, $defecto = "") {
    $c = $tabla.Attributes.AddNew($nombre, $tipo)
    $c.StereotypeEx = "EAUML::column"
    $c.AllowDuplicates = $noNulo
    $c.IsOrdered = $pk
    $c.IsStatic = $unico
    $c.IsCollection = $fk
    $c.Default = $defecto
    $c.Update() | Out-Null
    if ($pk) {
        # INTEGER PRIMARY KEY AUTOINCREMENT
        $tv = $c.TaggedValues.AddNew("AutoNum", "True"); $tv.Update() | Out-Null
    }
    $tabla.Attributes.Refresh()
}

# Agrega una restricción (PK, FK, unique o check) como operación con estereotipo
function Restriccion($tabla, $nombre, $tipo, $columna, $tipoColumna, $condicion = "") {
    $m = $tabla.Methods.AddNew($nombre, "")
    $m.StereotypeEx = "EAUML::$tipo"
    if ($condicion -ne "") { $m.Code = $condicion }   # la condición del CHECK
    $m.Update() | Out-Null
    if ($columna -ne "") {
        $p = $m.Parameters.AddNew($columna, $tipoColumna)
        $p.Update() | Out-Null
    }
    elseif ($condicion -ne "") {
        # Para que la condición se lea en el diagrama: CK_sesion_estado(estado IN (...)).
        # EA muestra el TIPO de cada parámetro, por eso la condición va como tipo
        $p = $m.Parameters.AddNew("condicion", $condicion)
        $p.Update() | Out-Null
    }
    $tabla.Methods.Refresh()
    return $m
}

# Pone un elemento en el diagrama (izquierda, arriba, ancho, alto)
function Poner($elemento, $x, $y, $ancho, $alto) {
    $o = $script:diag.DiagramObjects.AddNew("l=$x;r=$($x + $ancho);t=$y;b=$($y + $alto);", "")
    $o.ElementID = $elemento.ElementID
    $o.Update() | Out-Null
}

# ---------- 1. Abrir el modelo y recrear el paquete "Base de datos" ----------
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
        if ($raiz.Packages.GetAt($i).Name -eq "Base de datos") { $raiz.Packages.DeleteAt($i, $true) }
    }
    $raiz.Packages.Refresh()
    $paquete = $raiz.Packages.AddNew("Base de datos", "Package")
    $paquete.Update() | Out-Null

    # Diagrama de modelo de datos (Data Modeling)
    $diag = $paquete.Diagrams.AddNew("agenda.db", "Logical")
    $diag.MetaType = "Extended::Data Modeling"
    $diag.StyleEx = "MDGDgm=Extended::Data Modeling;"
    $diag.Update() | Out-Null

    # ---------- 2. Tablas (igual que BaseDatos.java) ----------
    # contacto
    $contacto = Nueva-Tabla "contacto"
    Columna $contacto "id"       "INTEGER" $true $true
    Columna $contacto "nombre"   "TEXT"    $true
    Columna $contacto "telefono" "TEXT"    $true
    Columna $contacto "correo"   "TEXT"    $false
    Restriccion $contacto "PK_contacto" "PK" "id" "INTEGER" | Out-Null

    # materia
    $materia = Nueva-Tabla "materia"
    Columna $materia "id"      "INTEGER" $true $true
    Columna $materia "nombre"  "TEXT"    $true $false $true
    Columna $materia "docente" "TEXT"    $false
    Columna $materia "color"   "TEXT"    $true $false $false $false "'#3F51B5'"
    Restriccion $materia "PK_materia" "PK" "id" "INTEGER" | Out-Null
    Restriccion $materia "UQ_materia_nombre" "unique" "nombre" "TEXT" | Out-Null

    # sesion
    $sesion = Nueva-Tabla "sesion"
    Columna $sesion "id"          "INTEGER" $true $true
    Columna $sesion "materia_id"  "INTEGER" $true $false $false $true
    Columna $sesion "fecha"       "TEXT"    $true
    Columna $sesion "hora_inicio" "TEXT"    $true
    Columna $sesion "hora_fin"    "TEXT"    $true
    Columna $sesion "estado"      "TEXT"    $true $false $false $false "'Pendiente'"
    Columna $sesion "notas"       "TEXT"    $false
    Restriccion $sesion "PK_sesion" "PK" "id" "INTEGER" | Out-Null
    $fk = Restriccion $sesion "FK_sesion_materia" "FK" "materia_id" "INTEGER"
    # ON DELETE CASCADE: se guarda en los valores etiquetados de la FK (como en los ejemplos de EA)
    foreach ($par in @(@("Delete", "Cascade"), @("Update", "No Action"),
                       @("property", "Delete Cascade=1;Update No Action=1;"))) {
        $tv = $fk.TaggedValues.AddNew($par[0], $par[1]); $tv.Update() | Out-Null
    }
    Restriccion $sesion "CK_sesion_estado" "check" "" "" "estado IN ('Pendiente', 'Completada', 'Reprogramada')" | Out-Null

    # meta_semanal
    $meta = Nueva-Tabla "meta_semanal"
    Columna $meta "id"             "INTEGER" $true $true
    Columna $meta "semana_inicio"  "TEXT"    $true $false $true
    Columna $meta "horas_objetivo" "INTEGER" $true
    Restriccion $meta "PK_meta_semanal" "PK" "id" "INTEGER" | Out-Null
    Restriccion $meta "UQ_meta_semanal_semana_inicio" "unique" "semana_inicio" "TEXT" | Out-Null
    Restriccion $meta "CK_meta_semanal_horas_objetivo" "check" "" "" "horas_objetivo BETWEEN 1 AND 80" | Out-Null

    # ---------- 3. Relación sesion.materia_id -> materia.id (1 a 0..*) ----------
    $rel = $sesion.Connectors.AddNew("ON DELETE CASCADE", "Association")
    $rel.StereotypeEx = "EAUML::FK"
    $rel.SupplierID = $materia.ElementID
    $rel.ClientEnd.Role = "FK_sesion_materia"
    $rel.ClientEnd.Cardinality = "0..*"
    $rel.SupplierEnd.Role = "PK_materia"
    $rel.SupplierEnd.Cardinality = "1"
    $rel.Update() | Out-Null

    # ---------- 4. Nota ----------
    $nota = $paquete.Elements.AddNew("", "Note")
    $nota.Notes = "Base de datos agenda.db, versión 2. El nombre del estudiante no está aquí: " +
                  "se guarda en SharedPreferences (archivo perfil, clave nombre)"
    $nota.Update() | Out-Null

    # EA escribe en la relación "(materia_id = id)": el ON DELETE CASCADE va en una nota debajo
    $notaFk = $paquete.Elements.AddNew("", "Note")
    $notaFk.Notes = "FK sesion.materia_id -> materia.id ON DELETE CASCADE: al borrar una materia se borran sus sesiones"
    $notaFk.Update() | Out-Null

    # ---------- 5. Posiciones: materia y sesion arriba, contacto y meta_semanal abajo ----------
    Poner $materia   60  60 300 180
    Poner $sesion   760  60 470 250
    Poner $contacto  60 360 300 160
    Poner $meta     760 380 470 170
    Poner $nota      60 580 400  60
    Poner $notaFk   420 240 300  60

    # ---------- 6. Guardar y exportar ----------
    $diag.Update() | Out-Null
    $repo.GetProjectInterface().PutDiagramImageToFile($diag.DiagramGUID, $imagen, 1) | Out-Null
    Write-Output "Imagen: $imagen"
    Write-Output "Listo: $modelo"
}
finally {
    $repo.CloseFile()
    $repo.Exit()
    [System.Runtime.InteropServices.Marshal]::ReleaseComObject($repo) | Out-Null
}

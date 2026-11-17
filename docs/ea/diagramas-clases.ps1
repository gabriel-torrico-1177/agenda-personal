# Importa el código Java de la app a Enterprise Architect (ingeniería inversa) en el paquete "Clases"
# y arma 5 diagramas de clases, uno por módulo. Exporta un PNG por diagrama (docs/ea/clases-*.png).
# Uso (desde la raíz del proyecto):  powershell -ExecutionPolicy Bypass -File docs\ea\diagramas-clases.ps1
# Se puede ejecutar varias veces: borra el paquete "Clases" y lo vuelve a crear.

$ErrorActionPreference = "Stop"

$carpeta = $PSScriptRoot
$modelo  = Join-Path $carpeta "AgendaEstudio.qea"
$codigo  = (Resolve-Path (Join-Path $carpeta "..\..\app\src\main\java\com\gabriel\agenda")).Path
$base    = "C:\Program Files\Sparx Systems\EA Trial\EABase.qea"

# ---------- Funciones de ayuda ----------

# Recorre los paquetes importados: guarda cada clase por su nombre y borra los diagramas
# que EA crea solo al importar (dejamos únicamente los 5 diagramas por módulo)
function Recorrer($paquete) {
    for ($i = $paquete.Diagrams.Count - 1; $i -ge 0; $i--) { $paquete.Diagrams.DeleteAt($i, $true) }
    foreach ($e in $paquete.Elements) { $script:clases[$e.Name] = $e }
    foreach ($p in $paquete.Packages) { Recorrer $p }
}

# Marca los getters y setters (getNombre / setNombre de un atributo "nombre") como métodos de
# propiedad. Así el diagrama los puede ocultar con HideProps. getDuracionMinutos() no es un getter
# (no hay atributo duracionMinutos), por eso sigue visible
function Marcar-Propiedades($clase) {
    $atributos = @{}
    foreach ($a in $clase.Attributes) { $atributos[$a.Name.ToLower()] = $true }
    foreach ($m in $clase.Methods) {
        if ($m.Name -match '^(get|is|set)([A-Z]\w*)$' -and $atributos.ContainsKey($Matches[2].ToLower())) {
            if ($Matches[1] -eq "set") { $m.StereotypeEx = "property set" } else { $m.StereotypeEx = "property get" }
            $m.Update() | Out-Null
        }
    }
}

# Calcula el ancho y el alto que necesita la caja de una clase (texto en una línea de ~4.3 px por letra)
function Medir($clase, $compacta) {
    $lineas = @(); $ops = 0; $atrs = 0
    if (-not $compacta) {
        foreach ($a in $clase.Attributes) {
            $texto = "- " + $a.Name + ": " + $a.Type
            # EA muestra el valor inicial (cortado a unas 20 letras con "...") y {readOnly} si es final
            if ($a.Default -ne "") { $texto += " = " + $a.Default.Substring(0, [Math]::Min($a.Default.Length, 20)) + "..." }
            if ($a.IsConst) { $texto += " {readOnly}" }
            $lineas += $texto; $atrs++
        }
        foreach ($m in $clase.Methods) {
            if ($m.Stereotype -like "property*") { continue }   # quedan ocultos
            $tipos = ($m.Parameters | ForEach-Object { $_.Type }) -join ", "
            $lineas += "+ " + $m.Name + "(" + $tipos + "): " + $m.ReturnType; $ops++
        }
    }
    $largo = ($lineas | ForEach-Object { $_.Length } | Measure-Object -Maximum).Maximum
    $ancho = [Math]::Max([Math]::Max($largo * 4.3, $clase.Name.Length * 6) + 40, 130)
    $alto  = 40 + 14 * ($atrs + $ops)
    if ($compacta) { $alto = 50; $ancho = [Math]::Max($ancho, 220) }
    return @([int]$ancho, [int]$alto)
}

# Arma un diagrama de clases. filas: lista de filas; cada fila es una lista de
# @(columna, nombre, compacta) o @(columna, nombre, compacta, ancho) para dar más ancho a una caja
# a la que llegan varias líneas (así sus etiquetas no se juntan)
function Nuevo-Diagrama($nombre, $archivo, $filas) {
    $d = $script:paquete.Diagrams.AddNew($nombre, "Logical")
    $d.Update() | Out-Null
    $d.ExtendedStyle = "HideProps=1;"   # ocultar getters y setters
    $d.Update() | Out-Null

    # Medir todas las cajas: ancho de cada columna y alto de cada fila
    $anchoCol = @{}; $altoFila = @{}; $medidas = @{}
    for ($f = 0; $f -lt $filas.Count; $f++) {
        foreach ($celda in $filas[$f]) {
            $m = Medir $script:clases[$celda[1]] $celda[2]
            if ($celda.Count -gt 3) { $m[0] = $celda[3] }
            $medidas[$celda[1]] = $m
            if (-not $anchoCol.ContainsKey($celda[0]) -or $anchoCol[$celda[0]] -lt $m[0]) { $anchoCol[$celda[0]] = $m[0] }
            if (-not $altoFila.ContainsKey($f) -or $altoFila[$f] -lt $m[1]) { $altoFila[$f] = $m[1] }
        }
    }
    # Poner cada caja centrada en su columna; las filas se apilan de arriba hacia abajo
    $y = 40
    for ($f = 0; $f -lt $filas.Count; $f++) {
        foreach ($celda in $filas[$f]) {
            $x = 40
            for ($c = 0; $c -lt $celda[0]; $c++) { if ($anchoCol.ContainsKey($c)) { $x += $anchoCol[$c] + 90 } }
            $m = $medidas[$celda[1]]
            $x += [int](($anchoCol[$celda[0]] - $m[0]) / 2)
            $o = $d.DiagramObjects.AddNew("l=$x;r=$($x + $m[0]);t=$y;b=$($y + $m[1]);", "")
            $o.ElementID = $script:clases[$celda[1]].ElementID
            if ($celda[2]) {
                # Caja compacta (clases padre o de otro módulo): solo el nombre
                $o.ShowPublicAttributes = $false; $o.ShowPrivateAttributes = $false
                $o.ShowProtectedAttributes = $false; $o.ShowPackageAttributes = $false
                $o.ShowPublicOperations = $false; $o.ShowPrivateOperations = $false
                $o.ShowProtectedOperations = $false; $o.ShowPackageOperations = $false
            }
            $o.Update() | Out-Null
        }
        $y += $altoFila[$f] + 80
    }
    $d.Update() | Out-Null
    $script:imagenes[$d.DiagramGUID] = Join-Path $carpeta $archivo
    return $d
}

# Mueve el punto de llegada de la línea "origen -> destino" (ex píxeles desde el centro del destino),
# para que varias líneas que llegan a la misma caja no junten sus etiquetas
function Separar($diagrama, $origen, $destino, $ex) {
    foreach ($c in $script:clases[$origen].Connectors) {
        if ($c.SupplierID -eq $script:clases[$destino].ElementID) {
            $l = $diagrama.DiagramLinks.AddNew("", "")
            $l.ConnectorID = $c.ConnectorID
            $l.Geometry = "SX=0;SY=0;EX=$ex;EY=0;"
            $l.Update() | Out-Null
        }
    }
}

# Crea una dependencia «use»: la clase usa a la otra dentro de un método (no como atributo),
# por eso la ingeniería inversa no la dibuja sola
function Usa($origen, $destino) {
    $c = $script:clases[$origen].Connectors.AddNew("", "Dependency")
    $c.SupplierID = $script:clases[$destino].ElementID
    $c.Stereotype = "use"
    $c.Update() | Out-Null
}

# ---------- 1. Abrir el modelo y recrear el paquete "Clases" ----------
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
        if ($raiz.Packages.GetAt($i).Name -eq "Clases") { $raiz.Packages.DeleteAt($i, $true) }
    }
    $raiz.Packages.Refresh()
    $paquete = $raiz.Packages.AddNew("Clases", "Package")
    $paquete.Update() | Out-Null

    # ---------- 2. Ingeniería inversa: importar la carpeta de código Java ----------
    # Crea un paquete por carpeta (controlador, datos, modelo, vista) con sus clases,
    # las asociaciones (atributos de otra clase) y la herencia entre clases del proyecto
    # Los .java están en UTF-8 sin BOM y EA los leería con la codificación de Windows
    # ("SesiÃ³n"). Se copian a una carpeta temporal con BOM (el código de la app no se toca)
    $copia = Join-Path ([IO.Path]::GetTempPath()) "agenda-ea-import\agenda"
    if (Test-Path $copia) { Remove-Item $copia -Recurse -Force }
    $utf8ConBom = New-Object Text.UTF8Encoding $true
    foreach ($archivo in Get-ChildItem $codigo -Recurse -Filter *.java) {
        $destino = Join-Path $copia $archivo.FullName.Substring($codigo.Length + 1)
        New-Item -ItemType Directory -Force (Split-Path $destino) | Out-Null
        [IO.File]::WriteAllText($destino, [IO.File]::ReadAllText($archivo.FullName, [Text.Encoding]::UTF8), $utf8ConBom)
    }
    $ok = $repo.GetProjectInterface().ImportDirectory($paquete.PackageGUID, "Java", $copia, "recurse=1")
    Remove-Item (Split-Path $copia) -Recurse -Force
    if (-not $ok) { throw "No se pudo importar el código de $codigo" }
    $paquete = $repo.GetPackageByGuid($paquete.PackageGUID)
    $clases = @{}
    Recorrer $paquete
    foreach ($c in @($clases.Values)) { Marcar-Propiedades $c }

    # ---------- 3. Clases de Android (no están en el código) y su herencia ----------
    $android = $paquete.Packages.AddNew("Android", "Package")
    $android.Update() | Out-Null
    foreach ($n in "AppCompatActivity", "BaseAdapter", "SQLiteOpenHelper") {
        $e = $android.Elements.AddNew($n, "Class")
        $e.Update() | Out-Null
        $clases[$n] = $e
    }
    $herencias = @(@("PantallaBase", "AppCompatActivity"), @("BaseDatos", "SQLiteOpenHelper"),
                   @("MateriaAdapter", "BaseAdapter"), @("SesionAdapter", "BaseAdapter"),
                   @("ContactoAdapter", "BaseAdapter"))
    foreach ($h in $herencias) {
        $g = $clases[$h[0]].Connectors.AddNew("", "Generalization")
        $g.SupplierID = $clases[$h[1]].ElementID
        $g.Update() | Out-Null
    }

    # Usos dentro de métodos del módulo Perfil y navegación (revisados en el código):
    # DialogoNombre crea un ControladorPerfil, MenuInferior abre DialogoNombre ("Mi perfil")
    # y PantallaBase llama a MenuInferior.abrir() al deslizar entre pestañas
    Usa "DialogoNombre" "ControladorPerfil"
    Usa "MenuInferior" "DialogoNombre"
    Usa "PantallaBase" "MenuInferior"

    # ---------- 4. Diagramas por módulo (fila por capa: padres, vista, controlador, datos) ----------
    $imagenes = @{}
    Nuevo-Diagrama "Materias" "clases-materias.png" @(
        @(@(1, "PantallaBase", $true), @(2, "BaseAdapter", $true)),
        @(@(0, "MateriaFormActivity", $false), @(1, "MateriasActivity", $false), @(2, "MateriaAdapter", $false)),
        @(@(1, "ControladorMateria", $false), @(2, "Materia", $false)),
        @(,@(1, "MateriaDAO", $false)),
        @(,@(1, "BaseDatos", $true))) | Out-Null

    Nuevo-Diagrama "Sesiones de estudio" "clases-sesiones.png" @(
        @(@(1, "PantallaBase", $true), @(2, "BaseAdapter", $true)),
        @(@(0, "SesionFormActivity", $false), @(1, "CalendarioActivity", $false), @(2, "SesionAdapter", $false)),
        @(@(0, "Materia", $true), @(1, "ControladorSesion", $false), @(2, "SesionEstudio", $false)),
        @(,@(1, "SesionDAO", $false)),
        @(,@(1, "BaseDatos", $true))) | Out-Null

    $progreso = Nuevo-Diagrama "Progreso" "clases-progreso.png" @(
        @(,@(1, "PantallaBase", $true)),
        @(@(0, "MetaActivity", $false), @(1, "EstadisticasActivity", $false), @(2, "ReporteActivity", $false)),
        @(@(0, "ControladorMeta", $false), @(1, "ControladorEstadisticas", $false), @(2, "ControladorReporte", $false)),
        @(@(0, "MetaDAO", $false), @(1, "SesionDAO", $true, 420), @(2, "FilaReporte", $false)),
        @(@(0, "MetaSemanal", $false), @(1, "BaseDatos", $true, 420)))
    # Tres controladores llegan a SesionDAO y dos DAO a BaseDatos: se separan sus llegadas
    Separar $progreso "ControladorMeta" "SesionDAO" -150
    Separar $progreso "ControladorReporte" "SesionDAO" 150
    Separar $progreso "MetaDAO" "BaseDatos" -130

    Nuevo-Diagrama "Contactos" "clases-contactos.png" @(
        @(@(1, "PantallaBase", $true), @(2, "BaseAdapter", $true)),
        @(@(0, "ContactoFormActivity", $false), @(1, "ContactosActivity", $false), @(2, "ContactoAdapter", $false)),
        @(@(1, "ControladorContacto", $false), @(2, "Contacto", $false)),
        @(,@(1, "ContactoDAO", $false)),
        @(,@(1, "BaseDatos", $true))) | Out-Null

    Nuevo-Diagrama "Perfil y navegación" "clases-perfil-navegacion.png" @(
        @(@(0, "AppCompatActivity", $true), @(2, "SQLiteOpenHelper", $true)),
        @(@(0, "PantallaBase", $false), @(1, "MenuInferior", $false), @(2, "BaseDatos", $false)),
        @(@(1, "DialogoNombre", $false), @(2, "ControladorPerfil", $false)),
        @(,@(2, "PerfilDatos", $false))) | Out-Null

    # ---------- 5. Exportar ----------
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

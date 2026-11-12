# Agenda de Estudio · instrucciones para Claude Code

## Quién soy y cómo quiero que me ayudes
- Soy Gabriel, estudiante de Ingeniería de Sistemas (UPDS, Santa Cruz de la Sierra), materia Aplicaciones Móviles I.
- Soy principiante en Java y en Android. Aprendo repitiendo y escribiendo código yo mismo.
- En la defensa del proyecto tengo que **explicar todo el código** y mostrar la app en mi teléfono. Por eso:
  - Trabaja **un paso a la vez**, nunca toda la app de golpe. Haz solo lo que pido en cada mensaje.
  - Antes de crear o cambiar archivos, dime en 2 o 3 líneas qué vas a hacer y en qué archivos.
  - Después de cada paso, explícame el código nuevo **en español sencillo**: qué hace cada método y por qué está en esa capa.
  - Pon comentarios cortos en español en el código (qué hace cada método y cada bloque importante).
  - Si algo es nuevo para mí (por ejemplo, un adaptador o un Intent), explícalo con un ejemplo corto.
  - Usa la forma larga `new View.OnClickListener() { ... }` en lugar de lambdas, que es la que vemos en clase.
  - Al final de cada paso, dime cómo probarlo en el emulador.
- Respóndeme siempre en español.

## Proyecto (consigna 7)
- "Aplicación móvil para gestionar y controlar las horas de estudio como estudiante". Nombre visible: **Agenda de Estudio**.
- App nativa Android en **Java** (no Kotlin), plantilla **Empty Views Activity** con layouts XML (no Jetpack Compose).
- Paquete base: `com.gabriel.agenda` (no cambiarlo). Mínimo SDK: API 24 (Android 7.0).
- Módulos: **materias**, **sesiones de estudio** (planificación), **meta semanal**, **reporte semanal**, **estadísticas** y **contactos** (compañeros y docentes; ya programado).
- Un solo estudiante por teléfono. Sin internet, sin inicio de sesión, sin notificaciones, sin nube, sin cronómetro, sin exportar a PDF. Interfaz en español.
- Historias de usuario y pruebas de aceptación: @docs/historias-de-usuario.md
- Diagramas de referencia (imágenes): carpeta `docs/diagramas/` (secuencia `hu01.png`…`hu15.png`, `dominio.png`, `bd.png`, `cl_*.png`, `arquitectura.png`, `prototipo.png`).

## Arquitectura en 3 capas (respetarla siempre)
Cada capa solo habla con la de abajo: Vista → Controlador → DAO → SQLite.

| Paquete | Clases | Regla |
|---|---|---|
| `com.gabriel.agenda.vista` | MainActivity, CalendarioActivity, SesionFormActivity, MateriasActivity, MateriaFormActivity, EstadisticasActivity, ReporteActivity, MetaActivity, ContactosActivity, ContactoFormActivity y los adaptadores (ContactoAdapter, MateriaAdapter, SesionAdapter) | Solo muestran datos y leen lo que escribe el usuario. **No** validan, **no** calculan horas y **no** usan SQL. |
| `com.gabriel.agenda.controlador` | ControladorContacto, ControladorMateria, ControladorSesion, ControladorMeta, ControladorReporte, ControladorEstadisticas | Validan los datos, calculan horas y porcentajes y deciden qué hacer. Devuelven un mensaje (String) o los datos que la vista muestra. |
| `com.gabriel.agenda.modelo` | Contacto, Materia, SesionEstudio, MetaSemanal, FilaReporte | Clases simples con atributos privados, constructores, getters y setters. |
| `com.gabriel.agenda.datos` | BaseDatos (extiende SQLiteOpenHelper), ContactoDAO, MateriaDAO, SesionDAO, MetaDAO | Único lugar donde hay SQL. |

- Principio de responsabilidad única (SRP): cada clase con una sola tarea.
- **No** crear interfaces extra para los DAO ni capas adicionales: el diseño debe ser simple (decisión del informe, sección 4.1).

## Base de datos (SQLite, archivo `agenda.db`, **versión 2**)
La versión 1 tenía las tablas `contacto` y `evento`. La versión 2 quita `evento` y agrega `materia`, `sesion` y `meta_semanal`.
```sql
CREATE TABLE contacto (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre    TEXT NOT NULL,
    telefono  TEXT NOT NULL,
    correo    TEXT
);
CREATE TABLE materia (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre   TEXT NOT NULL UNIQUE,
    docente  TEXT,
    color    TEXT NOT NULL DEFAULT '#3F51B5'
);
CREATE TABLE sesion (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    materia_id   INTEGER NOT NULL REFERENCES materia(id) ON DELETE CASCADE,
    fecha        TEXT NOT NULL,   -- AAAA-MM-DD
    hora_inicio  TEXT NOT NULL,   -- HH:MM
    hora_fin     TEXT NOT NULL,   -- HH:MM
    estado       TEXT NOT NULL DEFAULT 'Pendiente'
                 CHECK (estado IN ('Pendiente', 'Completada', 'Reprogramada')),
    notas        TEXT
);
CREATE TABLE meta_semanal (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    semana_inicio   TEXT NOT NULL UNIQUE,   -- AAAA-MM-DD del lunes
    horas_objetivo  INTEGER NOT NULL CHECK (horas_objetivo BETWEEN 1 AND 80)
);
```
- `onConfigure(db)`: llamar `db.setForeignKeyConstraintsEnabled(true)` para que funcione el `ON DELETE CASCADE`.
- `onUpgrade` de 1 a 2: `DROP TABLE IF EXISTS evento` y crear las tablas nuevas **sin borrar la tabla contacto** (los contactos ya guardados se conservan).
- Usar `SQLiteOpenHelper` con `ContentValues`, `insert`, `query`/`rawQuery` con parámetros `?`, `update` y `delete`. **No** usar Room.
- Orden: contactos y materias por nombre (A-Z). El DAO trae los datos con `COLLATE NOCASE`, pero el **orden final lo hace el controlador** (`ordenarPorNombre`, privado) con `java.text.Collator` en español (`Locale.forLanguageTag("es")`, fuerza `PRIMARY`), que ignora tildes y mayúsculas ("Álgebra" va antes que "Matemáticas"). Sesiones por `hora_inicio`.
- Las horas **no se guardan**: se calculan con `hora_fin - hora_inicio` (método `getDuracionMinutos()` de SesionEstudio). **Solo cuentan las sesiones "Completada"** para la meta, el reporte (horas hechas) y las estadísticas. El reporte muestra como "planificadas" las horas de todas las sesiones de la semana.
- La semana va de lunes a domingo. Para buscar sesiones de una semana: `fecha BETWEEN lunes AND domingo`.

## Métodos acordados (diagramas de clases del informe)
- ControladorContacto: `registrar(nombre, telefono, correo)`, `listar()`, `buscar(texto)`, `obtener(id)`, `actualizar(contacto)`, `eliminar(id)`, `validar(...)` (privado). **Ya existe: no cambiarlo.**
- ContactoDAO: `insertar(c)`, `obtenerTodos()`, `buscarPorNombre(texto)`, `buscarPorId(id)`, `actualizar(c)`, `eliminar(id)`. **Ya existe.**
- ControladorMateria: `registrar(nombre, docente, color)`, `listar()`, `obtener(id)`, `actualizar(materia)`, `eliminar(id)`, `validar(materia)` (privado).
- MateriaDAO: `insertar(m)`, `obtenerTodas()`, `buscarPorId(id)`, `existeNombre(nombre, idExcluir)`, `actualizar(m)`, `eliminar(id)`.
- ControladorSesion: `registrar(sesion)`, `listarPorFecha(fecha)`, `obtener(id)`, `actualizar(sesion)`, `eliminar(id)`, `validar(sesion)` (privado).
- SesionDAO: `insertar(s)`, `buscarPorFecha(fecha)`, `buscarPorSemana(lunes)`, `buscarPorId(id)`, `actualizar(s)`, `eliminar(id)`. Al leer, traer también el nombre y el color de la materia con un `JOIN`.
- ControladorMeta: `definirMeta(lunes, horas)`, `obtenerProgreso(lunes)` (horas completadas y meta).
- MetaDAO: `guardar(meta)` (inserta o actualiza la meta de esa semana), `buscarPorSemana(lunes)`.
- ControladorReporte: `generarReporte(lunes)` → `List<FilaReporte>` (materia, horas planificadas, horas completadas, porcentaje).
- ControladorEstadisticas: `totalHoras(lunes)`, `variacionSemanaAnterior(lunes)` (porcentaje), `horasPorMateria(lunes)` (de mayor a menor).

## Validaciones y mensajes (deben coincidir con las pruebas de aceptación)
- Contacto: "El nombre es obligatorio", "Ingrese un teléfono válido" (7 u 8 dígitos), "Ingrese un correo válido" (si se escribe y no tiene "@").
- Materia: "El nombre es obligatorio", "Ya existe una materia con ese nombre". Si no se elige color, usar `#3F51B5`. Docente opcional.
- Sesión: "Elija una materia", "La hora de fin debe ser posterior a la de inicio". Estado por defecto: "Pendiente". Si no hay materias al abrir el formulario: "Primero registre una materia".
- Meta: "Ingrese una meta entre 1 y 80 horas". Al alcanzarla: "¡Meta cumplida! Sigue así".
- Éxito: "Contacto guardado/actualizado/eliminado", "Materia guardada/actualizada/eliminada", "Sesión guardada/actualizada/eliminada", "Meta guardada".
- Listas vacías: "Aún no tienes contactos", "No se encontraron contactos", "Aún no tienes materias", "No tienes sesiones para este día", "No hay sesiones en esta semana", "Aún no hay horas registradas esta semana".
- Eliminar siempre con `AlertDialog` y botones "Sí" y "No": "¿Eliminar a {nombre}?", "¿Eliminar {materia}? También se borrarán sus sesiones", "¿Eliminar la sesión de {materia}?".
- Los errores de validación se muestran en el campo con `setError(...)` (en un `Spinner` o un selector, con `Toast`). Los mensajes de éxito, con `Toast`.

## Pantallas (prototipo: `docs/diagramas/prototipo.png`)
- P1 `MainActivity` (Inicio): saludo y fecha, progreso de la meta semanal ("12 / 20 h" con `ProgressBar` horizontal), próxima sesión, botón "+ Nueva sesión" y sesiones de hoy.
- P2 `CalendarioActivity`: tira con los 7 días de la semana (botones en un `LinearLayout` horizontal, ◀ ▶ para cambiar de semana, un punto en los días con sesiones), `ListView` de sesiones del día con materia (color), horario y estado, ✕ para eliminar y botón +.
- P3 `SesionFormActivity`: `Spinner` de materia, fecha con `DatePickerDialog`, inicio y fin con `TimePickerDialog`, estado con `RadioGroup` (Pendiente, Completada, Reprogramada), notas, Cancelar y Guardar. Sirve para crear y editar (recibe el id por `Intent`).
- P4 `MateriasActivity`: `ListView` con un círculo del color, nombre y docente, ✕ para eliminar y botón +.
- P5 `MateriaFormActivity`: nombre, docente y una fila de 6 colores para elegir. Crear y editar.
- P6 `EstadisticasActivity`: horas de la semana, comparación con la semana anterior (↑ / ↓ %), meta y una barra horizontal por materia (Views con ancho proporcional dentro de un `LinearLayout`, sin librerías de gráficos).
- P7 `ReporteActivity`: semana con ◀ ▶ y tabla Materia / Plan. / Hechas / % con fila de total (`TableLayout` o `ListView`).
- P8 `MetaActivity`: horas objetivo de la semana, progreso y Guardar.
- P9 `ContactosActivity` y P10 `ContactoFormActivity`: ya programadas.
- Menú inferior en P1, P2, P6 (y P4, P7, P8, P9): un `LinearLayout` horizontal con 4 botones: Inicio, Calendario, Estadísticas y Más. "Más" abre un `PopupMenu` con Materias, Reporte semanal, Meta semanal y Contactos. Simple, sin librerías extra.
- Para las listas usar `ListView` con `BaseAdapter`, que es lo que vemos en clase.
- Textos de la interfaz en `res/values/strings.xml`.

## Reglas de trabajo
- Usa solo lo que ya trae la plantilla (AndroidX, AppCompat, Material). **No** agregues librerías nuevas ni cambies versiones de Gradle sin preguntarme.
- Después de cambiar código, compila con `./gradlew assembleDebug` (en Windows: `gradlew.bat assembleDebug`) y corrige los errores antes de decir que terminaste.
- No borres ni reescribas archivos que no tienen que ver con el paso actual.
- Al terminar cada paso, agrega una línea a `docs/bitacora.md` con la fecha, la historia, qué se hizo y qué problemas hubo (la uso para la sección 5.2 del informe).
- Una historia está terminada (Done) cuando pasa **todas** sus pruebas de aceptación de `docs/historias-de-usuario.md`.
- Git: la rama main siempre debe compilar y funcionar. Para cada historia de usuario crea una rama feature/hu-XX-nombre desde main y haz commits pequeños en español. Cada vez que termines un paso que compila, haz commit y súbelo a GitHub (git push) en esa rama. Cuando la historia pase todas sus pruebas de aceptación: pídeme permiso, une la rama a main con git merge --no-ff y el mensaje "Merge HU-XX: nombre", sube main a GitHub y borra la rama en local y en GitHub.
- Cada historia tiene un issue en GitHub con el mismo número (HU-05 = #5). En el mensaje del merge a main agrega 'Closes #N' para que el issue se cierre solo.
- Documentación: no copiar el informe al repositorio. Documentar solo con: comentarios cortos en español en el código, mensajes de commit claros en español, una línea en docs/bitacora.md al terminar cada paso (fecha, historia, qué se hizo, problemas) y marcar con [x] en docs/historias-de-usuario.md cada prueba de aceptación que pase. El README.md se completa al final del proyecto con descripción, funciones, cómo ejecutarlo y capturas.

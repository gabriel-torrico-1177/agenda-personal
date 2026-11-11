# Agenda Personal · instrucciones para Claude Code

## Quién soy y cómo quiero que me ayudes
- Soy Gabriel, estudiante de Ingeniería de Sistemas (UPDS, Santa Cruz de la Sierra), materia Aplicaciones Móviles I.
- Soy principiante en Java y en Android. Aprendo repitiendo y escribiendo código yo mismo.
- En la defensa del proyecto tengo que **explicar todo el código** y mostrar la app en mi teléfono. Por eso:
  - Trabaja **un paso a la vez**, nunca toda la app de golpe. Haz solo lo que pido en cada mensaje.
  - Antes de crear o cambiar archivos, dime en 2 o 3 líneas qué vas a hacer y en qué archivos.
  - Después de cada paso, explícame el código nuevo **en español sencillo**: qué hace cada método y por qué está en esa capa.
  - Pon comentarios cortos en español en el código (qué hace cada método y cada bloque importante).
  - Si algo es nuevo para mí (por ejemplo, un adaptador o un Intent), explícalo con un ejemplo corto.
  - Al final de cada paso, dime cómo probarlo en el emulador.
- Respóndeme siempre en español.

## Proyecto
- App nativa Android en **Java** (no Kotlin), plantilla **Empty Views Activity** con layouts XML (no Jetpack Compose).
- Paquete base: `com.gabriel.agenda`. Mínimo SDK: API 24 (Android 7.0).
- Agenda personal **para cualquier usuario**: gestiona **contactos** y **eventos**. Un solo usuario por teléfono.
- Sin internet, sin inicio de sesión, sin notificaciones, sin nube. Interfaz en español.
- Historias de usuario y pruebas de aceptación: @docs/historias-de-usuario.md
- Diagramas de referencia (imágenes): carpeta `docs/diagramas/`.

## Arquitectura en 3 capas (respetarla siempre)
Cada capa solo habla con la de abajo: Vista → Controlador → DAO → SQLite.

| Paquete | Clases | Regla |
|---|---|---|
| `com.gabriel.agenda.vista` | MainActivity, ContactosActivity, ContactoFormActivity, EventosActivity, EventoFormActivity | Solo muestran datos y leen lo que escribe el usuario. **No** validan ni usan SQL. |
| `com.gabriel.agenda.controlador` | ControladorContacto, ControladorEvento | Validan los datos y deciden qué hacer. Devuelven un mensaje (String) que la vista muestra. |
| `com.gabriel.agenda.modelo` | Contacto, Evento | Clases simples con atributos privados, constructor, getters y setters. |
| `com.gabriel.agenda.datos` | BaseDatos (extiende SQLiteOpenHelper), ContactoDAO, EventoDAO | Único lugar donde hay SQL. |

- Principio de responsabilidad única (SRP): cada clase con una sola tarea.
- **No** crear interfaces extra para los DAO ni capas adicionales: el diseño debe ser simple (decisión del informe, sección 4.1).

## Base de datos (SQLite, archivo `agenda.db`, versión 1)
```sql
CREATE TABLE contacto (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre    TEXT NOT NULL,
    telefono  TEXT NOT NULL,
    correo    TEXT
);
CREATE TABLE evento (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo      TEXT NOT NULL,
    tipo        TEXT NOT NULL DEFAULT 'Otro'
                CHECK (tipo IN ('Personal', 'Trabajo', 'Estudio', 'Otro')),
    fecha       TEXT NOT NULL,   -- formato AAAA-MM-DD
    hora        TEXT NOT NULL,   -- formato HH:MM
    lugar       TEXT,
    descripcion TEXT
);
```
- Usar `SQLiteOpenHelper` con `ContentValues`, `insert`, `query`/`rawQuery` con parámetros `?`, `update` y `delete`. **No** usar Room.
- Contactos ordenados por nombre (A-Z). Eventos ordenados por hora.

## Métodos acordados (diagramas de clases del informe)
- ControladorContacto: `registrar(nombre, telefono, correo)`, `listar()`, `buscar(texto)`, `obtener(id)`, `actualizar(contacto)`, `eliminar(id)`, `validar(nombre, telefono)` (privado).
- ContactoDAO: `insertar(c)`, `obtenerTodos()`, `buscarPorNombre(texto)`, `buscarPorId(id)`, `actualizar(c)`, `eliminar(id)`.
- ControladorEvento: `registrar(evento)`, `listarPorFecha(fecha)`, `obtener(id)`, `actualizar(evento)`, `eliminar(id)`, `validar(evento)` (privado).
- EventoDAO: `insertar(e)`, `buscarPorFecha(fecha)`, `buscarPorId(id)`, `actualizar(e)`, `eliminar(id)`.

## Validaciones y mensajes (deben coincidir con las pruebas de aceptación)
- Nombre vacío → "El nombre es obligatorio".
- Teléfono que no tiene 7 u 8 dígitos (solo números) → "Ingrese un teléfono válido".
- Correo opcional; si se escribe y no tiene "@" → "Ingrese un correo válido".
- Título de evento vacío → "El título es obligatorio". Tipo por defecto: "Otro".
- Éxito: "Contacto guardado", "Contacto actualizado", "Contacto eliminado", "Evento guardado", "Evento actualizado", "Evento eliminado".
- Listas vacías: "Aún no tienes contactos", "No se encontraron contactos", "No tienes eventos para este día".
- Eliminar siempre con `AlertDialog`: "¿Eliminar a {nombre}?" / "¿Eliminar el evento {título}?" con botones "Sí" y "No".
- Los errores de validación se muestran en el campo con `setError(...)`. Los mensajes de éxito, con `Toast`.

## Pantallas (prototipo: `docs/diagramas/Prototipo-baja-fidelidad.png`)
- P1 `MainActivity` (panel principal): saludo y fecha, dos recuadros con contadores (eventos de hoy y contactos), botones "+ Nuevo evento" y "+ Nuevo contacto", próximos eventos de hoy y algunos contactos.
- P2 `ContactosActivity`: buscador arriba, `ListView` de contactos, botón ✕ para eliminar, botón + para agregar.
- P3 `ContactoFormActivity`: nombre, teléfono (`inputType="phone"`), correo, botones Cancelar y Guardar. La misma pantalla sirve para crear y editar (recibe el id por `Intent` si es edición).
- P4 `EventosActivity`: tira con los 7 días de la semana (botones en un `LinearLayout` horizontal, flechas para cambiar de semana), tarjeta con el próximo evento del día, `ListView` de eventos del día.
- P5 `EventoFormActivity`: título, `Spinner` de tipo, fecha con `DatePickerDialog`, hora con `TimePickerDialog`, lugar y descripción.
- Menú inferior en P1, P2 y P4: un `LinearLayout` horizontal con 3 botones (Inicio, Eventos, Contactos). Hacerlo simple, sin librerías extra.
- Para las listas usar `ListView` con `ArrayAdapter` o un adaptador sencillo (`BaseAdapter`), que es lo que vemos en clase.
- Textos de la interfaz en `res/values/strings.xml`.

## Reglas de trabajo
- Usa solo lo que ya trae la plantilla (AndroidX, AppCompat, Material). **No** agregues librerías nuevas ni cambies versiones de Gradle sin preguntarme.
- Después de cambiar código, compila con `./gradlew assembleDebug` (en Windows: `gradlew.bat assembleDebug`) y corrige los errores antes de decir que terminaste.
- No borres ni reescribas archivos que no tienen que ver con el paso actual.
- Si el proyecto usa Git, al terminar cada historia propón un commit con un mensaje en español, por ejemplo: "HU-01: registrar contacto".
- Al terminar cada paso, agrega una línea a `docs/bitacora.md` con la fecha, la historia, qué se hizo y qué problemas hubo (la uso para la sección 5.2 del informe).
- Una historia está terminada (Done) cuando pasa **todas** sus pruebas de aceptación de `docs/historias-de-usuario.md`.
- Git: la rama main siempre debe compilar y funcionar. Para cada historia de usuario crea una rama feature/hu-XX-nombre desde main y haz commits pequeños en español. Cada vez que termines un paso que compila, haz commit y súbelo a GitHub (git push) en esa rama. Cuando la historia pase todas sus pruebas de aceptación: pídeme permiso, une la rama a main con git merge --no-ff y el mensaje "Merge HU-XX: nombre", sube main a GitHub y borra la rama en local y en GitHub.
- Documentación: no copiar el informe al repositorio. Documentar solo con: comentarios cortos en español en el código, mensajes de commit claros en español, una línea en docs/bitacora.md al terminar cada paso (fecha, historia, qué se hizo, problemas) y marcar con [x] en docs/historias-de-usuario.md cada prueba de aceptación que pase. El README.md se completa al final del proyecto con descripción, funciones, cómo ejecutarlo y capturas.

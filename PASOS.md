# Cómo empezar a programar la Agenda Personal con Claude Code

## 1. Revisa que tengas todo
- **Android Studio** con un emulador creado (Device Manager) o tu teléfono con **Depuración USB** activada (lo necesitas para la defensa).
- **Claude Code** instalado: abre la terminal de Android Studio (abajo, pestaña *Terminal*) y escribe `claude --version`. Si responde con un número, está bien.
- **Plugin Claude Code [Beta]** activo en *Settings → Plugins*.
- (Recomendado) **Git**: si escribes `git --version` en la terminal y no lo reconoce, instálalo desde git-scm.com. Sirve para volver atrás si algo se rompe.

## 2. Crea el proyecto
1. *File → New → New Project* → **Empty Views Activity** → *Next*.
2. Completa:
   - **Name:** `Agenda Personal`
   - **Package name:** `com.gabriel.agenda`
   - **Language:** `Java`
   - **Minimum SDK:** `API 24 ("Nougat"; Android 7.0)`
   - Lo demás, como viene.
3. *Finish* y espera a que termine Gradle (barra de abajo).
4. Ejecuta una vez con ▶ en el emulador para comprobar que abre "Hello World!".

## 3. Copia el kit dentro del proyecto
1. Descomprime este kit.
2. Copia **`CLAUDE.md`** y la carpeta **`docs`** en la **carpeta raíz del proyecto** (donde están la carpeta `app` y el archivo `settings.gradle.kts`).
3. En Android Studio, cambia la vista de *Android* a *Project* (arriba a la izquierda) para verlos.

Claude Code lee `CLAUDE.md` **solo, cada vez que se abre**. Ahí está todo: la arquitectura, la base de datos, los mensajes y cómo quieres que te explique. No hace falta pegarlo en el chat.

## 4. (Recomendado) Activa Git
*VCS → Enable Version Control Integration → Git → OK*. Después de cada historia terminada, guarda un commit (Claude te lo propondrá).

## 5. Abre Claude Code
- Atajo **Ctrl + Esc**, o en la terminal de Android Studio escribe `claude`.
- La primera vez te pedirá iniciar sesión con tu cuenta de Claude.
- Cuando quiera cambiar un archivo, te mostrará los cambios y te pedirá permiso. **Léelos antes de aceptar**: así aprendes y lo puedes explicar en la defensa.
- **Esc** detiene a Claude si se está yendo por otro lado.

Primer mensaje (cópialo tal cual):
```
Lee CLAUDE.md y docs/historias-de-usuario.md. Dime en 5 líneas qué entendiste del proyecto y cuál es el primer paso. No cambies nada todavía.
```

## 6. Mensajes para cada paso
Envía **uno a la vez**. Después de cada uno: compila, prueba en el emulador, pregunta lo que no entiendas (por ejemplo, "explícame la línea 25 de ContactoDAO") y recién pasa al siguiente.

### Sprint 1: contactos (16/11 al 20/11)
**Paso 1 · Paquetes**
```
Paso 1: crea los paquetes vista, controlador, modelo y datos dentro de com.gabriel.agenda y mueve MainActivity al paquete vista (actualiza el AndroidManifest). Compila y explícame qué cambió.
```
**Paso 2 · Modelo Contacto**
```
Paso 2: crea la clase Contacto en el paquete modelo con id, nombre, telefono y correo, dos constructores (con id y sin id), getters y setters. Explícame para qué sirve cada parte.
```
**Paso 3 · Base de datos**
```
Paso 3: crea la clase BaseDatos en el paquete datos, que extienda SQLiteOpenHelper y cree las tablas contacto y evento exactamente como dice CLAUDE.md. Explícame onCreate y onUpgrade.
```
**Paso 4 · ContactoDAO**
```
Paso 4: crea ContactoDAO en el paquete datos con los métodos insertar, obtenerTodos (ordenado por nombre), buscarPorNombre, buscarPorId, actualizar y eliminar. Explícame ContentValues, Cursor y por qué se usan los signos ? en las consultas.
```
**Paso 5 · ControladorContacto**
```
Paso 5: crea ControladorContacto en el paquete controlador con las validaciones de CLAUDE.md y los métodos registrar, listar, buscar, obtener, actualizar y eliminar. Explícame cómo se conecta con el DAO.
```
**Paso 6 · HU-01 Registrar contacto**
```
Paso 6 (HU-01): crea ContactoFormActivity con su layout (nombre, teléfono, correo, Cancelar y Guardar) según el prototipo P3. Por ahora agrega en MainActivity un botón "+ Nuevo contacto" que la abra. Usa setError para los errores y Toast para "Contacto guardado". Dime cómo probar las pruebas de aceptación de HU-01.
```
**Paso 7 · HU-02 Ver y buscar**
```
Paso 7 (HU-02): crea ContactosActivity con el buscador y un ListView de contactos (nombre y teléfono), ordenados de la A a la Z, con los mensajes de lista vacía y sin resultados. Al guardar un contacto nuevo, que vuelva a esta lista. Explícame el adaptador.
```
**Paso 8 · HU-03 Editar**
```
Paso 8 (HU-03): al tocar un contacto de la lista, abre ContactoFormActivity con sus datos cargados para editarlo (pasa el id con un Intent). Explícame cómo se pasa el id entre pantallas.
```
**Paso 9 · HU-04 Eliminar**
```
Paso 9 (HU-04): agrega el botón ✕ en cada contacto que pida confirmación con un AlertDialog ("¿Eliminar a ...?", Sí / No) y luego actualice la lista.
```
**Paso 10 · Panel y menú (parte de contactos)**
```
Paso 10: arma MainActivity como el panel P1, por ahora solo con lo de contactos: saludo, fecha, recuadro con el número de contactos, botón "+ Nuevo contacto", algunos contactos y el menú inferior (Inicio, Eventos, Contactos). Pon también el menú inferior en ContactosActivity.
```
✔ Al terminar, revisa todas las pruebas de aceptación de HU-01 a HU-04 y haz un commit.

### Sprint 2: eventos (21/11 al 25/11)
**Paso 11 · Modelo y DAO**
```
Paso 11: crea la clase Evento en modelo y EventoDAO en datos (insertar, buscarPorFecha ordenado por hora, buscarPorId, actualizar, eliminar). Explícame por qué la fecha se guarda como AAAA-MM-DD.
```
**Paso 12 · ControladorEvento**
```
Paso 12: crea ControladorEvento con la validación del título y el tipo "Otro" por defecto, y los métodos de CLAUDE.md.
```
**Paso 13 · HU-05 Registrar evento**
```
Paso 13 (HU-05): crea EventoFormActivity según el prototipo P5, con Spinner de tipo, DatePickerDialog para la fecha y TimePickerDialog para la hora. Explícame cómo funcionan los selectores.
```
**Paso 14 · HU-06 Eventos de la semana**
```
Paso 14 (HU-06): crea EventosActivity según el prototipo P4: tira con los 7 días de la semana (con flechas para cambiar de semana), tarjeta del próximo evento y ListView de los eventos del día elegido, con el mensaje de día sin eventos y el menú inferior.
```
**Paso 15 · HU-07 Editar evento**
```
Paso 15 (HU-07): al tocar un evento, abre EventoFormActivity con sus datos para editarlo.
```
**Paso 16 · HU-08 Eliminar evento**
```
Paso 16 (HU-08): agrega el botón ✕ en cada evento con confirmación (AlertDialog) y actualiza la lista.
```
**Paso 17 · Completar el panel**
```
Paso 17: completa MainActivity: recuadro de eventos de hoy, botón "+ Nuevo evento" y lista de próximos eventos de hoy. Que los recuadros y el menú lleven a sus pantallas.
```
✔ Revisa todas las pruebas de aceptación de HU-05 a HU-08, prueba la app en tu teléfono y haz un commit.

## 7. Consejos
- **Si sale un error**, cópialo completo y pégaselo a Claude: "Me sale este error, explícame qué significa y corrígelo".
- **Si la conversación se hace muy larga**, escribe `/clear` y continúa con el siguiente paso: Claude vuelve a leer `CLAUDE.md` solo.
- **Para estudiar para la defensa:** "Hazme 5 preguntas sobre ContactoDAO como si fueras mi docente" o "Explícame el recorrido completo de HU-01, desde que toco Guardar hasta SQLite".
- **La bitácora** (`docs/bitacora.md`) se va llenando sola en cada paso; con ella escribimos la sección 5.2 del informe.
- No aceptes cambios que no entiendas: pregunta primero. En la defensa el docente te pedirá que expliques el código.

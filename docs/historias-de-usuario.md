# Historias de usuario · Agenda de Estudio

Prioridad de 100 a 500: un número mayor se desarrolla antes. Estimación en puntos de historia (total: 40).
Solo cuentan como horas estudiadas las sesiones marcadas como "Completada".

## Sprint 0 (04/11 al 15/11): contactos

### HU-01 Registrar contacto · prioridad 500 · 3 puntos · depende de: ninguna
Como estudiante, quiero registrar a mis compañeros y docentes con su nombre, teléfono y correo, para tener sus datos a mano cuando necesite coordinar mis estudios.
Pruebas de aceptación:
- [x] Introducir el nombre "Ana Pérez" y el teléfono "71234567", presionar "Guardar" y comprobar que aparece "Contacto guardado" y que Ana Pérez está en la lista.
- [x] Dejar el nombre vacío, presionar "Guardar" y comprobar que se indica "El nombre es obligatorio" y no se guarda nada.
- [x] Introducir un teléfono que no tiene 7 u 8 dígitos y comprobar que se indica "Ingrese un teléfono válido".
- [x] Introducir un nombre y un teléfono correctos sin correo y comprobar que el contacto se guarda igual.
Criterios de calidad: Usabilidad: el campo teléfono abre el teclado numérico. Eficiencia: el contacto se guarda en menos de 1 segundo.

### HU-02 Ver y buscar contactos · prioridad 480 · 3 puntos · depende de: HU-01
Como estudiante, quiero ver mis contactos ordenados alfabéticamente y buscarlos por nombre, para encontrar rápido a la persona que necesito.
Pruebas de aceptación:
- [x] Abrir "Contactos" con contactos guardados y comprobar que la lista está ordenada de la A a la Z, con nombre y teléfono.
- [x] Escribir "ana" en el buscador y comprobar que solo aparece "Ana Pérez", sin importar mayúsculas o minúsculas.
- [x] Buscar un nombre que no existe y comprobar que se indica "No se encontraron contactos".
- [x] Abrir "Contactos" sin ningún contacto guardado y comprobar que se indica "Aún no tienes contactos".
Criterios de calidad: Eficiencia: la lista y la búsqueda responden en menos de 1 segundo con hasta 500 contactos.

### HU-03 Editar contacto · prioridad 460 · 2 puntos · depende de: HU-01, HU-02
Como estudiante, quiero modificar los datos de un contacto, para mantener su información actualizada.
Pruebas de aceptación:
- [x] Seleccionar a "Ana Pérez", cambiar su teléfono a "76543210", presionar "Guardar" y comprobar que la lista muestra el teléfono nuevo y aparece "Contacto actualizado".
- [x] Borrar el nombre, presionar "Guardar" y comprobar que se indica "El nombre es obligatorio" y se conservan los datos anteriores.
- [x] Presionar "Cancelar" y comprobar que los datos del contacto no cambian.
Criterios de calidad: Usabilidad: el formulario aparece con los datos actuales ya cargados.

### HU-04 Eliminar contacto · prioridad 440 · 1 punto · depende de: HU-02
Como estudiante, quiero eliminar un contacto que ya no necesito, para mantener mi lista ordenada.
Pruebas de aceptación:
- [x] Presionar "Eliminar" en "Ana Pérez" y comprobar que la app pregunta "¿Eliminar a Ana Pérez?" con "Sí" y "No".
- [x] Confirmar con "Sí" y comprobar que el contacto desaparece y aparece "Contacto eliminado".
- [x] Responder "No" y comprobar que el contacto sigue en la lista.
Criterios de calidad: Seguridad: nada se elimina sin confirmación.

## Sprint 1 (16/11 al 20/11): materias y sesiones de estudio

### HU-05 Registrar materia · prioridad 420 · 2 puntos · depende de: ninguna
Como estudiante, quiero registrar mis materias con su nombre, su docente y un color, para organizar mis sesiones de estudio por materia.
Pruebas de aceptación:
- [x] Introducir "Cálculo II", el docente "Lic. Méndez", elegir un color, presionar "Guardar" y comprobar que aparece "Materia guardada" y la materia está en la lista.
- [x] Dejar el nombre vacío, presionar "Guardar" y comprobar que se indica "El nombre es obligatorio".
- [x] Introducir el nombre de una materia que ya existe y comprobar que se indica "Ya existe una materia con ese nombre".
- [x] Guardar una materia sin docente ni color elegido y comprobar que se guarda con un color por defecto.
Criterios de calidad: Usabilidad: los colores se distinguen bien entre sí. Eficiencia: la materia se guarda en menos de 1 segundo.

### HU-06 Ver materias · prioridad 400 · 2 puntos · depende de: HU-05
Como estudiante, quiero ver la lista de mis materias con su color y su docente, para tener presente lo que estoy cursando.
Pruebas de aceptación:
- [x] Abrir "Materias" con materias guardadas y comprobar que se ven ordenadas por nombre, cada una con su color y su docente.
- [x] Abrir "Materias" sin ninguna materia guardada y comprobar que se indica "Aún no tienes materias".
Criterios de calidad: Usabilidad: el color de cada materia se ve junto a su nombre.

### HU-07 Editar materia · prioridad 380 · 2 puntos · depende de: HU-05, HU-06
Como estudiante, quiero modificar el nombre, el docente o el color de una materia, para corregir sus datos.
Pruebas de aceptación:
- [x] Seleccionar "Cálculo II", cambiar el docente a "Lic. Vargas", presionar "Guardar" y comprobar que aparece "Materia actualizada" y la lista muestra el docente nuevo.
- [x] Cambiar el nombre por el de otra materia que ya existe y comprobar que se indica "Ya existe una materia con ese nombre".
- [x] Presionar "Cancelar" y comprobar que la materia no cambia.
Criterios de calidad: Usabilidad: el formulario aparece con los datos actuales ya cargados.

### HU-08 Eliminar materia · prioridad 360 · 2 puntos · depende de: HU-06
Como estudiante, quiero eliminar una materia que ya no curso, para que no aparezca en mi planificación.
Pruebas de aceptación:
- [x] Presionar "Eliminar" en "Cálculo II" y comprobar que la app pregunta "¿Eliminar Cálculo II? También se borrarán sus sesiones" con "Sí" y "No".
- [x] Confirmar con "Sí" y comprobar que la materia desaparece, que sus sesiones ya no están en el calendario y que aparece "Materia eliminada".
- [x] Responder "No" y comprobar que la materia sigue en la lista.
Criterios de calidad: Seguridad: se avisa que también se borrarán las sesiones antes de eliminar.

### HU-09 Planificar sesión de estudio · prioridad 340 · 5 puntos · depende de: HU-05
Como estudiante, quiero planificar una sesión de estudio eligiendo la materia, la fecha y la hora de inicio y de fin, para organizar mi tiempo de estudio.
Pruebas de aceptación:
- [x] Elegir la materia "Cálculo II", la fecha 13/11/2026 y el horario de 19:00 a 20:30, presionar "Guardar" y comprobar que aparece "Sesión guardada" y la sesión está en el calendario de ese día como "Pendiente".
- [x] Presionar "Guardar" sin elegir materia y comprobar que se indica "Elija una materia".
- [x] Elegir una hora de fin igual o anterior a la de inicio y comprobar que se indica "La hora de fin debe ser posterior a la de inicio".
- [x] Tocar el campo de fecha o de hora y comprobar que se abre un selector de calendario o de reloj.
- [x] Abrir "Nueva sesión" sin materias registradas y comprobar que se indica "Primero registre una materia".
- [x] Con una sesión el 12/11/2026 de 08:00 a 09:00, guardar otra ese día de 08:30 a 09:30 (de cualquier materia) y comprobar que se indica "Ya tienes una sesión en ese horario"; guardar otra de 09:00 a 10:00 y comprobar que sí se guarda.
Criterios de calidad: Usabilidad: la fecha y las horas se eligen con selectores, sin escribir a mano. Eficiencia: la sesión se guarda en menos de 1 segundo.

### HU-10 Ver sesiones de la semana · prioridad 320 · 3 puntos · depende de: HU-09
Como estudiante, quiero ver mi semana y las sesiones de cada día ordenadas por hora, para saber qué tengo que estudiar.
Pruebas de aceptación:
- [x] Abrir "Calendario" y comprobar que se ven los 7 días de la semana, marcados los que tienen sesiones, y las sesiones de hoy ordenadas por hora, con materia, horario y estado.
- [x] Tocar el viernes 13/11/2026 y comprobar que solo se ven las sesiones de ese día.
- [x] Tocar un día sin sesiones y comprobar que se indica "No tienes sesiones para este día".
- [x] Pasar a la semana siguiente con ▶ y comprobar que cambian los días.
Criterios de calidad: Usabilidad: el día elegido se ve resaltado. Eficiencia: la lista carga en menos de 1 segundo.

### HU-11 Editar sesión y cambiar su estado · prioridad 300 · 3 puntos · depende de: HU-09, HU-10
Como estudiante, quiero modificar una sesión y marcarla como completada, pendiente o reprogramada, para llevar el control de lo que realmente estudié.
Pruebas de aceptación:
- [x] Seleccionar la sesión de "Cálculo II", marcarla como "Completada", presionar "Guardar" y comprobar que aparece "Sesión actualizada" y el calendario muestra el estado nuevo.
- [x] Cambiar la fecha de la sesión a otro día y comprobar que ya no aparece en el día anterior y sí en el nuevo.
- [x] Poner una hora de fin anterior a la de inicio y comprobar que se indica "La hora de fin debe ser posterior a la de inicio".
- [x] Presionar "Cancelar" y comprobar que la sesión queda igual.
- [x] Cambiar el horario de una sesión para que se cruce con otra del mismo día y comprobar que se indica "Ya tienes una sesión en ese horario"; guardar la sesión sin cambiar su horario y comprobar que no aparece ese mensaje.
Criterios de calidad: Usabilidad: el estado se elige con un toque.

### HU-12 Eliminar sesión · prioridad 280 · 1 punto · depende de: HU-10
Como estudiante, quiero eliminar una sesión que ya no voy a realizar, para que mi planificación sea real.
Pruebas de aceptación:
- [x] Presionar "Eliminar" en la sesión de "Cálculo II" y comprobar que la app pregunta "¿Eliminar la sesión de Cálculo II?" con "Sí" y "No".
- [x] Confirmar con "Sí" y comprobar que la sesión desaparece y aparece "Sesión eliminada".
- [x] Responder "No" y comprobar que la sesión sigue en el calendario.
Criterios de calidad: Seguridad: nada se elimina sin confirmación.

## Sprint 2 (21/11 al 24/11): meta semanal, reporte y estadísticas

### HU-13 Definir meta semanal y ver el progreso · prioridad 260 · 3 puntos · depende de: HU-11
Como estudiante, quiero definir cuántas horas quiero estudiar en la semana y ver mi progreso, para mantenerme constante.
Pruebas de aceptación:
- [x] Introducir 20 horas como meta, presionar "Guardar" y comprobar que la pantalla de inicio muestra el progreso como "horas completadas / 20 h" con una barra.
- [x] Marcar como completada una sesión de 1 h 30 min y comprobar que el progreso aumenta en 1,5 horas.
- [x] Introducir 0 o 100 horas y comprobar que se indica "Ingrese una meta entre 1 y 80 horas".
- [x] Completar las horas de la meta y comprobar que aparece "¡Meta cumplida! Sigue así".
Criterios de calidad: Usabilidad: el progreso se ve en la pantalla de inicio sin entrar a otra pantalla.

### HU-14 Ver reporte semanal · prioridad 240 · 3 puntos · depende de: HU-11
Como estudiante, quiero ver un reporte de la semana con las horas planificadas y completadas por materia, para saber en qué materias cumplí lo que planifiqué.
Pruebas de aceptación:
- [ ] Abrir "Reporte semanal" y comprobar que por cada materia se ven las horas planificadas, las completadas y el porcentaje, y una fila de total.
- [ ] Pasar a la semana anterior con ◀ y comprobar que el reporte cambia a esa semana.
- [ ] Abrir una semana sin sesiones y comprobar que se indica "No hay sesiones en esta semana".
Criterios de calidad: Eficiencia: el reporte se calcula en menos de 1 segundo.

### HU-15 Ver estadísticas · prioridad 220 · 5 puntos · depende de: HU-11, HU-13
Como estudiante, quiero ver estadísticas de mi estudio, para conocer mis avances y en qué materias debo mejorar.
Pruebas de aceptación:
- [ ] Abrir "Estadísticas" y comprobar que se ven las horas estudiadas en la semana y la comparación en porcentaje con la semana anterior.
- [ ] Comprobar que se ve una barra por materia con sus horas, ordenadas de mayor a menor.
- [ ] Dejar una sesión como "Pendiente" y comprobar que sus horas no se suman en las estadísticas.
- [ ] Abrir "Estadísticas" en una semana sin sesiones completadas y comprobar que se indica "Aún no hay horas registradas esta semana".
Criterios de calidad: Usabilidad: cada barra usa el color de su materia. Eficiencia: las estadísticas se calculan en menos de 1 segundo.

## Requisitos no funcionales a cuidar mientras se programa
- Responder en menos de 1 segundo (listas, reporte y estadísticas); funcionar sin internet; los datos no se pierden al cerrar la app.
- La app no se cierra con datos vacíos o incorrectos.
- No pedir permisos (ni internet ni contactos del teléfono).
- Botones de al menos 48dp; mensajes de error claros junto al campo.
- Nada se elimina sin confirmación.

# Historias de usuario · Agenda Personal

Prioridad de 100 a 500: un número mayor se desarrolla antes. Estimación en puntos de historia.

## Sprint 1 (16/11 al 20/11): contactos

### HU-01 Registrar contacto · prioridad 500 · 3 puntos · depende de: ninguna
Como usuario de la agenda, quiero registrar un contacto con su nombre, teléfono y correo, para tener sus datos guardados en un solo lugar.
Pruebas de aceptación:
- [ ] Introducir el nombre "Ana Pérez" y el teléfono "71234567", presionar "Guardar" y comprobar que aparece "Contacto guardado" y que Ana Pérez está en la lista.
- [x] Dejar el nombre vacío, presionar "Guardar" y comprobar que se indica "El nombre es obligatorio" y no se guarda nada.
- [x] Introducir un teléfono que no tiene 7 u 8 dígitos y comprobar que se indica "Ingrese un teléfono válido".
- [x] Introducir un nombre y un teléfono correctos sin correo y comprobar que el contacto se guarda igual.

### HU-02 Ver y buscar contactos · prioridad 450 · 3 puntos · depende de: HU-01
Como usuario de la agenda, quiero ver mis contactos ordenados alfabéticamente y buscarlos por nombre, para encontrar rápido a la persona que necesito.
Pruebas de aceptación:
- [ ] Abrir "Contactos" con contactos guardados y comprobar que la lista está ordenada de la A a la Z, con nombre y teléfono.
- [ ] Escribir "ana" en el buscador y comprobar que solo aparece "Ana Pérez", sin importar mayúsculas o minúsculas.
- [ ] Buscar un nombre que no existe y comprobar que se indica "No se encontraron contactos".
- [ ] Abrir "Contactos" sin ningún contacto guardado y comprobar que se indica "Aún no tienes contactos".

### HU-03 Editar contacto · prioridad 250 · 2 puntos · depende de: HU-01, HU-02
Como usuario de la agenda, quiero modificar los datos de un contacto, para mantener su información actualizada.
Pruebas de aceptación:
- [ ] Seleccionar a "Ana Pérez", cambiar su teléfono a "76543210", presionar "Guardar" y comprobar que la lista muestra el teléfono nuevo y aparece "Contacto actualizado".
- [ ] Borrar el nombre, presionar "Guardar" y comprobar que se indica "El nombre es obligatorio" y se conservan los datos anteriores.
- [ ] Presionar "Cancelar" y comprobar que los datos del contacto no cambian.

### HU-04 Eliminar contacto · prioridad 150 · 1 punto · depende de: HU-02
Como usuario de la agenda, quiero eliminar un contacto que ya no necesito, para mantener mi lista ordenada.
Pruebas de aceptación:
- [ ] Presionar "Eliminar" en "Ana Pérez" y comprobar que la app pregunta "¿Eliminar a Ana Pérez?" con "Sí" y "No".
- [ ] Confirmar con "Sí" y comprobar que el contacto desaparece y aparece "Contacto eliminado".
- [ ] Responder "No" y comprobar que el contacto sigue en la lista.

## Sprint 2 (21/11 al 25/11): eventos

### HU-05 Registrar evento · prioridad 400 · 5 puntos · depende de: ninguna
Como usuario de la agenda, quiero registrar un evento (personal, trabajo, estudio u otro) con título, fecha, hora, lugar y descripción, para no olvidar mis compromisos.
Pruebas de aceptación:
- [ ] Introducir el título "Primer parcial", elegir el tipo "Estudio", la fecha 13/11/2026 y la hora 19:00, presionar "Guardar" y comprobar que aparece "Evento guardado" y el evento está en la lista del 13/11/2026.
- [ ] Dejar el título vacío, presionar "Guardar" y comprobar que se indica "El título es obligatorio".
- [ ] Tocar el campo de fecha o de hora y comprobar que se abre un selector de calendario o de reloj.
- [ ] Guardar un evento sin elegir tipo, lugar ni descripción y comprobar que se guarda con el tipo "Otro".

### HU-06 Ver eventos por fecha · prioridad 350 · 3 puntos · depende de: HU-05
Como usuario de la agenda, quiero ver los eventos de un día elegido, ordenados por hora, para saber qué tengo que hacer ese día.
Pruebas de aceptación:
- [ ] Abrir "Eventos" con eventos para hoy y comprobar que se ven los de hoy ordenados por hora, con título, tipo, hora y lugar.
- [ ] Elegir el 13/11/2026 en la tira de días y comprobar que solo se ven los eventos de ese día.
- [ ] Elegir un día sin eventos y comprobar que se indica "No tienes eventos para este día".

### HU-07 Editar evento · prioridad 200 · 3 puntos · depende de: HU-05, HU-06
Como usuario de la agenda, quiero modificar un evento ya registrado, para corregirlo cuando cambian mis planes.
Pruebas de aceptación:
- [ ] Seleccionar "Primer parcial", cambiar la hora a 20:00, presionar "Guardar" y comprobar que la lista muestra la hora nueva y aparece "Evento actualizado".
- [ ] Cambiar la fecha del evento a otro día y comprobar que ya no aparece en el día anterior y sí en el nuevo.
- [ ] Presionar "Cancelar" y comprobar que el evento queda igual que antes.

### HU-08 Eliminar evento · prioridad 100 · 1 punto · depende de: HU-06
Como usuario de la agenda, quiero eliminar un evento que ya no se realizará, para que mi agenda muestre solo lo que es real.
Pruebas de aceptación:
- [ ] Presionar "Eliminar" en "Primer parcial" y comprobar que la app pregunta "¿Eliminar el evento Primer parcial?" con "Sí" y "No".
- [ ] Confirmar con "Sí" y comprobar que el evento desaparece y aparece "Evento eliminado".
- [ ] Responder "No" y comprobar que el evento sigue en la lista.

## Requisitos no funcionales a cuidar mientras se programa
- Responder en menos de 1 segundo; funcionar sin internet; los datos no se pierden al cerrar la app.
- La app no se cierra con datos vacíos o incorrectos.
- No pedir permisos (ni internet ni contactos del teléfono).
- Botones de al menos 48dp; mensajes de error claros junto al campo.

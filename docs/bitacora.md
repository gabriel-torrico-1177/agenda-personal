# Bitácora de desarrollo

| Fecha | Historia | Qué se hizo | Problemas y cómo se resolvieron |
|---|---|---|---|
| 11/11/2026 | Configuración | Se inició el repositorio Git, se creó README.md, commit "Proyecto inicial", rama main y se subió a GitHub (gabriel-torrico-1177/agenda-personal) por SSH. Se agregó la regla de trabajo con ramas feature/hu-XX a CLAUDE.md. | Git no tenía nombre ni correo: se configuró con git config --global. SSH fallaba ("Host key verification failed" y luego "Permission denied (publickey)"): se aceptó la huella de GitHub, se creó una clave ed25519 y se agregó en GitHub → Settings → SSH keys. |
| 11/11/2026 | HU-01 (paso 1) | Rama feature/hu-01-registrar-contacto. Se crearon los paquetes vista, controlador, modelo y datos (arquitectura en 3 capas) y se movió MainActivity al paquete vista; se actualizó AndroidManifest (.vista.MainActivity). Compila con assembleDebug. | Al mover MainActivity hubo que importar com.gabriel.agenda.R, porque R se genera en el paquete base. Git no guarda carpetas vacías: se agregó un archivo .gitkeep en controlador, modelo y datos. |

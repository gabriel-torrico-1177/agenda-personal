package com.gabriel.agenda.controlador;

import android.content.Context;

import com.gabriel.agenda.datos.ContactoDAO;
import com.gabriel.agenda.modelo.Contacto;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// Controlador: valida los datos de un contacto y le pide al DAO que los guarde o los lea
public class ControladorContacto {

    // Mensajes que la pantalla muestra (públicos para poder compararlos desde la vista)
    public static final String ERROR_NOMBRE = "El nombre es obligatorio";
    public static final String ERROR_TELEFONO = "Ingrese un teléfono válido";
    public static final String ERROR_CORREO = "Ingrese un correo válido";
    public static final String CONTACTO_GUARDADO = "Contacto guardado";
    public static final String CONTACTO_ACTUALIZADO = "Contacto actualizado";
    public static final String CONTACTO_ELIMINADO = "Contacto eliminado";
    public static final String ERROR_GUARDAR = "No se pudo guardar";

    private final ContactoDAO contactoDAO;

    // Constructor: crea el DAO con el que se habla con la base de datos
    public ControladorContacto(Context context) {
        contactoDAO = new ContactoDAO(context);
    }

    // Valida y guarda un contacto nuevo. Devuelve el mensaje para la pantalla
    public String registrar(String nombre, String telefono, String correo) {
        nombre = limpiar(nombre);
        telefono = limpiar(telefono);
        correo = limpiar(correo);

        String error = validar(nombre, telefono, correo);
        if (error != null) {
            return error; // no se guarda nada
        }

        Contacto contacto = new Contacto(nombre, telefono, correo);
        long id = contactoDAO.insertar(contacto);
        if (id == -1) {
            return ERROR_GUARDAR;
        }
        return CONTACTO_GUARDADO;
    }

    // Devuelve todos los contactos ordenados A-Z (en español: sin importar tildes ni mayúsculas)
    public List<Contacto> listar() {
        List<Contacto> contactos = contactoDAO.obtenerTodos();
        ordenarPorNombre(contactos);
        return contactos;
    }

    // Busca por nombre sin importar tildes ni mayúsculas ("perez" encuentra "Ana Pérez").
    // Si el texto está vacío devuelve todos. Siempre ordenados A-Z
    public List<Contacto> buscar(String texto) {
        texto = limpiar(texto);
        List<Contacto> todos = contactoDAO.obtenerTodos();
        if (texto.isEmpty()) {
            ordenarPorNombre(todos);
            return todos;
        }

        // Se compara la versión "simple" (sin tildes y en minúsculas) del texto y del nombre
        String buscado = simplificar(texto);
        List<Contacto> encontrados = new ArrayList<>();
        for (Contacto contacto : todos) {
            if (simplificar(contacto.getNombre()).contains(buscado)) {
                encontrados.add(contacto);
            }
        }
        ordenarPorNombre(encontrados);
        return encontrados;
    }

    // Devuelve el contacto con ese id, o null si no existe
    public Contacto obtener(int id) {
        return contactoDAO.buscarPorId(id);
    }

    // Valida y actualiza un contacto existente. Devuelve el mensaje para la pantalla
    public String actualizar(Contacto contacto) {
        contacto.setNombre(limpiar(contacto.getNombre()));
        contacto.setTelefono(limpiar(contacto.getTelefono()));
        contacto.setCorreo(limpiar(contacto.getCorreo()));

        String error = validar(contacto.getNombre(), contacto.getTelefono(), contacto.getCorreo());
        if (error != null) {
            return error; // no se cambia nada
        }

        int filas = contactoDAO.actualizar(contacto);
        if (filas == 0) {
            return ERROR_GUARDAR;
        }
        return CONTACTO_ACTUALIZADO;
    }

    // Borra el contacto. Devuelve true si se borró
    public boolean eliminar(int id) {
        return contactoDAO.eliminar(id) > 0;
    }

    // Revisa las reglas. Devuelve el mensaje de error, o null si todo está bien
    private String validar(String nombre, String telefono, String correo) {
        if (nombre.isEmpty()) {
            return ERROR_NOMBRE;
        }
        // \\d{7,8} = solo dígitos, entre 7 y 8
        if (!telefono.matches("\\d{7,8}")) {
            return ERROR_TELEFONO;
        }
        // El correo es opcional: solo se revisa si se escribió algo
        if (!correo.isEmpty() && !correo.contains("@")) {
            return ERROR_CORREO;
        }
        return null;
    }

    // Ordena la lista por nombre con las reglas del español: "Álvaro" va junto a "Alberto"
    private void ordenarPorNombre(List<Contacto> contactos) {
        final Collator collator = Collator.getInstance(Locale.forLanguageTag("es"));
        collator.setStrength(Collator.PRIMARY); // PRIMARY: ignora tildes y mayúsculas
        Collections.sort(contactos, new Comparator<Contacto>() {
            @Override
            public int compare(Contacto a, Contacto b) {
                return collator.compare(a.getNombre(), b.getNombre());
            }
        });
    }

    // Quita las tildes y pasa a minúsculas: "Álvaro Pérez" → "alvaro perez"
    private String simplificar(String texto) {
        // NFD separa cada letra de su tilde: "á" pasa a ser "a" + "´"
        String separado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        // \\p{M} = las marcas (tildes, diéresis): se borran y queda solo la letra
        String sinTildes = separado.replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }

    // Quita los espacios de los extremos y convierte null en texto vacío
    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim();
    }
}

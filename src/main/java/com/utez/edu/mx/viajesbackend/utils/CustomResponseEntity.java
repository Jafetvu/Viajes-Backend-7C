package com.utez.edu.mx.viajesbackend.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio personalizado para crear respuestas HTTP con un formato uniforme.
 * Utiliza un cuerpo de respuesta estándar con los campos "message", "status", "code" y opcionalmente "data".
 */
@Service
public class CustomResponseEntity {
    private Map<String, Object> body;

    /**
     * Genera una respuesta con estado 200 (OK) para una solicitud exitosa.
     *
     * @param message Mensaje descriptivo de la respuesta.
     * @param status Estado de la respuesta (por ejemplo, "OK").
     * @param code Código de estado HTTP (200).
     * @param data Datos adicionales que pueden ser enviados en la respuesta (opcional).
     * @return Una respuesta HTTP con estado 200 y los datos proporcionados.
     */
    public ResponseEntity<?> getOkResponse(String message, String status, int code, Object data) {
        body = new HashMap<>();
        body.put("message", message); // Mensaje descriptivo
        body.put("status", status);   // Estado de la respuesta
        body.put("code", code);       // Código de estado HTTP
        if (data != null) {
            body.put("data", data);  // Datos opcionales
        }

        return new ResponseEntity<>(body, HttpStatus.OK); // Retorna ResponseEntity con estado 200
    }

    /**
     * Genera una respuesta con estado 400 (BAD REQUEST) para una solicitud inválida.
     *
     * @param message Mensaje descriptivo del error.
     * @return Una respuesta HTTP con estado 400 y el mensaje de error proporcionado.
     */
    public ResponseEntity<?> get400Response(String message) {
        body = new HashMap<>();
        body.put("message", message); // Mensaje descriptivo de error
        body.put("status", "error");   // Estado de error
        body.put("code", 400);         // Código de estado HTTP (400 - Bad Request)

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST); // Retorna ResponseEntity con estado 400
    }

    /**
     * Genera una respuesta con estado 404 (NOT FOUND) cuando el recurso no es encontrado.
     *
     * @return Una respuesta HTTP con estado 404 y un mensaje por defecto de "Recurso no encontrado".
     */
    public ResponseEntity<?> get404Response() {
        body = new HashMap<>();
        body.put("message", "Recurso no encontrado"); // Mensaje por defecto para no encontrado
        body.put("status", "NOT_FOUND");              // Estado de recurso no encontrado
        body.put("code", 404);                        // Código de estado HTTP (404)

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND); // Retorna ResponseEntity con estado 404
    }
}


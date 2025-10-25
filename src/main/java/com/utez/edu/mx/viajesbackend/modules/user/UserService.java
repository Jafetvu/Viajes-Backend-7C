package com.utez.edu.mx.viajesbackend.modules.user;

import com.utez.edu.mx.viajesbackend.modules.user.DTO.UserDTO;
import com.utez.edu.mx.viajesbackend.utils.CustomResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private CustomResponseEntity customResponseEntity;


    //UserDTO para mostrar solamente ciertos datos en las consultas
    public UserDTO transformUserToDTO(User u){
        return new UserDTO(
                u.getId(),
                u.getName(),
                u.getSurname(),
                u.getLastname(),
                u.getEmail(),
                u.getRol(),
                u.getUsername(),
                u.getPhoneNumber()
        );
    }


    //Buscar a todos los usuarios
    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll(){
        List<UserDTO> list = new ArrayList<>();
        String message = "";

        if(userRepository.findAll().isEmpty()){
            message = "No hay usuarios registrados";
        }else {
            message = "Usuarios encontrados";
            for(User u : userRepository.findAll()){
                list.add(transformUserToDTO(u));
            }
        }
        return customResponseEntity.getOkResponse(message,"ok",200, list);
    }

    //Buscar usuario por id
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long idUser){
        UserDTO dto = null;
        User found = userRepository.findById(idUser).orElse(null);
        String message = "";

        if(found == null){
            return customResponseEntity.get404Response();
        }else{
            message = "Usuario encontrado";
            dto = transformUserToDTO(found);
            return customResponseEntity.getOkResponse(message,"ok",200, dto);
        }

    }
    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<?> save(User user){
        try{
            // Unicidad de email
            if (userRepository.existsByEmail(user.getEmail())) {
                return customResponseEntity.get400Response("No se puede ocupar este correo");
            }
            // Unicidad de teléfono
            if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
                return customResponseEntity.get400Response("No se puede ocupar este teléfono");
            }

            // Generar username automáticamente (sin normalizar email/phone)
            String baseUsername = buildBaseUsername(user.getName(), user.getSurname(), user.getLastname());
            String uniqueUsername = ensureUniqueUsername(baseUsername);
            user.setUsername(uniqueUsername);

            userRepository.save(user);
            return customResponseEntity.getOkResponse("Usuario guaradado correctamente", "ok", 200, null);

        } catch(Exception e){
            e.printStackTrace();
            return customResponseEntity.get400Response("BAD_REQUEST");
        }
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<?> update(User user) {
        User found = userRepository.findById(user.getId());
        if (found == null) {
            return customResponseEntity.get404Response();
        }

        // Permite el mismo email/teléfono del propio usuario, rechaza si es de otro
        if (user.getEmail() != null &&
                userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
            return customResponseEntity.get400Response("No se puede actualizar este correo");
        }
        if (user.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumberAndIdNot(user.getPhoneNumber(), user.getId())) {
            return customResponseEntity.get400Response("No se puede actualizar este teléfono");
        }

        try {
            // Si no mandan password, conserva la actual
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(found.getPassword());
            }

            // Asegura que el id se mantenga (por si llega nulo en el body)
            user.setId(found.getId());

            //Se pone el status en true cuando es usuario cliente
            user.setStatus(true);

            // (Opcional) Si permites actualizar nombre/apellidos y quieres
            // mantener username en sync solo al crear, no toques username aquí.
            // Si quisieras recalcularlo en update, hazlo con cautela por colisiones.

            userRepository.save(user);
            return customResponseEntity.getOkResponse("Usuario modificado correctamente", "ok", 200, null);
        } catch (Exception e) {
            e.printStackTrace();
            return customResponseEntity.get400Response("BAD_REQUEST");
        }
    }

    /* ===== Helpers para username (no tocan email/phone) ===== */

    private String buildBaseUsername(String name, String surname, String lastname) {
        String firstName = firstToken(name);
        String firstSurname = firstToken(surname);
        String firstLastName = firstToken(lastname);

        StringBuilder sb = new StringBuilder();
        if (!firstName.isEmpty()) sb.append(firstName.toLowerCase());
        if (!firstSurname.isEmpty()) {
            if (sb.length() > 0) sb.append(".");
            sb.append(firstSurname.toLowerCase());
        }
        if (!firstLastName.isEmpty()) {
            if (sb.length() > 0) sb.append(".");
            sb.append(firstLastName.toLowerCase());
        }

        String slug = slugify(sb.toString());
        if (slug.isEmpty()) slug = "user";
        if (slug.length() > 30) slug = slug.substring(0, 30);
        return slug;
    }

    private String ensureUniqueUsername(String base) {
        String candidate = base;
        int suffix = 2;
        while (userRepository.existsByUsername(candidate)) {
            String next = base + suffix;
            if (next.length() > 30) {
                int maxBase = Math.max(1, 30 - String.valueOf(suffix).length());
                next = base.substring(0, Math.min(base.length(), maxBase)) + suffix;
            }
            candidate = next;
            suffix++;
        }
        return candidate;
    }

    private String firstToken(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return "";
        String token = trimmed.split("\\s+")[0];
        return slugify(token);
    }

    private String slugify(String input) {
        if (input == null) return "";
        String n = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        String noAccents = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Permite solo letras, números y puntos para username
        return noAccents.replaceAll("[^A-Za-z0-9.]", "");
    }




    //Eliminar usuarios
    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<?> deleteById(User user){
        if(userRepository.findById(user.getId()) == null){
            return customResponseEntity.get404Response();
        }else{
            try{
                userRepository.deleteById(user.getId());
                return customResponseEntity.getOkResponse("Usuario eliminado correctamente", "ok", 200, null);
            }catch(Exception e){
                e.printStackTrace();
                return customResponseEntity.get400Response("BAD_REQUEST");
            }
        }
    }

}

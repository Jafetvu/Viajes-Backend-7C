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
                u.getRol()
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

    //Guardar usuarios
    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<?> save(User user){
        try{
            //Verficar si el correo ya existe
            if(userRepository.existsByEmail(user.getEmail())){
                return customResponseEntity.get400Response("No se puede ocupar este correo");

            }

            userRepository.save(user);
            return customResponseEntity.getOkResponse("Usuario guaradado correctamente", "ok", 200, null);


        }catch(Exception e){
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

        // Normaliza por si vienen espacios o mayúsculas
        String newEmail = user.getEmail() == null ? null : user.getEmail().trim();
        String currentEmail = found.getEmail() == null ? null : found.getEmail().trim();

        boolean emailChanged = newEmail != null && !newEmail.equalsIgnoreCase(currentEmail);

        if (emailChanged && userRepository.existsByEmail(newEmail)) {
            return customResponseEntity.get400Response("No se puede actualizar este correo");
        }

        try {
            // Si no mandan password, conserva la actual
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(found.getPassword());
            }

            // Asegura que el id se mantenga (por si llega nulo en el body)
            user.setId(found.getId());

            userRepository.save(user);
            return customResponseEntity.getOkResponse("Usuario modificado correctamente", "ok", 200, null);
        } catch (Exception e) {
            e.printStackTrace();
            return customResponseEntity.get400Response("BAD_REQUEST");
        }
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

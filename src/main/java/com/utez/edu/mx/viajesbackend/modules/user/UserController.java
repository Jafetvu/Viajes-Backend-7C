package com.utez.edu.mx.viajesbackend.modules.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    //ENDPOINTS

    //Traer a todos los usuarios
    @GetMapping("")
    public ResponseEntity<?> findAll() {
        return userService.findAll();
    }

    //Traer usuario por id
    @GetMapping("/{idUser}")
    public ResponseEntity<?> findById(@PathVariable long idUser) {
        return userService.findById(idUser);
    }

    //Guardar usuarios
    @PostMapping("")
    public ResponseEntity<?> save(@RequestBody User user) {
        return userService.save(user);
    }

    //Actualizar usuarios
    @PutMapping("")
    public ResponseEntity<?> update(@RequestBody User user) {
        return userService.update(user);
    }

    //Eliminar usuarios
    @DeleteMapping("")
    public ResponseEntity<?> deleteById(@RequestBody User user) {
        return userService.deleteById(user);
    }
}

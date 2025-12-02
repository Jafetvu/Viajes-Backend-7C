package com.utez.edu.mx.viajesbackend.modules.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Get all users without pagination
    @GetMapping("")
    public ResponseEntity<?> findAll() {
        return userService.findAll();
    }

    // Get users with pagination and filters
    @GetMapping("/paginated")
    public ResponseEntity<?> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String search) {
        return userService.findAllPaginated(page, size, roleId, status, search);
    }

    // Get user by id
    @GetMapping("/{idUser}")
    public ResponseEntity<?> findById(@PathVariable long idUser) {
        return userService.findById(idUser);
    }

    // Save user
    @PostMapping("")
    public ResponseEntity<?> save(@RequestBody User user) {
        return userService.save(user);
    }

    // Update user
    @PutMapping("")
    public ResponseEntity<?> update(@RequestBody User user) {
        return userService.update(user);
    }

    // Delete user
    @DeleteMapping("")
    public ResponseEntity<?> deleteById(@RequestBody User user) {
        return userService.deleteById(user);
    }
}

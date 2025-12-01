package com.utez.edu.mx.viajesbackend.utils;

import com.utez.edu.mx.viajesbackend.modules.role.Role;
import com.utez.edu.mx.viajesbackend.modules.role.RoleRepository;
import com.utez.edu.mx.viajesbackend.modules.user.User;
import com.utez.edu.mx.viajesbackend.modules.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("CLIENTE");
        createRoleIfNotFound("CONDUCTOR");

        createAdminUserIfNotFound();
    }

    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
            System.out.println("Role created: " + name);
        }
    }

    private void createAdminUserIfNotFound() {
        String email = "admin@admin.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            Optional<Role> adminRole = roleRepository.findByName("ADMIN");
            if (adminRole.isPresent()) {
                User user = new User(
                        "Nélida",
                        "Barón",
                        "Pérez",
                        email,
                        adminRole.get(),
                        passwordEncoder.encode("Password123"),
                        true,
                        "7771234567",
                        "admin"
                );
                userRepository.save(user);
                System.out.println("Admin user created: " + email);
            } else {
                System.out.println("ADMIN role not found, cannot create admin user.");
            }
        }
    }
}

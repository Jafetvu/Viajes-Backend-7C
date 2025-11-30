package com.utez.edu.mx.viajesbackend.utils;

import com.utez.edu.mx.viajesbackend.modules.role.Role;
import com.utez.edu.mx.viajesbackend.modules.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("CLIENTE");
        createRoleIfNotFound("CONDUCTOR");
    }

    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
            System.out.println("Role created: " + name);
        }
    }
}

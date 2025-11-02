package com.utez.edu.mx.viajesbackend.modules.user;

import com.utez.edu.mx.viajesbackend.modules.role.Role;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    // Campo de nombre
    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^([A-ZÁÉÍÓÚÑ]{1}[a-záéíóúñ]+\\s*)*$", message = "El nombre debe comenzar con mayúscula y solo puede contener letras")
    @Column(name = "name", nullable = false)
    private String name;

    // Campo de apellido paterno
    @NotBlank(message = "El apellido paterno es obligatorio")
    @Pattern(regexp = "^([A-ZÁÉÍÓÚÑ]{1}[a-záéíóúñ]+\\s*)*$", message = "El apellido paterno debe comenzar con mayúscula y solo puede contener letras")
    @Column(name = "surname", nullable = false)
    private String surname;

    // Campo de apellido materno
    @Pattern(regexp = "^([A-ZÁÉÍÓÚÑ]{1}[a-záéíóúñ]+\\s*)*$", message = "El apellido materno debe comenzar con mayúscula y solo puede contener letras")
    @Column(name = "lastname")
    private String lastname;

    // Campo de username
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Column(name = "username", nullable = false)
    private String username;

    // Campo de correo electronico
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // Campo de número de celular
    @NotBlank(message = "El número telefónico es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El número telefónico debe tener exactamente 10 dígitos")
    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    // Campo de contraseña
    @Column(name = "password", nullable = false)
    private String password;

    // Campo de estado del usuario
    @Column(name = "status")
    private boolean status;

    // Relación con rol

    @ManyToOne
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    // Constructores
    public User() {
    }

    public User(String name, String surname, String lastname, String email, String password, boolean status,
            String phoneNumber, String username) {
        this.name = name;
        this.surname = surname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.username = username;
    }

    public User(long id, String name, String surname, String lastname, String email, String password, boolean status,
            String phoneNumber, String username) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.username = username;
    }

    public User(String name, String surname, String lastname, String email, Role role, String password, boolean status,
            String phoneNumber, String username) {
        this.name = name;
        this.surname = surname;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
        this.password = password;
        this.status = status;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    public User(long id, String name, String surname, String lastname, String email, String password, boolean status,
            Role role, String phoneNumber, String username) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.lastname = lastname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.status = status;
        this.role = role;
        this.username = username;
    }

    // Getters y setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

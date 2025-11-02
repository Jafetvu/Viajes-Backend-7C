package com.utez.edu.mx.viajesbackend.modules.user.DTO;

import com.utez.edu.mx.viajesbackend.modules.role.Role;

// DATA TRANSFER OBJECT (DTO): PLANTILLA DE TRANSFERENCIA PARA RECUPERAR USUARIOS
public class UserDTO {

    private long id;
    private String name, surname, lastname, email, username, phoneNumber;
    private boolean status;
    private Role role;

    public UserDTO(long id, String name, String surname, String lastname, String email, Role role, String username, String phoneNumber, boolean status) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.role = role;
    }


    //Getters y setters

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

    public Role getRole() {
        return role;
    }

    public void setRol(Role rol) {
        this.role = rol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}

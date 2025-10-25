package com.utez.edu.mx.viajesbackend.modules.user.DTO;

import com.utez.edu.mx.viajesbackend.modules.role.Rol;

// DATA TRANSFER OBJECT (DTO): PLANTILLA DE TRANSFERENCIA PARA RECUPERAR USUARIOS
public class UserDTO {

    private long id;
    private String name, surname, lastname, email, username, phoneNumber;
    private Rol rol;

    public UserDTO(long id, String name, String surname, String lastname, String email, Rol rol, String username, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.rol = rol;
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

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
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
}

package com.example.segiii.BDSegi.Entitys;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Usuario")
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    public long id_usuario;

    public String nombre;
    public String apellidos;
    public String nickname;
    public String email;
    public String contrasena;
    public long id_sistema;

    // Getters y setters
    public long getId_usuario() { return id_usuario; }
    public void setId_usuario(long id_usuario) { this.id_usuario = id_usuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public long getId_sistema() { return id_sistema; }
    public void setId_sistema(long id_sistema) { this.id_sistema = id_sistema; }
}
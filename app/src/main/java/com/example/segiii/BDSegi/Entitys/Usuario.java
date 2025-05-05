package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Usuario",
       foreignKeys = @ForeignKey(entity = SistemaNavegacion.class,
       parentColumns = "id_sistema",
       childColumns = "id_sistema",
       onDelete = ForeignKey.CASCADE))
public class Usuario {
    @PrimaryKey (autoGenerate = true)
    public long id_usuario;

    public String nombre;
    public String apellidos;
    public String usuario;
    public String contrasena;
    public long id_sistema;

    public long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public long getId_sistema() {
        return id_sistema;
    }

    public void setId_sistema(long id_sistema) {
        this.id_sistema = id_sistema;
    }
}

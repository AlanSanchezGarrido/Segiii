package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Usuario_Contacto")
public class UsuarioContacto {
    @PrimaryKey(autoGenerate = true)
    public long id_usuariocontacto;
    public long id_usuario;
    public long id_contacto;

    public long getId_usuariocontacto() {
        return id_usuariocontacto;
    }

    public void setId_usuariocontacto(long id_usuariocontacto) {
        this.id_usuariocontacto = id_usuariocontacto;
    }

    public long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public long getId_contacto() {
        return id_contacto;
    }

    public void setId_contacto(long id_contacto) {
        this.id_contacto = id_contacto;
    }
}

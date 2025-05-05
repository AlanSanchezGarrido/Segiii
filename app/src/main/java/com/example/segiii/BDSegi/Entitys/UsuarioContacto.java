package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "Usuario_Contacto",
        primaryKeys = {"id_usuario", "id_contacto"},
        foreignKeys = {@ForeignKey(entity = Usuario.class,
                                  parentColumns = "id_usuario",
                                  childColumns = "id_usuario",
                                   onDelete = ForeignKey.CASCADE),
                      @ForeignKey(entity = Contacto.class,
                      parentColumns = "id_contacto",
                      childColumns = "id_contacto",
                      onDelete = ForeignKey.CASCADE)})
public class UsuarioContacto {
    public long id_usuario;
    public long id_contacto;

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

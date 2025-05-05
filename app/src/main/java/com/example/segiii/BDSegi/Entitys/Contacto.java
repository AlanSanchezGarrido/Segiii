package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Contacto")
public class Contacto {
    @PrimaryKey(autoGenerate = true)
    public long id_contacto;

    public String nombre;
    public String telefono;
    public String relacion;

    public long getId_contacto() {
        return id_contacto;
    }

    public void setId_contacto(long id_contacto) {
        this.id_contacto = id_contacto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRelacion() {
        return relacion;
    }

    public void setRelacion(String relacion) {
        this.relacion = relacion;
    }
}

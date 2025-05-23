package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Sistema_emergencia")
public class SistemaEmergencia {
    @PrimaryKey(autoGenerate = true)
    public long id_sistema_emergencia;

    public String contacto_emergencia;


    public long getId_sistema_emergencia() {
        return id_sistema_emergencia;
    }

    public void setId_sistema_emergencia(long id_sistema_emergencia) {
        this.id_sistema_emergencia = id_sistema_emergencia;
    }

    public String getContacto_emergencia() {
        return contacto_emergencia;
    }

    public void setContacto_emergencia(String contacto_emergencia) {
        this.contacto_emergencia = contacto_emergencia;
    }
}

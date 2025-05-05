package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Sistema_emergencia",
        foreignKeys = {@ForeignKey(entity = Ubicacion.class,
                       parentColumns = "id_ubicacion",
                       childColumns = "id_ubicacion",
                       onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = SistemaNavegacion.class,
                    parentColumns = "id_sistema",
                    childColumns = "id_sistema",
                    onDelete = ForeignKey.CASCADE)})
public class SistemaEmergencia {
    @PrimaryKey(autoGenerate = true)
    public long id_sistema_emergencia;

    public String contacto_emergencia;
    public long id_ubicacion;
    public long id_sistema;

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

    public long getId_ubicacion() {
        return id_ubicacion;
    }

    public void setId_ubicacion(long id_ubicacion) {
        this.id_ubicacion = id_ubicacion;
    }

    public long getId_sistema() {
        return id_sistema;
    }

    public void setId_sistema(long id_sistema) {
        this.id_sistema = id_sistema;
    }
}

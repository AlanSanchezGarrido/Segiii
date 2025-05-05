package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "Rutas_Frecuentes",
        primaryKeys = {"id_usuario", "id_ruta"},
        foreignKeys = {@ForeignKey(entity = Usuario.class,
                       parentColumns = "id_usuario",
                       childColumns = "id_usuario",
                       onDelete = ForeignKey.CASCADE),
                       @ForeignKey(entity = Ruta.class,
                       parentColumns = "id_ruta",
                       childColumns = "id_ruta",
                       onDelete = ForeignKey.CASCADE)})
public class RutasFrecuentes {
    public long id_usuario;
    public long id_ruta;
    public int frecuencia;

    public long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public long getId_ruta() {
        return id_ruta;
    }

    public void setId_ruta(long id_ruta) {
        this.id_ruta = id_ruta;
    }

    public int getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(int frecuencia) {
        this.frecuencia = frecuencia;
    }
}

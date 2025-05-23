package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Rutas_Frecuentes")
public class RutasFrecuentes {
    @PrimaryKey(autoGenerate = true)
    public long id_rutasfrecuentes;
    public long id_usuario;
    public long id_ruta;
    public int frecuencia;

    public long getId_rutasfrecuentes() {
        return id_rutasfrecuentes;
    }

    public void setId_rutasfrecuentes(long id_rutasfrecuentes) {
        this.id_rutasfrecuentes = id_rutasfrecuentes;
    }

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

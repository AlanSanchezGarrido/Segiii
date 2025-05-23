package com.example.segiii.BDSegi.Entitys;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Guia_voz")
public class GuiaVoz {
    @PrimaryKey(autoGenerate = true)
    public long id_guia;
    public int volumen;
    public int velocidad;
    public String comando_voz;


    public long getId_guia() {
        return id_guia;
    }

    public void setId_guia(long id_guia) {
        this.id_guia = id_guia;
    }

    public int getVolumen() {
        return volumen;
    }

    public void setVolumen(int volumen) {
        this.volumen = volumen;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int velocidad) {
        this.velocidad = velocidad;
    }

    public String getComando_voz() {
        return comando_voz;
    }

    public void setComando_voz(String comando_voz) {
        this.comando_voz = comando_voz;
    }

}

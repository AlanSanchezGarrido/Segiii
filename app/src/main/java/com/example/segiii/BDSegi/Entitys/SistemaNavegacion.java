package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Sistema_navegacion")
public class SistemaNavegacion {
    @PrimaryKey(autoGenerate = true)
    public long id_sistema;

    public String nivel_detalle;


    public long getId_sistema() {
        return id_sistema;
    }

    public void setId_sistema(long id_sistema) {
        this.id_sistema = id_sistema;
    }

    public String getNivel_detalle() {
        return nivel_detalle;
    }

    public void setNivel_detalle(String nivel_detalle) {
        this.nivel_detalle = nivel_detalle;
    }

}

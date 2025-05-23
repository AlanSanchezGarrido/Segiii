package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Ubicacion")
public class Ubicacion {
    @PrimaryKey(autoGenerate = true)
    public long id_ubicacion;

    public String placeid;

    public String nombre;
    public double latitud;
    public double longitud;

    public String getPlaceid() {
        return placeid;
    }

    public void setPlaceid(String placeid) {
        this.placeid = placeid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public long getId_ubicacion() {
        return id_ubicacion;
    }

    public void setId_ubicacion(long id_ubicacion) {
        this.id_ubicacion = id_ubicacion;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

}

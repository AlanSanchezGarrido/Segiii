package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Ubicacion",
        foreignKeys = @ForeignKey(entity = SistemaNavegacion.class,
        parentColumns = "id_sistema",
        childColumns = "id_sistema",
        onDelete = ForeignKey.CASCADE))
public class Ubicacion {
    @PrimaryKey(autoGenerate = true)
    public long id_ubicacion;

    public String nombre;
    public double latitud;
    public double longitud;
    public long id_sistema;

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

    public long getId_sistema() {
        return id_sistema;
    }

    public void setId_sistema(long id_sistema) {
        this.id_sistema = id_sistema;
    }
}

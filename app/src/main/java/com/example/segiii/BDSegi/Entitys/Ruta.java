package com.example.segiii.BDSegi.Entitys;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity (tableName = "Ruta",
         foreignKeys = @ForeignKey(entity = SistemaNavegacion.class,
         parentColumns = "id_sistema",
         childColumns = "id_sistema",
         onDelete = ForeignKey.CASCADE))
public class Ruta {
    @PrimaryKey(autoGenerate = true)
    public long id_ruta;

    public String origen;
    public String destino;
    public double distancia;
    public int tiempo_estimado;
    public long id_sistema;

    public long getId_ruta() {
        return id_ruta;
    }

    public void setId_ruta(long id_ruta) {
        this.id_ruta = id_ruta;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public int getTiempo_estimado() {
        return tiempo_estimado;
    }

    public void setTiempo_estimado(int tiempo_estimado) {
        this.tiempo_estimado = tiempo_estimado;
    }

    public long getId_sistema() {
        return id_sistema;
    }

    public void setId_sistema(long id_sistema) {
        this.id_sistema = id_sistema;
    }
}

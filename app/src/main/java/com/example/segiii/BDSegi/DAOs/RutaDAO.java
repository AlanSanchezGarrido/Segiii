package com.example.segiii.BDSegi.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.segiii.BDSegi.Entitys.Ruta;

import java.util.List;

@Dao
public interface RutaDAO {
    @Insert
    void insert (Ruta ruta);
    @Update
    void update (Ruta ruta);
    @Delete
    void delete (Ruta ruta);
    @Query("SELECT * FROM Ruta")
    List<Ruta> getallRutas();
    @Query("SELECT * FROM Ruta WHERE id_ruta = :id")
    Ruta getRutaById (long id);
    @Query("SELECT * FROM Ruta WHERE id_sistema = :sistemaid")
    List<Ruta>getRutasBysistemaId (long sistemaid);
}

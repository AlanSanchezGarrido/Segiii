package com.example.segiii.BDSegi.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.segiii.BDSegi.Entitys.Ubicacion;

import java.util.List;

@Dao
public interface UbicacionDAO {
    @Insert
    void insert(Ubicacion ubicacion);
    @Update
    void update(Ubicacion ubicacion);
    @Delete
    void delete(Ubicacion ubicacion);
    @Query("SELECT * FROM Ubicacion")
    List<Ubicacion>getallUbicaciones();
    @Query("SELECT * FROM Ubicacion WHERE id_ubicacion = :id")
    Ubicacion getUbicacionesById(long id);
    @Query("SELECT * FROM ubicacion WHERE nombre = :nombreUbicacion ")
    Ubicacion getUbicacionByNombre(String nombreUbicacion);
}

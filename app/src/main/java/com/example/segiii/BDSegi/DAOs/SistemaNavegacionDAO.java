package com.example.segiii.BDSegi.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.segiii.BDSegi.Entitys.SistemaNavegacion;

import java.util.List;

@Dao
public interface SistemaNavegacionDAO {
    @Insert
    void insert (SistemaNavegacion sistemaNavegacion);
    @Update
    void update (SistemaNavegacion sistemaNavegacion);
    @Delete
    void delete (SistemaNavegacion sistemaNavegacion);
    @Query("SELECT * FROM Sistema_navegacion")
    List <SistemaNavegacion> getallSistemaNavegacion();
    @Query("SELECT * FROM sistema_navegacion WHERE id_sistema = :id")
    SistemaNavegacion getSistemaNavegacionById (long id);
}

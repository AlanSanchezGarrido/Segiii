package com.example.segiii.BDSegi.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.segiii.BDSegi.Entitys.SistemaEmergencia;

import java.util.List;

@Dao
public interface SistemaEmergenciaDAO {
    @Insert
    void insert(SistemaEmergencia sistemaEmergencia);
    @Update
    void update(SistemaEmergencia sistemaEmergencia);
    @Delete
    void delete(SistemaEmergencia sistemaEmergencia);
    @Query("SELECT * FROM sistema_emergencia")
    List<SistemaEmergencia>getallsistemasEmergencia();
    @Query("SELECT * FROM Sistema_emergencia WHERE id_sistema_emergencia = :id")
    SistemaEmergencia getSistemasEmergenciaById (long id);

}

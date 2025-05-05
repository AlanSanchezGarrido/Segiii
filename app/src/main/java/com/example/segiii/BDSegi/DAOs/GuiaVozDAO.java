package com.example.segiii.BDSegi.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.segiii.BDSegi.Entitys.GuiaVoz;

import java.util.List;

@Dao
public interface GuiaVozDAO {
    @Insert
    void insert(GuiaVoz guiaVoz);
    @Update
    void update(GuiaVoz guiaVoz);
    @Delete
    void delete (GuiaVoz guiaVoz);
    @Query("SELECT * FROM Guia_voz")
    List<GuiaVoz> getallGuiavoz();
    @Query("SELECT * FROM Guia_voz WHERE id_guia = :id")
    GuiaVoz getGuiavozById (long id);
    @Query("SELECT * FROM Guia_voz WHERE id_usuario = :usuarioid")
    List<GuiaVoz> getGuiavozByusuarioId(long usuarioid);
}

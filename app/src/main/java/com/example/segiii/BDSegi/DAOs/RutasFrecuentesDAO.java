package com.example.segiii.BDSegi.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.segiii.BDSegi.Entitys.Ruta;
import com.example.segiii.BDSegi.Entitys.RutasFrecuentes;

import java.util.List;

@Dao
public interface RutasFrecuentesDAO {
    @Insert
    void insert(RutasFrecuentes rutasFrecuentes);
    @Delete
    void delete(RutasFrecuentes rutasFrecuentes);
    @Query("SELECT * FROM Rutas_Frecuentes")
    List<RutasFrecuentes>getallRutasFrecuentes();
    @Query("SELECT * FROM Ruta WHERE id_ruta IN"+
           "(SELECT id_ruta FROM Rutas_Frecuentes WHERE id_usuario = :usuarioId)")
    List<Ruta>getRutasFrecuentesForUsuario(long usuarioId);
    @Query("SELECT * FROM Rutas_Frecuentes WHERE id_usuario = :usuarioId AND id_ruta= :rutaId")
    RutasFrecuentes getRutaFrecuente(long usuarioId, long rutaId);
}

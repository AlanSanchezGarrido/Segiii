package com.example.segiii.BDSegi.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.segiii.BDSegi.Entitys.Contacto;

import java.util.List;

@Dao
public interface ContactoDAO {
    @Insert
    void insert(Contacto contacto);
    @Update
    void update(Contacto contacto);
    @Delete
    void delete(Contacto contacto);
    @Query("SELECT * FROM Contacto")
    List<Contacto>getallContactos();
    @Query("SELECT * FROM Contacto WHERE id_contacto = :id")
    Contacto getContactosById(long id);
}

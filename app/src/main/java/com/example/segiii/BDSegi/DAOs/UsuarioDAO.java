package com.example.segiii.BDSegi.DAOs;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.segiii.BDSegi.Entitys.Usuario;

import java.util.List;

@Dao
public interface UsuarioDAO {
    @Insert
    void insert (Usuario usuario);
    @Update
    void update(Usuario usuario);
    @Delete
    void delete (Usuario usuario);
    @Query("SELECT * FROM Usuario")
    List <Usuario> getAllUsuarios();
    @Query("SELECT * FROM Usuario WHERE id_usuario = :id")
    Usuario getUsuarioById (long id);
    @Query("SELECT * FROM Usuario WHERE usuario = :username")
    Usuario getUsuarioByUsername(String username);




}

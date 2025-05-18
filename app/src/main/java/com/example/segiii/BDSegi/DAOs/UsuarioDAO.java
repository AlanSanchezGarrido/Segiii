package com.example.segiii.BDSegi.DAOs;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import com.example.segiii.BDSegi.Entitys.Usuario;

import java.util.List;

@Dao
public interface UsuarioDAO {
    @Insert
    void insert(Usuario usuario);

    @Update
    void update(Usuario usuario);

    @Query("SELECT * FROM Usuario WHERE id_usuario = :id")
    Usuario getUsuarioById(long id);

    @Query("SELECT * FROM Usuario WHERE email = :email")
    Usuario getUsuarioByEmail(String email);

    @Query("SELECT * FROM Usuario WHERE nickname = :nickname")
    Usuario getUsuarioByNickname(String nickname);

    @Query("SELECT * FROM Usuario")
    List<Usuario> getAllUsuarios();


}

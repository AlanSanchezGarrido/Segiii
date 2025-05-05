package com.example.segiii.BDSegi.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.example.segiii.BDSegi.Entitys.Contacto;
import com.example.segiii.BDSegi.Entitys.Usuario;
import com.example.segiii.BDSegi.Entitys.UsuarioContacto;

import java.util.List;

@Dao
public interface UsuarioContactoDAO {
    @Insert
    void insert(UsuarioContacto usuarioContacto);
    @Delete
    void delete (UsuarioContacto usuarioContacto);
    @Query("SELECT * FROM Usuario_Contacto")
    List<UsuarioContacto> getallSistemasNavegion();
    @Query("SELECT * FROM Contacto WHERE id_contacto IN"+
            "(SELECT id_contacto FROM Usuario_Contacto WHERE id_usuario = :usuarioId)")
    List<Contacto>getContactoforsuarios (long usuarioId);
    @Query("SELECT * FROM Usuario WHERE id_usuario IN"+
            "(SELECT id_usuario FROM Usuario_Contacto WHERE id_contacto = :contactoId)")
    List<Usuario>getContactoForContacto(long contactoId);
}

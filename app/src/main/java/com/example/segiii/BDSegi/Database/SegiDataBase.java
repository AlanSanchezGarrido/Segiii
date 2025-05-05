package com.example.segiii.BDSegi.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.segiii.BDSegi.DAOs.ContactoDAO;
import com.example.segiii.BDSegi.DAOs.GuiaVozDAO;
import com.example.segiii.BDSegi.DAOs.RutaDAO;
import com.example.segiii.BDSegi.DAOs.RutasFrecuentesDAO;
import com.example.segiii.BDSegi.DAOs.SistemaEmergenciaDAO;
import com.example.segiii.BDSegi.DAOs.SistemaNavegacionDAO;
import com.example.segiii.BDSegi.DAOs.UbicacionDAO;
import com.example.segiii.BDSegi.DAOs.UsuarioContactoDAO;
import com.example.segiii.BDSegi.DAOs.UsuarioDAO;
import com.example.segiii.BDSegi.Entitys.Contacto;
import com.example.segiii.BDSegi.Entitys.GuiaVoz;
import com.example.segiii.BDSegi.Entitys.Ruta;
import com.example.segiii.BDSegi.Entitys.RutasFrecuentes;
import com.example.segiii.BDSegi.Entitys.SistemaEmergencia;
import com.example.segiii.BDSegi.Entitys.SistemaNavegacion;
import com.example.segiii.BDSegi.Entitys.Ubicacion;
import com.example.segiii.BDSegi.Entitys.Usuario;
import com.example.segiii.BDSegi.Entitys.UsuarioContacto;

@Database(entities = {
        SistemaNavegacion.class,
        Contacto.class,
        Usuario.class,
        Ruta.class,
        GuiaVoz.class,
        UsuarioContacto.class,
        Ubicacion.class,
        SistemaEmergencia.class,
        RutasFrecuentes.class
}, version = 1, exportSchema = false)
public abstract class SegiDataBase extends RoomDatabase {
    public abstract UsuarioDAO usuarioDAO();
    public abstract SistemaNavegacionDAO sistemaNavegacionDAO();
    public abstract RutaDAO rutaDAO();
    public abstract GuiaVozDAO guiaVozDAO();
    public abstract ContactoDAO contactoDAO();
    public abstract UsuarioContactoDAO usuarioContactoDAO();
    public abstract UbicacionDAO ubicacionDAO();
    public abstract SistemaEmergenciaDAO sistemaEmergenciaDAO();
    public abstract RutasFrecuentesDAO rutasFrecuentesDAO();

    private static volatile SegiDataBase INSTANCE;

    public static SegiDataBase getDatabase(final Context context){
        if (INSTANCE==null){
            synchronized (SegiDataBase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SegiDataBase.class, "segi_database").build();
                }
            }

        }
        return INSTANCE;
    }


}

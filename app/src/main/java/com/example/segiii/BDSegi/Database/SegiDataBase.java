package com.example.segiii.BDSegi.Database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.segiii.BDSegi.DAOs.*;
import com.example.segiii.BDSegi.Entitys.*;

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
}, version = 2, exportSchema = false)
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

    public static SegiDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SegiDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    SegiDataBase.class, "segi_database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Usuario ADD COLUMN email TEXT");
            database.execSQL("ALTER TABLE Usuario ADD COLUMN nickname TEXT");
            database.execSQL("UPDATE Usuario SET nickname = usuario");
        }
    };
}
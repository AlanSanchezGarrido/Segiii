package com.example.segiii;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.segiii.BDSegi.Database.SegiDataBase;
import com.example.segiii.BDSegi.Entitys.SistemaNavegacion;
import com.example.segiii.BDSegi.Entitys.Usuario;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    /*
    private TextView userTextView;
    private Button loadUserButton;
    private Button addUsersButton;
    private SegiDataBase db;
    */


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    /*
        userTextView = findViewById(R.id.userTextView);
        loadUserButton = findViewById(R.id.loadUserButton);
        addUsersButton = findViewById(R.id.addUsersButton);

        // Initialize database
        db = SegiDataBase.getDatabase(this);

        loadUserButton.setOnClickListener(v -> loadUser(2)); // Existing load user functionality

        addUsersButton.setOnClickListener(v -> addTwoUsers());

    */

    }

    /*
    private void loadUser(long userId) {
        // Run database operation on a background thread
        new Thread(() -> {
            // Retrieve user from database
            Usuario usuario = db.usuarioDAO().getUsuarioById(userId);

            // Update UI on the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if (usuario != null) {
                    String userDetails = "ID: " + usuario.getId_usuario() +
                            "Nombre: " + usuario.getNombre() +
                            "Apellidos: " + usuario.getApellidos() +
                            "Usuario: " + usuario.getUsuario() +
                            "Contraseña: " + usuario.getContrasena() +
                            "Sistema ID: " + usuario.getId_sistema();
                    userTextView.setText(userDetails);
                } else {
                    userTextView.setText("User not found!");
                }
            });
        }).start();
    }

    private void addTwoUsers() {
        new Thread(() -> {
            try {
                // Step 1: Ensure a SistemaNavegacion exists (required due to foreign key)
                long sistemaId;
                List<SistemaNavegacion> sistemas = db.sistemaNavegacionDAO().getallSistemaNavegacion();
                if (sistemas.isEmpty()) {
                    SistemaNavegacion sistema = new SistemaNavegacion();
                    sistema.setNivel_detalle("Básico");
                    sistema.setId_proveedor(1);
                    db.sistemaNavegacionDAO().insert(sistema);
                    // Fetch the inserted sistema to get its ID
                    sistemaId = db.sistemaNavegacionDAO().getallSistemaNavegacion().get(0).getId_sistema();
                } else {
                    sistemaId = sistemas.get(0).getId_sistema();
                }

                // Step 2: Create and insert two users
                Usuario usuario1 = new Usuario();
                usuario1.setNombre("Ana");
                usuario1.setApellidos("Gómez");
                usuario1.setUsuario("anagomez");
                usuario1.setContrasena("password123");
                usuario1.setId_sistema(sistemaId);

                Usuario usuario2 = new Usuario();
                usuario2.setNombre("Carlos");
                usuario2.setApellidos("López");
                usuario2.setUsuario("carloslopez");
                usuario2.setContrasena("secure456");
                usuario2.setId_sistema(sistemaId);

                // Insert users into the database
                db.usuarioDAO().insert(usuario1);
                db.usuarioDAO().insert(usuario2);

                // Step 3: Show success message on the main thread
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Toast.makeText(MainActivity.this, "Two users added successfully!", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                // Handle errors (e.g., foreign key violation, database issues)
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Toast.makeText(MainActivity.this, "Error adding users: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

     */
}
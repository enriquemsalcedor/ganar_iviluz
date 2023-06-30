package com.example.iviluz_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.iviluz_app.modelos.Ganar;
import com.example.iviluz_app.ui.ganar.GanarFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import utilidades.LoadingDialog;

public class SesionActivity extends AppCompatActivity {

    private EditText txtCorreo;
    private EditText txtClave;
    private Button btnIniciar;
    private ImageButton showHideBtn;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private String email;
    private String clave;
    private String estatus;
    private boolean verClave;

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion);

        txtCorreo = (EditText) findViewById(R.id.correo);
        txtClave = (EditText) findViewById(R.id.clave);
        btnIniciar = (Button) findViewById(R.id.iniciar);
        showHideBtn = (ImageButton) findViewById(R.id.showHideBtn);


        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = txtCorreo.getText().toString();
                clave = txtClave.getText().toString();

                if (!email.isEmpty() && !clave.isEmpty()){
                    if(clave.length() >= 6){
                        login();
                    }else{
                        Toast.makeText(SesionActivity.this, "La contraseña debe ser mayor o igual a 6 carácteres.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(SesionActivity.this, "Debes completar los campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        showHideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verClave){
                    txtClave.setTransformationMethod(new PasswordTransformationMethod());
                    showHideBtn.setImageResource(R.drawable.ic_baseline_remove_red_eye_24);
                    verClave = !verClave;
                } else{
                    txtClave.setTransformationMethod(null);
                    showHideBtn.setImageResource(R.drawable.ic_outline_remove_red_eye_24);
                    verClave = !verClave;
                }
            }
        });

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(SesionActivity.this);

    }

    private void login() {

        if (networkInfo != null && networkInfo.isConnected()) {
            loadingDialog.starDialog();
            auth.signInWithEmailAndPassword(email, clave).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        if (!email.equals("adminiviluz@gmail.com")) {
                            getInfoUser(email);
                        }
                        loadingDialog.dismissDialog();
                        startActivity(new Intent(SesionActivity.this, MainActivity.class));
                    }else{
                        loadingDialog.dismissDialog();
                        Toast.makeText(SesionActivity.this, "No se pudo iniciar sesión.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast notificacion=Toast.makeText(this,"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG);
            notificacion.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null){
            startActivity(new Intent(SesionActivity.this, MainActivity.class));
            finish();

        }
    }

    private void getInfoUser(String email){

        SharedPreferences user = getApplicationContext().getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor userInfo = user.edit();

        if (networkInfo != null && networkInfo.isConnected()) {

            firestore.collection("Usuario")
                    .whereEqualTo("email", email.toString())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    if (doc.get("nombre") != null) {
                                        userInfo.putString("idUser", doc.getId());
                                        userInfo.putString("nombreUser", doc.getString("nombre"));
                                        userInfo.putString("telefonoUser", doc.getString("telefono"));
                                        userInfo.putString("estatusUser", doc.getString("estatus"));
                                        userInfo.commit();

                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),"Ha ocurrido un error listado los Servicios",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            loadingDialog.dismissDialog();
            Toast.makeText(this.getApplicationContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
        }



    }

}
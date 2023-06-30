package com.example.iviluz_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import utilidades.LoadingDialog;

public class PerfilEstatusCero extends AppCompatActivity {

    private TextView nombre;
    private TextView email;
    private TextView nivel;
    private TextView lider;
    private TextView telefono;

    private EditText clave;
    private EditText claveConfim;

    private ImageButton showHideBtn;
    private ImageButton showHideBtn2;

    private Button actualizar;
    private Button cancelar;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private String idLider;
    private boolean verClave;
    private boolean verClaveConf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_estatus_cero);

        actualizar = (Button) findViewById(R.id.actualizar);
        cancelar = (Button) findViewById(R.id.cancelar);

        nombre = (TextView) findViewById(R.id.fecha);
        email = (TextView) findViewById(R.id.email);
        telefono = (TextView) findViewById(R.id.telefono);
        lider = (TextView) findViewById(R.id.lider);
        nivel = (TextView) findViewById(R.id.nivel);

        showHideBtn = (ImageButton) findViewById(R.id.showHideBtn);
        showHideBtn2 = (ImageButton) findViewById(R.id.showHideBtn2);

        clave = (EditText) findViewById(R.id.clave);
        claveConfim = (EditText) findViewById(R.id.claveConfim);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(PerfilEstatusCero.this);

        getInfo();

        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!clave.getText().toString().isEmpty() && !claveConfim.getText().toString().isEmpty()){
                    if(clave.getText().length() >= 6 && claveConfim.getText().length() >= 6){
                        cambiarPassword();
                    }else{
                        Toast.makeText(PerfilEstatusCero.this, "La contraseña debe ser mayor o igual a 6 carácteres.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(PerfilEstatusCero.this, "Debes completar los campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelar();
            }
        });

        showHideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verClave){
                    clave.setTransformationMethod(new PasswordTransformationMethod());
                    showHideBtn.setImageResource(R.drawable.ic_baseline_remove_red_eye_24);
                    verClave = !verClave;
                } else{
                    clave.setTransformationMethod(null);
                    showHideBtn.setImageResource(R.drawable.ic_outline_remove_red_eye_24);
                    verClave = !verClave;
                }
            }
        });

        showHideBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verClaveConf){
                    claveConfim.setTransformationMethod(new PasswordTransformationMethod());
                    showHideBtn2.setImageResource(R.drawable.ic_baseline_remove_red_eye_24);
                    verClaveConf = !verClaveConf;
                } else{
                    claveConfim.setTransformationMethod(null);
                    showHideBtn2.setImageResource(R.drawable.ic_outline_remove_red_eye_24);
                    verClaveConf = !verClaveConf;
                }
            }
        });
    }

    public void getInfo() {
        SharedPreferences user = getApplicationContext().getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
        nombre.setText(user.getString("nombreUser", "").toUpperCase());
        telefono.setText(user.getString("telefonoUser", ""));
        email.setText(auth.getCurrentUser().getEmail().toUpperCase());

        idLider = user.getString("idLider", "");

        if(user.getString("nivelUser", "").equals("")){
            nivel.setText("LIDER 12 ");
        }else{
            nivel.setText("NIVEL: " + user.getString("nivelUser", ""));
        }

        if(user.getString("liderUser", "").equals("")){
            lider.setVisibility(View.GONE);
        }else{
            lider.setText("LIDER: " + user.getString("liderUser", "").toUpperCase());
        }

    }

    public void cancelar() {
        if (networkInfo != null && networkInfo.isConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Desea calcelar la verificación?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            auth.signOut();
                            startActivity(new Intent(getBaseContext(), SesionActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } else {
            Toast.makeText(this,"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
        }
    }

    public void cambiarPassword(){
        if (networkInfo != null && networkInfo.isConnected()) {
            loadingDialog.starDialog();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider
                    .getCredential(auth.getCurrentUser().getEmail(), "123123");
                user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(clave.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            actualizarEstatus(idLider);

                                        } else {
                                            loadingDialog.dismissDialog();
                                            Toast.makeText(getApplicationContext(),"Ha ocurrido un error.!",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                loadingDialog.dismissDialog();
                                Toast.makeText(getApplicationContext(),"Error de autenticación de usuario.",Toast.LENGTH_LONG).show();

                            }
                        }
                    });
        } else {
            Toast.makeText(this,"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
        }
    }
    public void actualizarEstatus(String id){
        if (networkInfo != null && networkInfo.isConnected()) {

            Map<String, Object> estatusMap = new HashMap<>();
            estatusMap.put("estatus", "1");

            firestore.collection("Lider").document(id).update(estatusMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            SharedPreferences user = getApplicationContext().getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
                            SharedPreferences.Editor userInfo = user.edit();
                            userInfo.putString("estatusUser", "1");
                            userInfo.commit();
                            loadingDialog.dismissDialog();
                            finish();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingDialog.dismissDialog();
                            Toast.makeText(getApplicationContext(), "Ha ocurrido un error.", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            loadingDialog.dismissDialog();
            Toast.makeText(getApplicationContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
        }
    }
}
package com.example.iviluz_app.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.PerfilEstatusCero;
import com.example.iviluz_app.R;
import com.example.iviluz_app.SesionActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import utilidades.LoadingDialog;

public class CambiarClaveDialogFragment extends DialogFragment {
    private Button actualizar;
    private Button cancelar;

    private TextView nombre;
    private TextView telefono;
    private TextView email;

    private EditText clave;
    private EditText confirmar;


    private FirebaseFirestore firestore;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    public CambiarClaveDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return crearDialogo();
    }

    private AlertDialog crearDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_cambiar_clave_dialog, null);
        builder.setView(v);

        actualizar = (Button) v.findViewById(R.id.actualizar);
        cancelar = (Button) v.findViewById(R.id.cancelar);

        nombre = (TextView) v.findViewById(R.id.nombre);
        email = (TextView) v.findViewById(R.id.email);
        telefono = (TextView) v.findViewById(R.id.telefono);

        clave = (EditText) v.findViewById(R.id.clave);
        confirmar = (EditText) v.findViewById(R.id.claveConfim);

        getPersona();

        firestore = FirebaseFirestore.getInstance();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());


        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actualizarClave();


            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cambiar_clave_dialog, container, false);
    }

    public void getPersona(){
        SharedPreferences prefe = getContext().getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
        nombre.setText(prefe.getString("nombreUser",""));
        telefono.setText(prefe.getString("telefonoUser",""));
        email.setText(auth.getCurrentUser().getEmail());

    }

    public void actualizarClave(){
        if (networkInfo != null && networkInfo.isConnected()) {
            if (!clave.getText().toString().isEmpty() && !confirmar.getText().toString().isEmpty()){
                if(clave.getText().length() >= 6 && confirmar.getText().length() >= 6){
                    if (clave.getText().toString().equals(confirmar.getText().toString())) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String newPassword = clave.getText().toString();

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Al cambiar la contraseña se cerrará la sesion actual \n ¿Desea continuar?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        loadingDialog.starDialog();
                                        user.updatePassword(newPassword)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getContext(), "Cierre sesión e inicie nuevamente con la nueva contraseña .", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            loadingDialog.dismissDialog();
                                                            Toast.makeText(getContext(), "No se pudo cambiar la contraseña debido a un error.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();
                    }else{
                        Toast.makeText(getContext(), "Las contraseña deben iguales.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), "La contraseña debe ser mayor o igual a 6 carácteres.", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getContext(), "Debes completar los campos.", Toast.LENGTH_SHORT).show();
            }
        }else{
            loadingDialog.dismissDialog();
            Toast.makeText(this.getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
        }
    }
}
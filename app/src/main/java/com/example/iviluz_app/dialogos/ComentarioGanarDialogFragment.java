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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import utilidades.LoadingDialog;

public class ComentarioGanarDialogFragment extends DialogFragment {

    private Button guardar;
    private Button cancelar;

    private TextView nombre;
    private TextView telefono;
    private EditText comentario;
    private RadioButton contesto;
    private RadioButton no_contesto;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    private String idGanar;
    public ComentarioGanarDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return crearDialogo();
    }

    private AlertDialog crearDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_comentario_ganar_dialog, null);
        builder.setView(v);

        guardar = (Button) v.findViewById(R.id.guardar);
        cancelar = (Button) v.findViewById(R.id.cancelar);
        nombre = (TextView) v.findViewById(R.id.nombre);
        telefono = (TextView) v.findViewById(R.id.telefono);
        comentario = (EditText) v.findViewById(R.id.comentario);
        contesto = (RadioButton) v.findViewById(R.id.contesto);
        no_contesto = (RadioButton) v.findViewById(R.id.no_contesto);

        firestore = FirebaseFirestore.getInstance();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        getPersona();

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                loadingDialog.starDialog();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (comentario.getText().toString().equals("") ){
                        loadingDialog.dismissDialog();
                        Toast.makeText(getContext(), "Agregue un comentario para guardar.", Toast.LENGTH_SHORT).show();
                    }else{
                        String estatus = "";
                        if (contesto.isChecked()==true) {
                            estatus = "CONTESTÓ";
                        } else if (no_contesto.isChecked()==true) {
                            estatus = "NO CONTESTÓ";
                        }else{
                            estatus = "";
                        }
                        Map<String, Object> ganarMap = new HashMap<>();
                        ganarMap.put("comentario", comentario.getText().toString());
                        ganarMap.put("estatus", estatus);

                        firestore.collection("Ganar").document(idGanar).update(ganarMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(getContext(), "Actualizado con exito", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(getContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                } else {
                    loadingDialog.dismissDialog();
                    Toast.makeText(getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
                }
            }
        });

        telefono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numero = telefono.getText().toString();

                if (!numero.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(numero + " \n ¿Desea llamar al número de contacto?")
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Uri number = Uri.parse("tel:" + numero); // Creamos una uri con el numero de telefono
                                    Intent dial = new Intent(Intent.ACTION_DIAL, number); // Creamos una llamada al Intent de llamadas
                                    startActivity(dial);

                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                }else{
                    Toast.makeText(getContext(),"No hay número para llamar.",Toast.LENGTH_LONG).show();
                }
            }
        });

        return builder.create();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comentario_ganar_dialog, container, false);
    }

    public void getPersona(){
        SharedPreferences prefe = getContext().getSharedPreferences("datos", Context.MODE_PRIVATE);
        idGanar = prefe.getString("idGanar","");
        nombre.setText(prefe.getString("nombre","").toUpperCase());
        telefono.setText(prefe.getString("telefono","").toUpperCase());
        comentario.setText(prefe.getString("comentario",""));
        System.out.println(":::::>" + prefe.getString("comentario",""));
        if (prefe.getString("estatus","").equals("CONTESTÓ")) {
            contesto.setChecked(true);
        }else if (prefe.getString("estatus","").equals("NO CONTESTÓ")) {
            no_contesto.setChecked(true);
        }

    }

}
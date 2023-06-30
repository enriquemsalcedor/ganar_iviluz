package com.example.iviluz_app.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import utilidades.LoadingDialog;

public class InformacionDialogFragment extends DialogFragment {

    private Button guardar;
    private Button cancelar;

    private EditText titulo;
    private EditText contenido;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    String ID = "";
    boolean isEdit = false;

    public InformacionDialogFragment() {
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
        View v = inflater.inflate(R.layout.fragment_informacion_dialog, null);
        builder.setView(v);

        guardar = (Button) v.findViewById(R.id.guardar);
        cancelar = (Button) v.findViewById(R.id.cancelar);

        titulo = (EditText) v.findViewById(R.id.titulo);
        contenido = (EditText) v.findViewById(R.id.contenido);

        firestore = FirebaseFirestore.getInstance();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                limpiar();
                dismiss();
            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (networkInfo != null && networkInfo.isConnected()) {
                    if (titulo.getText().toString().equals("")
                            || contenido.getText().toString().equals("")

                    ){
                        Toast.makeText(getContext(), "Complete los datos para guardar.", Toast.LENGTH_SHORT).show();
                    }else{
                        loadingDialog.starDialog();

                        Date date = new Date();
                        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

                        Map<String, Object> infoMap = new HashMap<>();
                        infoMap.put("titulo", titulo.getText().toString());
                        infoMap.put("contenido", contenido.getText().toString());
                        infoMap.put("fecha", format.format(date));

                        firestore.collection("Informacion").add(infoMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                limpiar();
                                loadingDialog.dismissDialog();
                                Toast.makeText(getContext(), "Agregado con exito", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingDialog.dismissDialog();
                                Toast.makeText(getContext(), "Ha ocurrido un error al guardar los datos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_informacion_dialog, container, false);
    }

    private void limpiar(){
        titulo.setText("");
        contenido.setText("");
    }

    public void getInfo(){
        SharedPreferences prefe = getContext().getSharedPreferences("datos", Context.MODE_PRIVATE);
        ID = prefe.getString("idUsuario","");
        titulo.setText(prefe.getString("titulo",""));
        contenido.setText(prefe.getString("contenido",""));
        isEdit = true;

    }
}
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import utilidades.LoadingDialog;


public class InfoLiderDialogFragment extends DialogFragment {

    private Button actualizar;
    private Button cancelar;

    private TextView nombre;
    private TextView telefono;
    private TextView email;
    private TextView nivel;
    private TextView lider;

    private Switch coordGanar;
    private Switch servGanar;
    private Switch movilizacion;

    private boolean swCoordGanar = false;
    private boolean swServGanar = false;
    private boolean swMovilizacion = false;

    String idLider;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    public InfoLiderDialogFragment() {

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
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
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
        View v = inflater.inflate(R.layout.fragment_info_lider_dialog, null);
        builder.setView(v);

        actualizar = (Button) v.findViewById(R.id.actualizar);
        cancelar = (Button) v.findViewById(R.id.cancelar);

        nombre = (TextView) v.findViewById(R.id.fecha);
        email = (TextView) v.findViewById(R.id.email);
        telefono = (TextView) v.findViewById(R.id.telefono);
        nivel = (TextView) v.findViewById(R.id.nivel);
        lider = (TextView) v.findViewById(R.id.lider);

        coordGanar = (Switch) v.findViewById(R.id.coordGanar);
        servGanar = (Switch) v.findViewById(R.id.servGanar);
        movilizacion = (Switch) v.findViewById(R.id.movilizacion);

        getPersona();

        firestore = FirebaseFirestore.getInstance();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());


        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (coordGanar.isChecked())
                    swCoordGanar = true;
                else
                    swCoordGanar = false;

                if (servGanar.isChecked())
                    swServGanar = true;
                else
                    swServGanar = false;

                if (movilizacion.isChecked())
                    swMovilizacion = true;
                else
                    swMovilizacion = false;

                loadingDialog.starDialog();
                if (networkInfo != null && networkInfo.isConnected()) {

                    Map<String, Object> ganarMap = new HashMap<>();
                    ganarMap.put("coordGanar", swCoordGanar);
                    ganarMap.put("ganar", swServGanar);
                    ganarMap.put("movilizacion", swMovilizacion);

                    firestore.collection("Lider").document(idLider).update(ganarMap)
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
                } else {
                    loadingDialog.dismissDialog();
                    Toast.makeText(getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
                }

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
        return inflater.inflate(R.layout.fragment_info_lider_dialog, container, false);
    }

    public void getPersona(){
        SharedPreferences prefe = getContext().getSharedPreferences("datosLider", Context.MODE_PRIVATE);
        idLider = prefe.getString("idLider","");
        nombre.setText(prefe.getString("nombre",""));
        telefono.setText(prefe.getString("telefono",""));
        email.setText(prefe.getString("email",""));
        //lider.setText("Ministerio: " + prefe.getString("lider",""));
        nivel.setText("Lider: " + prefe.getString("nivel",""));
        /*
        swCoordGanar.("Lider: " + prefe.getString("nivel",""));
        swServGanar.setText("Lider: " + prefe.getString("nivel",""));
        swMovilizacion.setText("Lider: " + prefe.getString("nivel",""));

         */
    }
}
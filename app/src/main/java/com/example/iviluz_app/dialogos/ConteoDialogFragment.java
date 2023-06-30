package com.example.iviluz_app.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.example.iviluz_app.modelos.Ganar;
import com.example.iviluz_app.ui.ganar.GanarFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import utilidades.LoadingDialog;


public class ConteoDialogFragment extends DialogFragment {

    private Button guardar;
    private Button cancelar;
    private Spinner spServicios;
    private EditText arriba;
    private EditText abajo;
    private EditText kids;
    private EditText prejuvenil;
    private EditText ganar;

    private String servicioSelect;
    private String elementoSelect;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    private ArrayList<String> listServicios;
    private ArrayList<String> listInServicios;

    private String fechaFormat = "";
    private Boolean existeConteo = false;

    private String fechaValid = "";
    private String servicioValid = "";

    public ConteoDialogFragment() {
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
        View v = inflater.inflate(R.layout.fragment_conteo_dialog, null);
        builder.setView(v);

        guardar = (Button) v.findViewById(R.id.guardar);
        cancelar = (Button) v.findViewById(R.id.cancelar);

        arriba = (EditText) v.findViewById(R.id.arriba);
        abajo = (EditText) v.findViewById(R.id.abajo);
        kids = (EditText) v.findViewById(R.id.kids);
        prejuvenil = (EditText) v.findViewById(R.id.prejuvenil);
        spServicios = (Spinner) v.findViewById(R.id.spServicios);

        firestore = FirebaseFirestore.getInstance();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());
        getServicios();

        listServicios = new ArrayList<>();
        listInServicios = new ArrayList<>();

        Date date = new Date();
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        fechaFormat = format.format(date);

        spServicios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                elementoSelect = listServicios.get(i);
                servicioSelect = listInServicios.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (servicioSelect.toString().equals("0")
                            || arriba.getText().toString().equals("")
                            || abajo.getText().toString().equals("")
                            || kids.getText().toString().equals("")
                            || prejuvenil.getText().toString().equals("")
                            || ganar.getText().toString().equals("")
                    ){
                        Toast.makeText(getContext(), "Complete los datos para guardar.", Toast.LENGTH_SHORT).show();
                    }else {
                        verificarConteo(fechaFormat, servicioSelect.toString());
                    }

                } else {
                    Toast notificacion=Toast.makeText(getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG);
                    notificacion.show();
                }
            }
        });

        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conteo_dialog, container, false);
    }


    public void getServicios(){
        loadingDialog.starDialog();
        if (networkInfo != null && networkInfo.isConnected()) {
            firestore.collection("Servicios")
                .whereEqualTo("estatus", "ACTIVO")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            loadingDialog.dismissDialog();
                            listServicios.add("-- Seleccione un Servicio --");
                            listInServicios.add("0");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                System.out.println(document.getId() + " => " + document.getString("nombre"));

                                listServicios.add(document.getString("nombre"));
                                listInServicios.add(document.getString("numero"));
                            }
                            ArrayAdapter adapterSp = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, listServicios);
                            spServicios.setAdapter(adapterSp);
                        } else {
                            Toast notificacion = Toast.makeText(getContext(),"Ha ocurrido un error listado los Servicios",Toast.LENGTH_LONG);
                            notificacion.show();
                            loadingDialog.dismissDialog();
                        }
                    }
                });
        } else {
            loadingDialog.dismissDialog();
            Toast notificacion=Toast.makeText(this.getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG);
            notificacion.show();
        }

    }


    public void verificarConteo(String fecha, String servicio){

        firestore.collection("Conteo")
                .whereEqualTo("fecha_creacion", fecha.toString()).whereEqualTo("servicio", servicio.toString()).limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String fechaAux = "";
                            String servicioAux = "";

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                System.out.println(document.getString("fecha_creacion") + " => " + document.getString("servicio"));

                                fechaAux = document.getString("fecha_creacion");
                                servicioAux = document.getString("servicio");
                            }
                            fechaValid = fechaAux;
                            servicioValid = servicioAux;

                            if (fechaValid.equals(fecha) && servicioValid.equals(servicio)){
                                Toast notificacion = Toast.makeText(getContext(),"Ya existe un Conteo registrado para el " + elementoSelect.toString(),Toast.LENGTH_LONG);
                                notificacion.show();
                            }else{
                                registrar();
                            }
                        } else {
                            Toast notificacion = Toast.makeText(getContext(),"Ha ocurrido un error Verificando el Conteo",Toast.LENGTH_LONG);
                            notificacion.show();
                        }
                    }
                });
    }

    public void registrar(){
        if (servicioSelect.toString().equals("0")
                || arriba.getText().toString().equals("")
                || abajo.getText().toString().equals("")
                || kids.getText().toString().equals("")
                || prejuvenil.getText().toString().equals("")
        ){
            Toast.makeText(getContext(), "Complete los datos para guardar.", Toast.LENGTH_SHORT).show();
        }else {
            loadingDialog.starDialog();

            Map<String, Object> conteoMap = new HashMap<>();
            conteoMap.put("servicio", servicioSelect.toString());
            conteoMap.put("arriba", arriba.getText().toString());
            conteoMap.put("abajo", abajo.getText().toString());
            conteoMap.put("kids", kids.getText().toString());
            conteoMap.put("prejuvenil", prejuvenil.getText().toString());
            conteoMap.put("usuario_creador", auth.getCurrentUser().getEmail().toString());
            conteoMap.put("fecha_creacion", fechaFormat);

            firestore.collection("Conteo").add(conteoMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    dismiss();
                    loadingDialog.dismissDialog();
                    Toast.makeText(getContext(), "Conteo agregado con exito", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Ha ocurrido un error al guardar el Conteo.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
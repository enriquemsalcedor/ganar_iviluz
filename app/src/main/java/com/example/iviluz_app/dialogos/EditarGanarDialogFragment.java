package com.example.iviluz_app.dialogos;

import android.annotation.SuppressLint;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utilidades.LoadingDialog;

public class EditarGanarDialogFragment extends DialogFragment {

    private Button guardar;
    private Button cancelar;

    private EditText nombre;
    private EditText telefono;
    private EditText direccion;
    private EditText peticion;
    private EditText invitado_por;
    private RadioButton contesto;
    private RadioButton no_contesto;

    private EditText servicio;
    private EditText fecha;
    private Spinner lider;

    private String liderSelectID = "";
    private String liderSelect = "";

    private FirebaseFirestore firestore;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    private String idGanar;
    private ArrayList<String> listMinisterio;
    private ArrayList<String> listIdMinisterio;

    private ArrayList<String> listServicios;
    private ArrayList<String> listInServicios;
    private String idMinisterio = "";
    private ArrayAdapter adapterSp;


    public EditarGanarDialogFragment() {
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

    @SuppressLint("WrongViewCast")
    private AlertDialog crearDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_editar_ganar_dialog, null);
        builder.setView(v);

        guardar = (Button) v.findViewById(R.id.guardar);
        cancelar = (Button) v.findViewById(R.id.cancelar);

        nombre = (EditText) v.findViewById(R.id.nombre);
        telefono = (EditText) v.findViewById(R.id.telefono);
        direccion = (EditText) v.findViewById(R.id.direccion);
        peticion = (EditText) v.findViewById(R.id.peticion);
        invitado_por = (EditText) v.findViewById(R.id.invitado_por);
        contesto = (RadioButton) v.findViewById(R.id.contesto);
        no_contesto = (RadioButton) v.findViewById(R.id.no_contesto);
        servicio = (EditText) v.findViewById(R.id.servicios);
        fecha = (EditText) v.findViewById(R.id.fecha);
        lider = (Spinner) v.findViewById(R.id.lider);


        listMinisterio = new ArrayList<>();
        listIdMinisterio = new ArrayList<>();

        listServicios = new ArrayList<>();
        listInServicios = new ArrayList<>();

        firestore = FirebaseFirestore.getInstance();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        invitado_por.setVisibility(View.GONE);

        getPersona();
        getLideres();

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
                try {
                    if (networkInfo != null && networkInfo.isConnected()) {
                        if (nombre.getText().toString().equals("")
                                && telefono.getText().toString().equals("")
                                && direccion.getText().toString().equals("")
                                && peticion.getText().toString().equals("")
                                && peticion.getText().toString().equals("")
                        ){
                            loadingDialog.dismissDialog();
                            Toast.makeText(getContext(), "Complete los datos para guardar.", Toast.LENGTH_SHORT).show();
                        }else{
                            String estatus = "";
                            if (contesto.isChecked()==true) {
                                estatus = "CONTESTÓ";
                            }else if (no_contesto.isChecked()==true) {
                                estatus = "NO CONTESTÓ";
                            }else{
                                estatus = "";
                            }
                            Map<String, Object> ganarMap = new HashMap<>();
                            ganarMap.put("nombre", nombre.getText().toString());
                            ganarMap.put("telefono", telefono.getText().toString());
                            ganarMap.put("direccion", direccion.getText().toString());
                            ganarMap.put("peticion", peticion.getText().toString());
                            ganarMap.put("lider_12", liderSelect);
                            ganarMap.put("id_ministerio", liderSelectID);
                            ganarMap.put("invitado_por", invitado_por.getText().toString());
                            ganarMap.put("usuario_creador", auth.getCurrentUser().getEmail().toString());
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
                }catch (Exception e){
                    loadingDialog.dismissDialog();
                    Toast.makeText(getContext(),"Ha ocurrido un error!.",Toast.LENGTH_LONG).show();
                }
            }
        });


        lider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String elemento = listIdMinisterio.get(i);
                String ministerio = listMinisterio.get(i);

                liderSelectID = elemento;

                if (elemento.equals("0")){
                    invitado_por.setText("");
                }else{
                    liderSelect = ministerio;
                    idMinisterio = elemento;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lider.post(new Runnable() {
            @Override
            public void run() {
                if(liderSelect != null){
                    try{
                        lider.setSelection(adapterSp.getPosition(liderSelect));
                    }catch (Exception e){
                        //Toast.makeText(getContext(), "Ha ocurrido un error inesperado. " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
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
        return inflater.inflate(R.layout.fragment_editar_ganar_dialog, container, false);
    }

    public void getPersona(){
        SharedPreferences prefe = getContext().getSharedPreferences("datos", Context.MODE_PRIVATE);
        idGanar = prefe.getString("idGanar","");
        nombre.setText(prefe.getString("nombre",""));
        telefono.setText(prefe.getString("telefono",""));
        direccion.setText(prefe.getString("direccion",""));
        peticion.setText(prefe.getString("peticion",""));

        if(prefe.getString("lider_12","").equals("")){
            liderSelect = null;
        }else{
            liderSelect = prefe.getString("lider_12","");
        }


        if(prefe.getString("invitado_por","") != null) {
            invitado_por.setVisibility(View.VISIBLE);
            invitado_por.setText(prefe.getString("invitado_por", ""));
        }else{
            invitado_por.setVisibility(View.GONE);
        }
        fecha.setText(prefe.getString("fecha_servicio",""));

        String servicioStg= prefe.getString("servicio","");

        switch (servicioStg) {
            case "1" : servicioStg = "Primer Servicio";
                break;
            case "2" : servicioStg = "Segundo Servicio";
                break;
            case "3" : servicioStg = "Tercer Servicio";
                break;
            case "4" : servicioStg = "Cuarto Servicio";
                break;
        }

        servicio.setText(servicioStg);

        if (prefe.getString("estatus","").equals("CONTESTÓ")) {
            contesto.setChecked(true);
        }else if (prefe.getString("estatus","").equals("NO CONTESTÓ")) {
            no_contesto.setChecked(true);
        }

    }

    public void getLideres() {
        if (networkInfo != null && networkInfo.isConnected()) {
            firestore.collection("Ministerio")
                    .whereEqualTo("estatus", "ACTIVO")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                listMinisterio.add("-- Seleccione un lider --");
                                listIdMinisterio.add("");

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getString("nombre") != null) {
                                        System.out.println(document.getId() + " => " + document.getString("nombre"));

                                        listIdMinisterio.add(document.getId());
                                        listMinisterio.add(document.getString("nombre"));

                                    }
                                }
                                adapterSp = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, listMinisterio);
                                lider.setAdapter(adapterSp);
                                lider.setSelection(adapterSp.getPosition(liderSelect));

                            } else {
                                Toast.makeText(getContext(), "Ha ocurrido un error listado los Lideres", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this.getContext(), "No tienes conexión a internet en este momento.", Toast.LENGTH_LONG).show();
        }
    }
}
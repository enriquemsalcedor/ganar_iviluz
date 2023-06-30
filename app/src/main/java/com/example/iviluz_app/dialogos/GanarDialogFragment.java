package com.example.iviluz_app.dialogos;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.DatePickerFragment;
import com.example.iviluz_app.R;
import com.example.iviluz_app.ui.ganar.GanarFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import utilidades.LoadingDialog;


public class GanarDialogFragment extends DialogFragment {

    private Button guardar;
    private Button cancelar;

    private EditText nombre;
    private EditText telefono;
    private EditText direccion;
    private EditText peticion;
    private EditText invitado_por;
    private Spinner lider;
    private Spinner servicios;

    private EditText fecha;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    private ArrayList<String> listMinisterio;
    private ArrayList<String> listIdMinisterio;

    private ArrayList<String> listServicios;
    private ArrayList<String> listInServicios;
    private String servicio = "0";
    private String idMinisterio = "";
    private String fechaFormat = "";
    private String liderSelectID = "0";
    private String liderSelect = "";
    public GanarDialogFragment() {
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
        View v = inflater.inflate(R.layout.fragment_ganar_dialog, null);
        builder.setView(v);

        guardar = (Button) v.findViewById(R.id.guardar);
        cancelar = (Button) v.findViewById(R.id.cancelar);

        nombre = (EditText) v.findViewById(R.id.nombre);
        telefono = (EditText) v.findViewById(R.id.telefono);
        direccion = (EditText) v.findViewById(R.id.direccion);
        peticion = (EditText) v.findViewById(R.id.peticion);
        invitado_por = (EditText) v.findViewById(R.id.invitado_por);
        lider = (Spinner) v.findViewById(R.id.lider);
        servicios = (Spinner) v.findViewById(R.id.servicios);
        fecha = (EditText) v.findViewById(R.id.fecha);

        firestore = FirebaseFirestore.getInstance();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        listMinisterio = new ArrayList<>();
        listIdMinisterio = new ArrayList<>();

        invitado_por.setVisibility(View.GONE);

        listServicios = new ArrayList<>();
        listInServicios = new ArrayList<>();

        Date date = new Date();
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        fechaFormat = format.format(date);

        getServicios();

        fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.fecha:
                        showDatePickerDialog();
                        break;
                }
            }
        });

        servicios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String elemento = listInServicios.get(i);
                if (!elemento.equals("0")){
                    servicio = elemento;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        getLideres();

        lider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String elemento = listIdMinisterio.get(i);
                String ministerio = listMinisterio.get(i);

                liderSelectID = elemento;
                liderSelect = ministerio;
                if (elemento.equals("0")){
                    invitado_por.setVisibility(View.VISIBLE);
                }else if (elemento.equals("")){
                    invitado_por.setText("");
                    invitado_por.setVisibility(View.GONE);
                }else{
                    idMinisterio = elemento;
                    invitado_por.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                try {
                    if (networkInfo != null && networkInfo.isConnected()) {
                        boolean esVacio = false;
                        if(liderSelectID.equals("0") || liderSelect.equals("-- No es un lider 12 --")){
                            if(invitado_por.getText().toString().equals("")) {
                                esVacio = true;
                            }else{
                                esVacio = false;
                                liderSelect = "";
                            }
                        }else{
                            if(liderSelectID.equals("")){
                                esVacio = true;
                            }else{
                                esVacio = false;
                            }
                        }

                        if (nombre.getText().toString().equals("")
                                || telefono.getText().toString().equals("")
                                || direccion.getText().toString().equals("")
                                || peticion.getText().toString().equals("")
                                || esVacio
                                || fecha.getText().toString().equals("")
                                || servicio.toString().equals("0")
                        ){
                            Toast.makeText(getContext(), "Complete los datos para guardar.", Toast.LENGTH_SHORT).show();
                        }else{
                            loadingDialog.starDialog();

                            Map<String, Object> ganarMap = new HashMap<>();
                            ganarMap.put("nombre", nombre.getText().toString());
                            ganarMap.put("telefono", telefono.getText().toString());
                            ganarMap.put("direccion", direccion.getText().toString());
                            ganarMap.put("peticion", peticion.getText().toString());
                            ganarMap.put("lider_12", liderSelect);
                            ganarMap.put("invitado_por", invitado_por.getText().toString());
                            ganarMap.put("servicio", servicio);
                            ganarMap.put("id_ministerio", liderSelectID);
                            ganarMap.put("usuario_creador", auth.getCurrentUser().getEmail().toString());
                            ganarMap.put("estatus", "NUEVO");
                            ganarMap.put("estado", "ACTIVO");
                            ganarMap.put("comentario", "");
                            ganarMap.put("fecha_creacion", fechaFormat);
                            ganarMap.put("fecha_servicio", fecha.getText().toString());

                            firestore.collection("Ganar").add(ganarMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
                }catch (Exception e){
                    Toast.makeText(getContext(),"Ha ocurrido un error!",Toast.LENGTH_LONG).show();
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

        return inflater.inflate(R.layout.fragment_ganar_dialog, container, false);
    }

    private void limpiar(){
        nombre.setText("");
        telefono.setText("");
        direccion.setText("");
        peticion.setText("");
        invitado_por.setText("");
        lider.setSelection(0);
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
                                listMinisterio.add("-- No es un lider 12 --");
                                listIdMinisterio.add("0");

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getString("nombre") != null) {
                                        System.out.println(document.getId() + " => " + document.getString("nombre"));

                                        listIdMinisterio.add(document.getId());
                                        listMinisterio.add(document.getString("nombre"));

                                    }
                                }
                                ArrayAdapter adapterSp = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, listMinisterio);
                                lider.setAdapter(adapterSp);

                            } else {
                                Toast.makeText(getContext(), "Ha ocurrido un error listado los Lideres", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this.getContext(), "No tienes conexión a internet en este momento.", Toast.LENGTH_LONG).show();
        }
    }

    public void getServicios(){
        if (networkInfo != null && networkInfo.isConnected()) {
            firestore.collection("Servicios")
                    .whereEqualTo("estatus", "ACTIVO")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                listServicios.add("-- Seleccione un Servicio --");
                                listInServicios.add("0");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    System.out.println(document.getId() + " => " + document.getString("nombre"));

                                    listServicios.add(document.getString("nombre"));
                                    listInServicios.add(document.getString("numero"));
                                }
                                ArrayAdapter adapterSp = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, listServicios);
                                servicios.setAdapter(adapterSp);
                            } else {
                                Toast notificacion = Toast.makeText(getContext(),"Ha ocurrido un error listado los Servicios",Toast.LENGTH_LONG);
                                notificacion.show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this.getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
        }

    }


    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                final String selectedDate = twoDigits(day) + "/" + twoDigits(month+1) + "/" + year;
                fecha.setText(selectedDate);
            }
        });

        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }
}
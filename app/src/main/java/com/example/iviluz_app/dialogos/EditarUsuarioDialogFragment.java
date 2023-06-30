package com.example.iviluz_app.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.example.iviluz_app.modelos.FechaServicio;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utilidades.LoadingDialog;


public class EditarUsuarioDialogFragment extends DialogFragment {

    private Button guardar;
    private Button cancelar;
    private EditText nombre;
    private EditText telefono;
    private EditText email;
    private Spinner lider;
    private RadioButton activo;
    private RadioButton inactivo;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    private ArrayList<String> listMinisterio;
    private ArrayList<String> listIdMinisterio;

    boolean existeEmail = false;
    String idMinisterio = "";
    String ministerio = "";

    private String idUsuario;
    private ArrayAdapter adapterSpMin;

    public EditarUsuarioDialogFragment() {

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
        View v = inflater.inflate(R.layout.fragment_editar_usuario_dialog, null);
        builder.setView(v);

        builder.setView(v);

        guardar = (Button) v.findViewById(R.id.guardar);
        cancelar = (Button) v.findViewById(R.id.cancelar);

        nombre = (EditText) v.findViewById(R.id.nombre);
        telefono = (EditText) v.findViewById(R.id.telefono);
        email = (EditText) v.findViewById(R.id.email);
        lider = (Spinner) v.findViewById(R.id.lider);
        activo = (RadioButton) v.findViewById(R.id.activo);
        inactivo = (RadioButton) v.findViewById(R.id.inactivo);

        firestore = FirebaseFirestore.getInstance();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        listMinisterio = new ArrayList<>();
        listIdMinisterio = new ArrayList<>();

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editarUsuario();
            }
        });

        getLideres();
        getUsuario();

        lider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                idMinisterio = listIdMinisterio.get(i);
                ministerio = listMinisterio.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lider.post(new Runnable() {
            @Override
            public void run() {
                if(ministerio != null) {
                    try {
                        lider.setSelection(adapterSpMin.getPosition(ministerio));
                    } catch (Exception e) {
                        //Toast.makeText(getContext(), "Ha ocurrido un error inesperado. " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return builder.create();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_editar_usuario_dialog, container, false);
    }

    public void editarUsuario(){
        if (networkInfo != null && networkInfo.isConnected()) {
            if (nombre.getText().toString().equals("")
                    || telefono.getText().toString().equals("")
                    || email.getText().toString().equals("")
                    || ministerio.equals("0")

            ){
                Toast.makeText(getContext(), "Complete los datos para guardar.", Toast.LENGTH_SHORT).show();
            }else{
                try{
                    loadingDialog.starDialog();
                    if (!verificarUsuario(email.getText().toString())) {

                        String estatus = "";
                        if (activo.isChecked()==true) {
                            estatus = "1";
                        }
                        if (inactivo.isChecked()==true) {
                            estatus = "0";
                        }
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("nombre", nombre.getText().toString());
                        userMap.put("email", email.getText().toString());
                        userMap.put("telefono", telefono.getText().toString());
                        userMap.put("lider", ministerio);
                        userMap.put("liderID", idMinisterio);
                        userMap.put("estatus", estatus);

                        firestore.collection("Usuario").document(idUsuario).update(userMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getContext(), "El usuario para " + nombre.getText().toString().toUpperCase() + " se actualizó con exito", Toast.LENGTH_SHORT).show();
                                        dismiss();
                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingDialog.dismissDialog();
                                        Toast.makeText(getContext(), "Ha ocurrido un error al guardar los datos", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }else{
                        Toast.makeText(getContext(), "El correo electrónico ingresado ya existe para otro usuario.", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), "Ha ocurrido un error.!", Toast.LENGTH_SHORT).show();
                }

            }
        } else {
            loadingDialog.dismissDialog();
            Toast.makeText(getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
        }
    }


    public boolean verificarUsuario(String emailUsuario){
        firestore.collection("Lider")
                .whereEqualTo("email", emailUsuario)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast notificacion = Toast.makeText(getContext(),"Ha ocurrido un error listando las personas del Ganar",Toast.LENGTH_LONG);
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("email") != null) {
                                existeEmail = true;
                            }
                        }

                        loadingDialog.dismissDialog();
                    }
                });
        return existeEmail;
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

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getString("nombre") != null) {
                                        System.out.println(document.getId() + " => " + document.getString("nombre"));

                                        listIdMinisterio.add(document.getId());
                                        listMinisterio.add(document.getString("nombre"));

                                    }
                                }

                                adapterSpMin = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, listMinisterio);
                                lider.setAdapter(adapterSpMin);

                            } else {
                                Toast.makeText(getContext(), "Ha ocurrido un error listado los Lideres", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this.getContext(), "No tienes conexión a internet en este momento.", Toast.LENGTH_LONG).show();
        }
    }

    public void getUsuario(){
        SharedPreferences prefe = getContext().getSharedPreferences("datos", Context.MODE_PRIVATE);
        idUsuario = prefe.getString("idUsuario","");
        nombre.setText(prefe.getString("nombre",""));
        telefono.setText(prefe.getString("telefono",""));
        email.setText(prefe.getString("email",""));

        ministerio = prefe.getString("lider","");
        if (prefe.getString("estatus","").equals("1")) {
            activo.setChecked(true);
        }
        if (prefe.getString("estatus","").equals("0")) {
            inactivo.setChecked(true);
        }

    }



}
package com.example.iviluz_app.ui.ganar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
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
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.example.iviluz_app.databinding.GanarFragmentBinding;
import com.example.iviluz_app.dialogos.ComentarioGanarDialogFragment;
import com.example.iviluz_app.dialogos.EditarGanarDialogFragment;
import com.example.iviluz_app.dialogos.GanarDialogFragment;
import com.example.iviluz_app.modelos.Ganar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import utilidades.LoadingDialog;

public class GanarFragment extends Fragment {

    private GanarFragmentBinding binding;

    private GanarViewModel mViewModel;

    private ListView lvGanar;
    private Spinner spServicios;
    private Button addGanar;
    private TextView fecha;
    private TextView cantidad;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private ArrayList<Ganar> listGanar;
    private ArrayList<String> listServicios;
    private ArrayList<String> listInServicios;

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    public static GanarFragment newInstance() {
        return new GanarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = GanarFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        spServicios = (Spinner) view.findViewById(R.id.spServicios);
        lvGanar = (ListView) view.findViewById(R.id.lvGanar);
        addGanar = (Button) view.findViewById(R.id.addGanar);
        cantidad = (TextView) view.findViewById(R.id.cantidad);
        fecha = (TextView) view.findViewById(R.id.fecha);

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        listServicios = new ArrayList<>();
        listInServicios = new ArrayList<>();

        getServicios();

        spServicios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String elemento = listInServicios.get(i);
                if (elemento.equals("0")){
                    getGanar();
                }else {
                    getGanarPorServicio(elemento);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        addGanar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GanarDialogFragment ganarDialogFragment = new GanarDialogFragment();
                ganarDialogFragment.show(getActivity().getSupportFragmentManager(), "GanarFragmentDialog");
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(GanarViewModel.class);
        // TODO: Use the ViewModel
    }

    public void getGanar(){
        loadingDialog.starDialog();
        try {
            if (networkInfo != null && networkInfo.isConnected()) {
                firestore.collection("Ganar")
                        .whereEqualTo("estado", "ACTIVO")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast notificacion = Toast.makeText(getContext(),"Ha ocurrido un error listando las personas del Ganar",Toast.LENGTH_LONG);
                                    notificacion.show();
                                    loadingDialog.dismissDialog();
                                    return;
                                }

                                listGanar = new ArrayList<Ganar>();
                                int cant = 0;
                                for (QueryDocumentSnapshot doc : value) {
                                    if (doc.get("nombre") != null) {
                                        System.out.println(doc.getId() + " => " + doc.getString("comentario"));
                                        Ganar objGanar = null;
                                        objGanar = new Ganar();

                                        objGanar.setId(doc.getId());
                                        objGanar.setNombre(doc.getString("nombre"));
                                        objGanar.setTelefono(doc.getString("telefono"));
                                        objGanar.setDireccion(doc.getString("direccion"));
                                        objGanar.setInvitado_por(doc.getString("invitado_por"));
                                        objGanar.setLider(doc.getString("lider_12"));
                                        objGanar.setPeticion(doc.getString("peticion"));
                                        objGanar.setEstatus(doc.getString("estatus"));
                                        objGanar.setServicio(doc.getString("servicio"));
                                        objGanar.setFechaServicio(doc.getString("fecha_servicio"));
                                        objGanar.setComentario(doc.getString("comentario"));
                                        listGanar.add(objGanar);
                                        cant++;
                                    }
                                }
                                cantidad.setText("Total: "+cant);
                                loadingDialog.dismissDialog();

                                AdaptadorGanar adaptador = new AdaptadorGanar(getActivity());
                                lvGanar.setAdapter(adaptador);

                                if (listGanar.size() == 0){
                                    Toast.makeText(getContext(),"No hay información para mostrar.",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            } else {
                loadingDialog.dismissDialog();
                Toast.makeText(this.getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            loadingDialog.dismissDialog();
            Toast.makeText(getContext(),"Ha ocurrido un error!.",Toast.LENGTH_LONG).show();
        }
    }

    public void getGanarPorServicio(String servicio){
        loadingDialog.starDialog();
        try{
            if (networkInfo != null && networkInfo.isConnected()) {
                firestore.collection("Ganar")
                        .whereEqualTo("servicio", servicio)
                        .whereEqualTo("estado", "ACTIVO")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast notificacion = Toast.makeText(getContext(),"Ha ocurrido un error listando las personas del Ganar",Toast.LENGTH_LONG);
                                    loadingDialog.dismissDialog();
                                    return;
                                }

                                listGanar = new ArrayList<Ganar>();
                                int cant = 0;
                                for (QueryDocumentSnapshot doc : value) {
                                    if (doc.get("nombre") != null) {
                                        System.out.println(doc.getId() + " => " + doc.getString("comentario"));
                                        Ganar objGanar = null;
                                        objGanar = new Ganar();

                                        objGanar.setId(doc.getId());
                                        objGanar.setNombre(doc.getString("nombre"));
                                        objGanar.setTelefono(doc.getString("telefono"));
                                        objGanar.setDireccion(doc.getString("direccion"));
                                        objGanar.setInvitado_por(doc.getString("invitado_por"));
                                        objGanar.setPeticion(doc.getString("peticion"));
                                        objGanar.setEstatus(doc.getString("estatus"));
                                        objGanar.setServicio(doc.getString("servicio"));
                                        objGanar.setComentario(doc.getString("comentario"));
                                        objGanar.setLider(doc.getString("lider_12"));
                                        objGanar.setFechaServicio(doc.getString("fecha_servicio"));
                                        listGanar.add(objGanar);
                                        cant++;
                                    }
                                }
                                cantidad.setText("Total: "+cant);
                                AdaptadorGanar adaptador = new AdaptadorGanar(getActivity());
                                lvGanar.setAdapter(adaptador);

                                if (listGanar.size() == 0){
                                    Toast.makeText(getContext(),"No hay información para mostrar.",Toast.LENGTH_LONG).show();
                                }

                                loadingDialog.dismissDialog();
                            }
                        });
            } else {
                loadingDialog.dismissDialog();
                Toast notificacion=Toast.makeText(this.getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG);
                notificacion.show();
            }
        }catch (Exception e){
            loadingDialog.dismissDialog();
            Toast.makeText(getContext(),"Ha ocurrido un error!.",Toast.LENGTH_LONG).show();
        }

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
                                listServicios.add("-- Todos los Servicios --");
                                listInServicios.add("0");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    System.out.println(document.getId() + " => " + document.getString("nombre"));

                                    listServicios.add(document.getString("nombre"));
                                    listInServicios.add(document.getString("numero"));
                                }
                                ArrayAdapter adapterSp = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, listServicios);
                                spServicios.setAdapter(adapterSp);
                            } else {
                                loadingDialog.dismissDialog();
                                Toast notificacion = Toast.makeText(getContext(),"Ha ocurrido un error listado los Servicios",Toast.LENGTH_LONG);
                                notificacion.show();
                            }
                        }
                    });
        } else {
            loadingDialog.dismissDialog();
            Toast.makeText(this.getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
        }

    }

    class AdaptadorGanar extends ArrayAdapter<Ganar> {

        private Context context;
        AppCompatActivity appCompatActivity;

        AdaptadorGanar(Context context) {
            super(context, R.layout.cell_layout_ganar, listGanar);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            view = LayoutInflater.from(context).inflate(R.layout.cell_layout_ganar, null);

            TextView nombre = (TextView) view.findViewById(R.id.fecha);
            nombre.setText(listGanar.get(position).getNombre());

            TextView telefono = (TextView) view.findViewById(R.id.telefono);
            telefono.setText("Telf.: " + listGanar.get(position).getTelefono());

            TextView invitado_por = (TextView) view.findViewById(R.id.invitado_por);
            invitado_por.setText("Invitado por: " + listGanar.get(position).getInvitado_por());

            TextView direccion = (TextView) view.findViewById(R.id.direccion);
            direccion.setText("Dirección: " + listGanar.get(position).getDireccion());

            String invitado = listGanar.get(position).getInvitado_por();
            String lider = listGanar.get(position).getLider();
            String invitadoAux =  "";

            if (!invitado.equals("")){
                invitadoAux =  "("+invitado+")";
            }
            if (lider == null){
                invitado_por.setText("Invitado por: " + invitado);
            }else{
                invitado_por.setText("Invitado por: " + lider + invitadoAux);
            }

            TextView estatus = (TextView) view.findViewById(R.id.estatus);
            estatus.setText(" " + listGanar.get(position).getEstatus() + " ");

            TextView servicio = (TextView) view.findViewById(R.id.servicio);
            servicio.setText( listGanar.get(position).getServicio() + "º Servicio   |  Fecha: " + listGanar.get(position).getFechaServicio());

            if (listGanar.get(position).getEstatus().toString().equals("NUEVO")) {
                estatus.setBackgroundTintList(getResources().getColorStateList(R.color._amarillo));
            }else if (listGanar.get(position).getEstatus().toString().equals("CONTESTÓ")) {
                estatus.setBackgroundTintList(getResources().getColorStateList(R.color._verde));
            }else if (listGanar.get(position).getEstatus().toString().equals("NO CONTESTÓ")){
                estatus.setBackgroundTintList(getResources().getColorStateList(R.color._rojo));
            }else{
                estatus.setBackgroundTintList(getResources().getColorStateList(R.color._amarillo));
                estatus.setText(" NUEVO ");
            }

            ImageButton editar = (ImageButton) view.findViewById(R.id.editar);
            ImageButton llamar = (ImageButton) view.findViewById(R.id.llamar);
            ImageButton comentar = (ImageButton) view.findViewById(R.id.comentar);
            ImageButton eliminar = (ImageButton) view.findViewById(R.id.eliminar);

            if (auth.getCurrentUser().getEmail().equals("adminiviluz@gmail.com")) {
                eliminar.setVisibility(View.VISIBLE);
                llamar.setVisibility(View.GONE);
            }else{
                eliminar.setVisibility(View.GONE);
            }

            editar.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {

                      SharedPreferences preferencias = getContext().getSharedPreferences("datos",Context.MODE_PRIVATE);
                      SharedPreferences.Editor editor = preferencias.edit();
                      editor.putString("idGanar", listGanar.get(position).getId());
                      editor.putString("nombre", listGanar.get(position).getNombre());
                      editor.putString("telefono", listGanar.get(position).getTelefono());
                      editor.putString("direccion", listGanar.get(position).getDireccion());
                      editor.putString("invitado_por", listGanar.get(position).getInvitado_por());
                      editor.putString("lider_12", listGanar.get(position).getLider());
                      editor.putString("peticion", listGanar.get(position).getPeticion());
                      editor.putString("estatus", listGanar.get(position).getEstatus());
                      editor.putString("comentario", listGanar.get(position).getComentario());
                      editor.putString("servicio", listGanar.get(position).getServicio());
                      editor.putString("fecha_servicio", listGanar.get(position).getFechaServicio());
                      editor.commit();

                      EditarGanarDialogFragment editarGanarDialogFragment = new EditarGanarDialogFragment();
                      editarGanarDialogFragment.show(getActivity().getSupportFragmentManager(), "EditarGanarFragmentDialog");
                  }
            });

            llamar.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    String numero = listGanar.get(position).getTelefono();

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

            comentar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences preferencias = getContext().getSharedPreferences("datos",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferencias.edit();
                    editor.putString("idGanar", listGanar.get(position).getId());
                    editor.putString("nombre", listGanar.get(position).getNombre().toUpperCase());
                    editor.putString("telefono", listGanar.get(position).getTelefono());
                    editor.putString("estatus", listGanar.get(position).getEstatus());
                    editor.putString("comentario", listGanar.get(position).getComentario());
                    editor.commit();

                    ComentarioGanarDialogFragment comentarioGanarDialogFragment = new ComentarioGanarDialogFragment();
                    comentarioGanarDialogFragment.show(getActivity().getSupportFragmentManager(), "ComentarioGanarDialogFragment");
                }
            });

            eliminar.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    String nombre = listGanar.get(position).getNombre();
                    String idGanar = listGanar.get(position).getId();

                    if (!nombre.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("¿Desea elimiar a " + nombre +"?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        firestore.collection("Ganar").document(idGanar)
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(),"Persona eliminada con éxito.",Toast.LENGTH_LONG).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(),"Ha ocurrido un problema.",Toast.LENGTH_LONG).show();
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
                    }
                }
            });

            return view;
        }
    }


}
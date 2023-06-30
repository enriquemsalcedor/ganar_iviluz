package com.example.iviluz_app.ui.reporte_ganar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.DatePickerFragment;
import com.example.iviluz_app.R;
import com.example.iviluz_app.databinding.ReporteGanarFragmentBinding;
import com.example.iviluz_app.dialogos.InformacionDialogFragment;
import com.example.iviluz_app.dialogos.ResumenGanarDialogFragment;
import com.example.iviluz_app.modelos.Ganar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import utilidades.LoadingDialog;

public class ReporteGanarFragment extends Fragment {

    private ReporteGanarViewModel mViewModel;
    private ReporteGanarFragmentBinding binding;

    private EditText fechaDesde;
    private EditText fechaHasta;
    private Spinner spServicios;
    private Spinner spLideres;
    private ListView lvGanar;

    private ImageButton buscar;
    private ImageButton compartir;

    private ImageButton resumen;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    private ArrayList<String> listServicios;
    private ArrayList<String> listInServicios;

    private ArrayList<String> listMinisterio;
    private ArrayList<String> listIdMinisterio;
    private ArrayList<Ganar> listGanar;
    private String servicio = "";
    private String lider = "";
    private Date fechaD;
    private Date fechaH;
    private Date fechaServ;
    private String msjCompartir = "";
    private String servicioSelect = "";
    private String liderSelect = "";



    public static ReporteGanarFragment newInstance() {
        return new ReporteGanarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ReporteGanarFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        spServicios = (Spinner) view.findViewById(R.id.spServicios);
        fechaDesde = (EditText) view.findViewById(R.id.fechaDesde);
        fechaHasta = (EditText) view.findViewById(R.id.fechaHasta);
        spLideres = (Spinner) view.findViewById(R.id.spLideres);
        lvGanar = (ListView) view.findViewById(R.id.lvGanar);
        buscar = (ImageButton) view.findViewById(R.id.buscar);
        compartir = (ImageButton) view.findViewById(R.id.compartir);
        resumen = (ImageButton) view.findViewById(R.id.resumen);

        listServicios = new ArrayList<>();
        listInServicios = new ArrayList<>();

        listMinisterio = new ArrayList<>();
        listIdMinisterio = new ArrayList<>();

        compartir.setEnabled(false);
        resumen.setEnabled(false);

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        getServicios();
        getLideres();

        fechaDesde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.fechaDesde:
                        showDatePickerDialog(fechaDesde);
                        break;
                }
            }
        });

        fechaHasta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.fechaHasta:
                        showDatePickerDialog(fechaHasta);
                        break;
                }
            }
        });

        spServicios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                servicio = listInServicios.get(i);
                servicioSelect = listServicios.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spLideres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                lider = listIdMinisterio.get(i);
                liderSelect = listMinisterio.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        resumen.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(listGanar.size()>0){
                         msjCompartir = "*REPORTE MINISTERIO DEL GANAR*" + "\n";

                         if(!lider.isEmpty()){
                             msjCompartir = msjCompartir + "*LIDER:* " + liderSelect + "\n";
                         }
                         if(!servicio.equals("0")){
                             msjCompartir = msjCompartir + "*SERVICIO:* " + servicioSelect + "\n";
                         }
                         if(!fechaDesde.getText().toString().isEmpty()
                                 && !fechaHasta.getText().toString().isEmpty()) {
                             msjCompartir = msjCompartir + "*FECHA:* " +
                                     fechaDesde.getText().toString() + " - " +
                                     fechaHasta.getText().toString() + "\n";
                         }

                         for (Ganar item : listGanar) {
                             msjCompartir = msjCompartir + "\n" +
                                     "*Nombre:* " + item.getNombre() + "\n" +
                                     "*Telefono:* " + item.getTelefono() + "\n" +
                                     "*Direccion:* " + item.getDireccion() + "\n" +
                                     "*Fecha que asistio:* " + item.getFechaServicio() + "\n" +
                                     "*Peticion:* " + item.getPeticion() + "\n" +
                                     "*Estatus:* " + item.getEstatus() + "\n";
                         }

                     SharedPreferences user = getContext().getSharedPreferences("resumen", Context.MODE_PRIVATE);
                     SharedPreferences.Editor resumenRepor = user.edit();
                     resumenRepor.putString("mensaje", msjCompartir);

                     resumenRepor.commit();

                     ResumenGanarDialogFragment resumen = new ResumenGanarDialogFragment();
                     resumen.show(getActivity().getSupportFragmentManager(), "ResumenGanarDialogFragment");

                 }
             }
        });

        compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listGanar != null) {
                    msjCompartir = "*REPORTE MINISTERIO DEL GANAR*" + "\n";

                    if(!lider.isEmpty()){
                        msjCompartir = msjCompartir + "*LIDER:* " + liderSelect + "\n";
                    }
                    if(!servicio.equals("0")){
                        msjCompartir = msjCompartir + "*SERVICIO:* " + servicioSelect + "\n";
                    }
                    if(!fechaDesde.getText().toString().isEmpty()
                            && !fechaHasta.getText().toString().isEmpty()) {
                        msjCompartir = msjCompartir + "*FECHA:* " +
                                fechaDesde.getText().toString() + " - " +
                                fechaHasta.getText().toString() + "\n";
                    }

                    for (Ganar item : listGanar) {
                        msjCompartir = msjCompartir + "\n" +
                                "*Nombre:* " + item.getNombre() + "\n" +
                                "*Telefono:* " + item.getTelefono() + "\n" +
                                "*Direccion:* " + item.getDireccion() + "\n" +
                                "*Fecha que asistio:* " + item.getFechaServicio() + "\n" +
                                "*Peticion:* " + item.getPeticion() + "\n" +
                                "*Estatus:* " + item.getEstatus() + "\n";
                    }
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                    intent.putExtra(Intent.EXTRA_TEXT, msjCompartir);

                    try {
                        getContext().startActivity(intent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        ex.printStackTrace();
                        Toast.makeText(getContext(),"El dispositivo no tiene instalado WhatsApp.",Toast.LENGTH_LONG).show();

                    }
                }else{
                    Toast.makeText(getContext(),"No hay informacion para compartir",Toast.LENGTH_LONG).show();
                }
            }
        });

        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGanar(servicio, lider);
            }
        });

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ReporteGanarViewModel.class);
        // TODO: Use the ViewModel
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
                                listServicios.add("-- Servicio --");
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
                            }
                        }
                    });
        } else {
            Toast.makeText(this.getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
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
                                listMinisterio.add("-- Lider --");
                                listIdMinisterio.add("");
                                listMinisterio.add("Sin lider");
                                listIdMinisterio.add("0");

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getString("nombre") != null) {
                                        System.out.println(document.getId() + " => " + document.getString("nombre"));

                                        listIdMinisterio.add(document.getId());
                                        listMinisterio.add(document.getString("nombre"));

                                    }
                                }
                                ArrayAdapter adapterSp = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, listMinisterio);
                                spLideres.setAdapter(adapterSp);

                            } else {
                                Toast.makeText(getContext(), "Ha ocurrido un error listado los Lideres", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this.getContext(), "No tienes conexión a internet en este momento.", Toast.LENGTH_LONG).show();
        }
    }

    public void getGanar(String servicio, String lider){

        if(!fechaDesde.getText().toString().isEmpty()
                && !fechaHasta.getText().toString().isEmpty()){

            fechaD = getDateFormat(fechaDesde.getText().toString());
            fechaH = getDateFormat(fechaHasta.getText().toString());

            if(fechaD.after(fechaH)){
                Toast.makeText(getContext(),"El intervalo de fecha es incorrecto.",Toast.LENGTH_LONG).show();
                fechaDesde.setText("");
                fechaHasta.setText("");

                return;
            }
        }

        if (networkInfo != null && networkInfo.isConnected()) {

            loadingDialog.starDialog();
            Query query;

            query = firestore.collection("Ganar");

            if(!servicio.equals("0")){
                query = query.whereEqualTo("servicio", servicio);
            }
            if (!lider.isEmpty()) {
                if (lider.equals("0")){
                    lider = "";
                }
                query = query.whereEqualTo("id_ministerio", lider);
            }

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            int cant1 = 0;
                            int cant2 = 0;
                            int cant3 = 0;
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

                                    if(!fechaDesde.getText().toString().isEmpty()
                                            && !fechaHasta.getText().toString().isEmpty()){

                                        fechaD = getDateFormat(fechaDesde.getText().toString());
                                        fechaH = getDateFormat(fechaHasta.getText().toString());
                                        String fechaServicio = objGanar.setFechaServicio(doc.getString("fecha_servicio"));

                                        fechaServ = getDateFormat(fechaServicio);

                                        if(!fechaD.after(fechaServ) && !fechaH.before(fechaServ)){
                                            listGanar.add(objGanar);
                                            cant++;

                                            if(doc.getString("estatus").equals("NUEVO")){
                                                cant1++;
                                            }
                                            if(doc.getString("estatus").equals("CONTESTÓ")){
                                                cant2++;
                                            }
                                            if(doc.getString("estatus").equals("NO CONTESTÓ")){
                                                cant3++;
                                            }

                                        }
                                    }else {
                                        listGanar.add(objGanar);
                                        cant++;
                                        if(doc.getString("estatus").equals("NUEVO")){
                                            cant1++;
                                        }
                                        if(doc.getString("estatus").equals("CONTESTÓ")){
                                            cant2++;
                                        }
                                        if(doc.getString("estatus").equals("NO CONTESTÓ")){
                                            cant3++;
                                        }
                                    }
                                }
                            }
                            SharedPreferences user = getContext().getSharedPreferences("resumen", Context.MODE_PRIVATE);
                            SharedPreferences.Editor resumenRepor = user.edit();
                            resumenRepor.putString("total", cant+"");
                            resumenRepor.putString("nuevo", cant1+"");
                            resumenRepor.putString("contesto", cant2+"");
                            resumenRepor.putString("no_contesto", cant3+"");
                            resumenRepor.commit();

                            AdaptadorGanar adaptador = new AdaptadorGanar(getActivity());
                            lvGanar.setAdapter(adaptador);

                            if (listGanar.size() == 0){
                                Toast.makeText(getContext(),"No hay información para mostrar.",Toast.LENGTH_LONG).show();
                            }else{
                                compartir.setEnabled(true);
                                resumen.setEnabled(true);
                            }

                            loadingDialog.dismissDialog();
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
                invitado_por.setText("Invitado por: " + lider +" "+ invitadoAux);
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
            }

            ImageButton editar = (ImageButton) view.findViewById(R.id.editar);
            ImageButton llamar = (ImageButton) view.findViewById(R.id.llamar);
            ImageButton comentar = (ImageButton) view.findViewById(R.id.comentar);
            ImageButton eliminar = (ImageButton) view.findViewById(R.id.eliminar);

            editar.setVisibility(View.GONE);
            eliminar.setVisibility(View.GONE);
            comentar.setVisibility(View.GONE);

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

            comentar.setVisibility(View.GONE);

            return view;
        }
    }

    public static Date getDateFormat(String formatPattern) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy");
        java.sql.Date fechaConvertida = null;

        try {
            Date parsed = dateFormat.parse(formatPattern);
            fechaConvertida = new java.sql.Date(parsed.getTime());
        } catch (Exception exception) {
            System.out.println("Error occurred" + exception.getMessage());
        }
        return fechaConvertida;
    }

    private void showDatePickerDialog(final EditText editText) {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                final String selectedDate = twoDigits(day) + "/" + twoDigits(month+1) + "/" + year;
                editText.setText(selectedDate);
            }
        });

        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

}


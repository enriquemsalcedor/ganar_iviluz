package com.example.iviluz_app.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.iviluz_app.MainActivity;
import com.example.iviluz_app.R;
import com.example.iviluz_app.SesionActivity;
import com.example.iviluz_app.databinding.FragmentHomeBinding;
import com.example.iviluz_app.dialogos.ComentarioGanarDialogFragment;
import com.example.iviluz_app.dialogos.ConteoDialogFragment;
import com.example.iviluz_app.dialogos.EditarGanarDialogFragment;
import com.example.iviluz_app.dialogos.EditarInformacionDialogFragment;
import com.example.iviluz_app.dialogos.GanarDialogFragment;
import com.example.iviluz_app.dialogos.InformacionDialogFragment;
import com.example.iviluz_app.dialogos.LiderDialogFragment;
import com.example.iviluz_app.modelos.Ganar;
import com.example.iviluz_app.modelos.Informacion;
import com.example.iviluz_app.modelos.Inicio;
import com.example.iviluz_app.ui.ganar.GanarFragment;
import com.example.iviluz_app.ui.ganar.GanarViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel mViewModel;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    private ListView lvGanar;
    private ListView lvNoticia;

    private Button add;

    private ArrayList<Informacion> listInfo;

    private ArrayList<Inicio> listReport;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        lvGanar = (ListView) view.findViewById(R.id.lvGanar);
        lvNoticia = (ListView) view.findViewById(R.id.lvNoticia);
        add = (Button) view.findViewById(R.id.add);
        lvGanar.setEnabled(false);

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        getGanar();
        getInformaciones();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InformacionDialogFragment informacionDialogFragment = new InformacionDialogFragment();
                informacionDialogFragment.show(getActivity().getSupportFragmentManager(), "InformacionDialogFragment");
            }
        });

        if(auth.getCurrentUser() != null){
            if (!auth.getCurrentUser().getEmail().equals("adminiviluz@gmail.com")) {
                add.setVisibility(View.INVISIBLE);
            }
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void getGanar(){
        try {
            if (networkInfo != null && networkInfo.isConnected()) {
                firestore.collection("Ganar")
                        .whereEqualTo("estado", "ACTIVO")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast.makeText(getContext(),"Ha ocurrido un error listando las personas del Ganar",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                int cant1 = 0;
                                int cant2 = 0;
                                int cant3 = 0;
                                for (QueryDocumentSnapshot doc : value) {
                                    if (doc.get("nombre") != null) {
                                        if(doc.getString("servicio").equals("1")){
                                            cant1++;
                                        }
                                        if(doc.getString("servicio").equals("2")){
                                            cant2++;
                                        }
                                        if(doc.getString("servicio").equals("3")){
                                            cant3++;
                                        }
                                    }
                                }
                                Inicio obj = new Inicio();
                                listReport = new ArrayList<Inicio>();
                                obj.setCantidad_primero(cant1);
                                obj.setCantidad_segundo(cant2);
                                obj.setCantidad_tercero(cant3);

                                listReport.add(obj);
                                HomeFragment.AdaptadorGanar adaptador = new HomeFragment.AdaptadorGanar(getActivity());
                                lvGanar.setAdapter(adaptador);

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

    public void getInformaciones(){
        try {
            if (networkInfo != null && networkInfo.isConnected()) {
                firestore.collection("Informacion")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast.makeText(getContext(),"Ha ocurrido un error listando las personas del Ganar",Toast.LENGTH_LONG).show();
                                    return;
                                }

                                listInfo = new ArrayList<Informacion>();
                                int cant = 0;
                                for (QueryDocumentSnapshot doc : value) {
                                    if (doc.get("titulo") != null) {
                                        System.out.println(doc.getId() + " => " + doc.getString("comentario"));
                                        Informacion obj = null;
                                        obj = new Informacion();

                                        obj.setId(doc.getId());
                                        obj.setTitulo(doc.getString("titulo"));
                                        obj.setContenido(doc.getString("contenido"));
                                        obj.setFecha(doc.getString("fecha"));

                                        listInfo.add(obj);
                                    }
                                }

                                HomeFragment.AdaptadorNoticias adaptador = new HomeFragment.AdaptadorNoticias(getActivity());
                                lvNoticia.setAdapter(adaptador);
                            }
                        });
            } else {
                Toast.makeText(this.getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            Toast.makeText(getContext(),"Ha ocurrido un error!.",Toast.LENGTH_LONG).show();
        }
    }

    class AdaptadorGanar extends ArrayAdapter<Inicio> {

        private Context context;
        AppCompatActivity appCompatActivity;

        AdaptadorGanar(Context context) {
            super(context, R.layout.cell_ganar_inicio, listReport);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            view = LayoutInflater.from(context).inflate(R.layout.cell_ganar_inicio, null);

            TextView cantidad1 = (TextView) view.findViewById(R.id.cantidad1);
            TextView cantidad2 = (TextView) view.findViewById(R.id.cantidad2);
            TextView cantidad3 = (TextView) view.findViewById(R.id.cantidad3);

            cantidad1.setText(String.valueOf(listReport.get(position).getCantidad_primero()));
            cantidad2.setText(String.valueOf(listReport.get(position).getCantidad_segundo()));
            cantidad3.setText(String.valueOf(listReport.get(position).getCantidad_tercero()));


            return view;
        }
    }

    class AdaptadorNoticias extends ArrayAdapter<Informacion> {

        private Context context;
        AppCompatActivity appCompatActivity;

        AdaptadorNoticias(Context context) {
            super(context, R.layout.cell_informaciones, listInfo);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            view = LayoutInflater.from(context).inflate(R.layout.cell_informaciones, null);

            TextView titulo = (TextView) view.findViewById(R.id.titulo);
            titulo.setText(listInfo.get(position).getTitulo());

            TextView contenido = (TextView) view.findViewById(R.id.contenido);
            contenido.setText(listInfo.get(position).getContenido());

            TextView fecha = (TextView) view.findViewById(R.id.fecha);
            fecha.setText("Publicado: " + listInfo.get(position).getFecha());

            ImageButton editar = (ImageButton) view.findViewById(R.id.editar);
            ImageButton eliminar = (ImageButton) view.findViewById(R.id.eliminar);

            if (auth.getCurrentUser().getEmail().equals("adminiviluz@gmail.com")) {
                eliminar.setVisibility(View.VISIBLE);
                editar.setVisibility(View.VISIBLE);
            }else{
                eliminar.setVisibility(View.GONE);
                editar.setVisibility(View.GONE);
            }

            editar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences preferencias = getContext().getSharedPreferences("datos",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferencias.edit();
                    editor.putString("id", listInfo.get(position).getId());
                    editor.putString("titulo", listInfo.get(position).getTitulo());
                    editor.putString("contenido", listInfo.get(position).getContenido());
                    editor.putString("fecha", listInfo.get(position).getFecha());

                    editor.commit();

                    EditarInformacionDialogFragment informacionDialogFragment = new EditarInformacionDialogFragment();
                    informacionDialogFragment.show(getActivity().getSupportFragmentManager(), "EditarInformacionDialogFragment");               }
            });

            eliminar.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    String titulo = listInfo.get(position).getTitulo();
                    String ID = listInfo.get(position).getId();

                    if (!titulo.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("¿Desea elimiar?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        firestore.collection("Informacion").document(ID)
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
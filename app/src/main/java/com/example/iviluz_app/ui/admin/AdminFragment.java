package com.example.iviluz_app.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.example.iviluz_app.databinding.AdminFragmentBinding;
import com.example.iviluz_app.modelos.Inicio;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utilidades.LoadingDialog;

public class AdminFragment extends Fragment {

    private AdminFragmentBinding binding;
    private AdminViewModel mViewModel;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    private ListView lvGanar;
    private Button procesar;
    private TextView infoTotal;

    private ArrayList<Inicio> listReport;
    private ArrayList<String> listDoc;

    int total = 0;
    int bien = 0;

    public static AdminFragment newInstance() {
        return new AdminFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        lvGanar = (ListView) view.findViewById(R.id.lvGanar);
        procesar = (Button) view.findViewById(R.id.procesar);
        infoTotal = (TextView) view.findViewById(R.id.infoTotal);

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        getGanar();

        procesar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                procesar();
            }
        });

        return view;
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
                                listDoc = new ArrayList<String>();
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
                                        total++;

                                        listDoc.add(doc.getId());
                                    }
                                }
                                Inicio obj = new Inicio();
                                listReport = new ArrayList<Inicio>();
                                obj.setCantidad_primero(cant1);
                                obj.setCantidad_segundo(cant2);
                                obj.setCantidad_tercero(cant3);

                                listReport.add(obj);
                                AdminFragment.AdaptadorGanar adaptador = new AdminFragment.AdaptadorGanar(getActivity());
                                lvGanar.setAdapter(adaptador);
                                infoTotal.setText("Personas Activas: " + String.valueOf(total));

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
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        // TODO: Use the ViewModel
    }

    public void procesar(){
        if(listDoc.size() != 0)
            try {
                if (networkInfo != null && networkInfo.isConnected()) {
                    loadingDialog.starDialog();
                    Map<String, Object> ganarMap = new HashMap<>();
                    ganarMap.put("estado", "PROCESADO");
                    loadingDialog.starDialog();

                    for (String i : listDoc) {
                        firestore.collection("Ganar").document(i.toString()).update(ganarMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        bien++;
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
                    if (bien == listDoc.size()) {
                        Toast.makeText(getContext(), total + " Personas procesadas.", Toast.LENGTH_LONG).show();
                    }
                    loadingDialog.dismissDialog();
                } else {
                    loadingDialog.dismissDialog();
                    Toast.makeText(getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                loadingDialog.dismissDialog();
                Toast.makeText(getContext(),"Ha ocurrido un error!.",Toast.LENGTH_LONG).show();
            }
        }
    }


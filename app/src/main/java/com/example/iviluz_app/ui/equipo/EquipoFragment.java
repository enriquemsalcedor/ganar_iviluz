package com.example.iviluz_app.ui.equipo;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.example.iviluz_app.databinding.EquipoFragmentBinding;
import com.example.iviluz_app.dialogos.EditarLiderDialogFragment;
import com.example.iviluz_app.dialogos.EquipoDialogFragment;
import com.example.iviluz_app.dialogos.InfoLiderDialogFragment;
import com.example.iviluz_app.modelos.Lider;
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

public class EquipoFragment extends Fragment {

    private EquipoFragmentBinding binding;
    private EquipoViewModel mViewModel;

    private ListView lvLideres;
    private Button addLider;

    private ArrayList<Lider> listLider;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;


    public static EquipoFragment newInstance() {
        return new EquipoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = EquipoFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        lvLideres = (ListView) view.findViewById(R.id.lvLideres);
        addLider = (Button) view.findViewById(R.id.addLider);

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        getLideres();

        addLider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EquipoDialogFragment equipoDialogFragment = new EquipoDialogFragment();
                equipoDialogFragment.show(getActivity().getSupportFragmentManager(), "EquipoDialogFragment");
            }
        });

        return view;
    }

    public void getLideres(){
        loadingDialog.starDialog();

        if (networkInfo != null && networkInfo.isConnected()) {
            firestore.collection("Ministerio")
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

                            listLider = new ArrayList<Lider>();
                            for (QueryDocumentSnapshot doc : value) {
                                if (doc.get("nombre") != null) {
                                    System.out.println(doc.getId() + " => " + doc.getString("nombre"));
                                    Lider objLider = null;
                                    objLider = new Lider();

                                    objLider.setId(doc.getId());
                                    objLider.setNombre(doc.getString("nombre"));
                                    objLider.setTelefono(doc.getString("telefono"));
                                    objLider.setEmail(doc.getString("email"));
                                    objLider.setEstatus(doc.getString("estatus"));

                                    listLider.add(objLider);
                                }
                            }
                            EquipoFragment.AdaptadorLider adaptador = new EquipoFragment.AdaptadorLider(getActivity());
                            lvLideres.setAdapter(adaptador);

                            if (listLider.size() == 0){
                                Toast.makeText(getContext(),"No hay información para mostrar.",Toast.LENGTH_LONG).show();
                            }

                            loadingDialog.dismissDialog();
                        }
                    });
        } else {
            loadingDialog.dismissDialog();
            Toast.makeText(this.getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
        }
    }

    class AdaptadorLider extends ArrayAdapter<Lider> {

        private Context context;
        AppCompatActivity appCompatActivity;

        AdaptadorLider(Context context) {
            super(context, R.layout.cell_layout_usuario, listLider);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            view = LayoutInflater.from(context).inflate(R.layout.cell_layout_lideres, null);

            TextView nombre = (TextView) view.findViewById(R.id.nombre);
            nombre.setText(listLider.get(position).getNombre().toUpperCase());

            ImageButton editar = (ImageButton) view.findViewById(R.id.editar);
            ImageButton activar = (ImageButton) view.findViewById(R.id.activar);
            ImageButton desactivar = (ImageButton) view.findViewById(R.id.desactivar);

            if (listLider.get(position).getEstatus().equals("ACTIVO")){
                activar.setVisibility(View.GONE);
            }else{
                desactivar.setVisibility(View.GONE);
            }

            editar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences preferencias = getContext().getSharedPreferences("datos",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferencias.edit();
                    editor.putString("idMinisterio", listLider.get(position).getId());
                    editor.putString("nombre", listLider.get(position).getNombre());
                    editor.putString("telefono", listLider.get(position).getTelefono());
                    editor.putString("direccion", listLider.get(position).getEmail());

                    editor.commit();

                    EditarLiderDialogFragment editarLiderDialogFragment = new EditarLiderDialogFragment();
                    editarLiderDialogFragment.show(getActivity().getSupportFragmentManager(), "EditarLiderDialogFragment");

                }
            });

            activar.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (networkInfo != null && networkInfo.isConnected()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("¿Desea activar este registro?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        loadingDialog.starDialog();
                                        Map<String, Object> minMap = new HashMap<>();
                                        minMap.put("estatus", "ACTIVO");
                                        firestore.collection("Ministerio").document(listLider.get(position).getId()).update(minMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        loadingDialog.dismissDialog();
                                                        Toast.makeText(getContext(), "Activado con exito", Toast.LENGTH_SHORT).show();
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
                                })
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();

                    } else {
                        loadingDialog.dismissDialog();
                        Toast.makeText(getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
                    }
                }
            });

            desactivar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (networkInfo != null && networkInfo.isConnected()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("¿Desea desactivar este registro?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        loadingDialog.starDialog();
                                        Map<String, Object> minMap = new HashMap<>();
                                        minMap.put("estatus", "INACTIVO");
                                        firestore.collection("Ministerio").document(listLider.get(position).getId()).update(minMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        loadingDialog.dismissDialog();
                                                        Toast.makeText(getContext(), "Desactivado con exito", Toast.LENGTH_SHORT).show();
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
                                })
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();

                    } else {
                        loadingDialog.dismissDialog();
                        Toast.makeText(getContext(),"No tienes conexión a internet en este momento.",Toast.LENGTH_LONG).show();
                    }
                }
            });

            return view;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EquipoViewModel.class);
        // TODO: Use the ViewModel
    }

}
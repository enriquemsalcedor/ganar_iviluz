package com.example.iviluz_app.ui.usuarios;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.example.iviluz_app.databinding.UsuarioFragmentBinding;
import com.example.iviluz_app.dialogos.EditarUsuarioDialogFragment;
import com.example.iviluz_app.dialogos.UsuarioDialogFragment;
import com.example.iviluz_app.modelos.Lider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utilidades.LoadingDialog;

public class UsuarioFragment extends Fragment {

    private UsuarioFragmentBinding binding;
    private UsuarioViewModel mViewModel;

    private ListView lvUsuario;
    private Spinner nivel;
    private Button addUsuario;
    private EditText busqueda;

    private ArrayList<Lider> listUsuarios;
    private ArrayList<String> listaNivel;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    public static UsuarioFragment newInstance() {
        return new UsuarioFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = UsuarioFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        lvUsuario = (ListView) view.findViewById(R.id.lvUsuario);
        addUsuario = (Button) view.findViewById(R.id.addUsuario);
        busqueda = (EditText) view.findViewById(R.id.busqueda);

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        listUsuarios = new ArrayList<Lider>();

        UsuarioFragment.AdaptadorUsuario adapter = new UsuarioFragment.AdaptadorUsuario(getActivity());

        getUsuarios();

        addUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsuarioDialogFragment usuarioDialogFragment = new UsuarioDialogFragment();
                usuarioDialogFragment.show(getActivity().getSupportFragmentManager(), "UsuarioDialogFragment");
            }
        });

        busqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.filtrar(busqueda.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    public void getUsuarios(){
        loadingDialog.starDialog();
        if (networkInfo != null && networkInfo.isConnected()) {
            firestore.collection("Usuario")
                    .whereNotEqualTo("estatus","2")
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

                            listUsuarios = new ArrayList<Lider>();
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
                                    objLider.setLider(doc.getString("lider"));

                                    listUsuarios.add(objLider);
                                }
                            }
                            UsuarioFragment.AdaptadorUsuario adaptador = new UsuarioFragment.AdaptadorUsuario(getActivity());
                            lvUsuario.setAdapter(adaptador);

                            if (listUsuarios.size() == 0){
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

    class AdaptadorUsuario extends ArrayAdapter<Lider> {

        private Context context;
        AppCompatActivity appCompatActivity;
        ArrayList<Lider> copyLideres = new ArrayList<>(listUsuarios);

        AdaptadorUsuario(Context context) {
            super(context, R.layout.cell_layout_usuario, listUsuarios);
            this.context = context;
            this.copyLideres.addAll(listUsuarios); // Crea una copia de los contactos
        }

        /* Filtra los datos del adaptador */
        public void filtrar(String texto) {

            // Elimina todos los datos del ArrayList que se cargan en los
            // elementos del adaptador
            listUsuarios.clear();

            // Si no hay texto: agrega de nuevo los datos del ArrayList copiado
            // al ArrayList que se carga en los elementos del adaptador
            if (texto.length() == 0) {
                System.out.println("texto.length() -->" + texto.length());
                listUsuarios.addAll(copyLideres);
            } else {
                System.out.println("listUsuarios.size()-------" + listUsuarios.size());
                System.out.println("copyLideres.size()-------" + copyLideres.size());
                // Recorre todos los elementos que contiene el ArrayList copiado
                // y dependiendo de si estos contienen el texto ingresado por el
                // usuario los agrega de nuevo al ArrayList que se carga en los
                // elementos del adaptador.
                for (Lider lider : copyLideres) {
                    System.out.println("-->" + lider.getNombre());
                    if (lider.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                        System.out.println("-->" + lider.getNombre().contains(texto));
                        listUsuarios.add(lider);
                    }
                }
            }

            // Actualiza el adaptador para aplicar los cambios
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            view = LayoutInflater.from(context).inflate(R.layout.cell_layout_usuario, null);

            TextView nombre = (TextView) view.findViewById(R.id.nombre);
            nombre.setText(listUsuarios.get(position).getNombre().toUpperCase());

            TextView telefono = (TextView) view.findViewById(R.id.telefono);
            telefono.setText("Telf.: " + listUsuarios.get(position).getTelefono());

            TextView email = (TextView) view.findViewById(R.id.email);
            email.setText("Email: " + listUsuarios.get(position).getEmail());

            ImageView estatus = (ImageView) view.findViewById(R.id.estatus);
            if (listUsuarios.get(position).getEstatus().equals("0")){
                estatus.setImageResource(R.drawable.ic_baseline_close_white_24);
                estatus.setBackgroundColor(Color.parseColor("#ff3333"));
            }else{
                estatus.setImageResource(R.drawable.ic_baseline_check_white_24);
                estatus.setBackgroundColor(Color.parseColor("#00bd00"));
            }

            ImageButton editar = (ImageButton) view.findViewById(R.id.editar);
            ImageButton enviarMsj = (ImageButton) view.findViewById(R.id.enviarMsj);
            ImageButton eliminar = (ImageButton) view.findViewById(R.id.eliminar);

            editar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences preferencias = getContext().getSharedPreferences("datos",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferencias.edit();
                    editor.putString("idUsuario", listUsuarios.get(position).getId());
                    editor.putString("nombre", listUsuarios.get(position).getNombre());
                    editor.putString("telefono", listUsuarios.get(position).getTelefono());
                    editor.putString("email", listUsuarios.get(position).getEmail());
                    editor.putString("lider", listUsuarios.get(position).getLider());
                    editor.putString("estatus", listUsuarios.get(position).getEstatus());
                    editor.commit();

                    EditarUsuarioDialogFragment editarUsuarioDialogFragment = new EditarUsuarioDialogFragment();
                    editarUsuarioDialogFragment.show(getActivity().getSupportFragmentManager(), "EditarUsuarioDialogFragment");
                }
            });

            enviarMsj.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    String numero = listUsuarios.get(position).getTelefono();
                    String usuario = listUsuarios.get(position).getEmail();
                    String nombre = listUsuarios.get(position).getNombre();
                    numero = numero.substring(1);
                    String numeroStg = "+58"+numero;

                    String msj = "*MINISTERIO DE GANAR* " +
                            "\n\nTe hemos creado un usuario para la aplicación " +
                            "del Ganar, pídele a tu coordinador que te envie el APK e instalala en tu dispositivo móvil." +
                            "\n\n Podrás ingresar a la app con estas credenciales:\n" +
                            "*Usuario:* " + usuario +"\n"+
                            "*Contraseña:* 123123" +
                            "\n\n _Dios te bendiga!_";
                    if (!numero.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("¿Desea enviar una notificación a \n"+ nombre+"?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        String uri = "whatsapp://send?phone=+" + numeroStg + "&text=" + msj.toString();
                                        intent.setData(Uri.parse(uri));

                                        startActivity(intent);
                                        try {
                                            getContext().startActivity(intent);
                                        } catch (android.content.ActivityNotFoundException ex) {

                                        }
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

            eliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (networkInfo != null && networkInfo.isConnected()) {
                        String idUsuario = listUsuarios.get(position).getId();
                        String nombre = listUsuarios.get(position).getNombre();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("¿Desea elimiara el usuario de \n" + nombre +"?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        loadingDialog.starDialog();

                                        Map<String, Object> eliminarMap = new HashMap<>();
                                        eliminarMap.put("estatus", "2");
                                        firestore.collection("Usuario").document(idUsuario).update(eliminarMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        loadingDialog.dismissDialog();
                                                        Toast.makeText(getContext(), "Usuario eliminado con exito", Toast.LENGTH_SHORT).show();
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
                    }else{
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
        mViewModel = new ViewModelProvider(this).get(UsuarioViewModel.class);
        // TODO: Use the ViewModel
    }




}
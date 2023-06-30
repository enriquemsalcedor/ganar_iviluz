package com.example.iviluz_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.dialogos.CambiarClaveDialogFragment;
import com.example.iviluz_app.dialogos.EditarUsuarioDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iviluz_app.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import utilidades.LoadingDialog;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private TextView nombreUsuario;
    private TextView emailUsuario;

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View headerView = navigationView.getHeaderView(0);
        nombreUsuario = (TextView) headerView.findViewById(R.id.nombreMinisterio);
        emailUsuario = (TextView) headerView.findViewById(R.id.emailUsuario);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        if (auth.getCurrentUser() == null){
            startActivity(new Intent(this, SesionActivity.class));

        } else {
            SharedPreferences user = getApplicationContext().getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);

            if(auth.getCurrentUser().getEmail().equals("adminiviluz@gmail.com")) {
                nombreUsuario.setText("USUARIO ADMINISTRADOR");
                emailUsuario.setText(auth.getCurrentUser().getEmail());
            }else{
                nombreUsuario.setText(user.getString("nombreUser", "").toUpperCase());
                emailUsuario.setText(auth.getCurrentUser().getEmail());

                if(user.getString("estatusUser", "").equals("0")){
                    auth.signOut();
                    startActivity(new Intent(getBaseContext(), SesionActivity.class));
                    finish();
                }

                if(user.getString("estatusUser", "").equals("2")){
                    auth.signOut();
                    Toast.makeText(getApplicationContext(), "Tu usuario se encuentra inactivo, no puedes iniciar sesión.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getBaseContext(), SesionActivity.class));
                    finish();
                }


            }
            hideItemMenu();
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_admin,
                R.id.nav_lider,
                R.id.nav_equipo,
                R.id.nav_ganar,
                R.id.nav_conteo,
                R.id.nav_reporte_ganar,
                R.id.nav_usuario)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void loginOut(MenuItem item) {

        if (item.toString().equals("Cerrar Sesión")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Desea cerrar la sesión?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            auth.signOut();
                            startActivity(new Intent(getBaseContext(), SesionActivity.class));
                            finish();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.cambiarclave) {
            if (auth.getCurrentUser().getEmail().equals("adminiviluz@gmail.com")) {
                Toast.makeText(getApplicationContext(), "El usuario Administrador no puede cambiar contraseña.", Toast.LENGTH_SHORT).show();
            }else{
                CambiarClaveDialogFragment CambiarClaveDialogFragment = new CambiarClaveDialogFragment();
                CambiarClaveDialogFragment.show(this.getSupportFragmentManager(), "CambiarClaveDialogFragment");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideItemMenu()
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();

        if(auth.getCurrentUser() != null) {
            if (auth.getCurrentUser().getEmail().equals("adminiviluz@gmail.com")) {

                nav_Menu.findItem(R.id.nav_reporte_ganar).setVisible(false);
                nav_Menu.findItem(R.id.nav_conteo).setVisible(false);

                nav_Menu.findItem(R.id.nav_ganar).setVisible(true);
                nav_Menu.findItem(R.id.nav_usuario).setVisible(true);
                nav_Menu.findItem(R.id.nav_admin).setVisible(true);
                nav_Menu.findItem(R.id.nav_equipo).setVisible(true);

            } else {
                nav_Menu.findItem(R.id.nav_ganar).setVisible(true);
                nav_Menu.findItem(R.id.nav_reporte_ganar).setVisible(true);
                nav_Menu.findItem(R.id.nav_equipo).setVisible(true);

                nav_Menu.findItem(R.id.nav_usuario).setVisible(false);
                nav_Menu.findItem(R.id.nav_admin).setVisible(false);
                nav_Menu.findItem(R.id.nav_conteo).setVisible(false);
            }
        }else{
            auth.signOut();
            startActivity(new Intent(getBaseContext(), SesionActivity.class));
            finish();
        }
    }



}
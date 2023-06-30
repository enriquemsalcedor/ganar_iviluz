package com.example.iviluz_app.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iviluz_app.R;

public class ResumenGanarDialogFragment extends DialogFragment {

    private Button compartir;
    private Button cancelar;

    private TextView cantidad1;
    private TextView cantidad2;
    private TextView cantidad3;
    private TextView infoTotal;

    String mensaje = "";


    public ResumenGanarDialogFragment() {
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
        View v = inflater.inflate(R.layout.fragment_resumen_ganar_dialog, null);
        builder.setView(v);

        cantidad1 = (TextView) v.findViewById(R.id.cantidad1);
        cantidad2 = (TextView) v.findViewById(R.id.cantidad2);
        cantidad3 = (TextView) v.findViewById(R.id.cantidad3);
        infoTotal = (TextView) v.findViewById(R.id.infoTotal);

        compartir = (Button) v.findViewById(R.id.compartir);
        cancelar = (Button) v.findViewById(R.id.cancelar);

        getResumen();

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviar();
            }
        });
        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_resumen_ganar_dialog, container, false);
    }

    public void getResumen(){
        SharedPreferences prefe = getContext().getSharedPreferences("resumen", Context.MODE_PRIVATE);
        cantidad1.setText(prefe.getString("nuevo",""));
        cantidad2.setText(prefe.getString("contesto",""));
        cantidad3.setText(prefe.getString("no_contesto",""));
        infoTotal.setText("Total: " +prefe.getString("total",""));
        mensaje = prefe.getString("mensaje","");

    }

    public void enviar(){
        SharedPreferences prefe = getContext().getSharedPreferences("resumen", Context.MODE_PRIVATE);
        String msjCompartir = mensaje + "\n\n" +
                                    "*RESUMEN* \n" +
                                    "\uD83D\uDCCC *Total:* " +prefe.getString("total","") +"\n"+
                                    "\uD83D\uDFE8 *Sin contactar:* " +prefe.getString("nuevo","") +"\n"+
                                    "\uD83D\uDFE9 *Si contesto:* " +prefe.getString("contesto","") +"\n"+
                                    "\uD83D\uDFE5 *No contesto:* " +prefe.getString("no_contesto","");

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
    }
}
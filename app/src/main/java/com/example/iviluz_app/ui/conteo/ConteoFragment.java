package com.example.iviluz_app.ui.conteo;

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
import android.widget.Button;

import com.example.iviluz_app.R;
import com.example.iviluz_app.databinding.ConteoFragmentBinding;
import com.example.iviluz_app.databinding.GanarFragmentBinding;
import com.example.iviluz_app.databinding.LiderFragmentBinding;
import com.example.iviluz_app.dialogos.ConteoDialogFragment;
import com.example.iviluz_app.dialogos.GanarDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import utilidades.LoadingDialog;

public class ConteoFragment extends Fragment {

    private ConteoFragmentBinding binding;
    private ConteoViewModel mViewModel;

    private Button addConteo;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private NetworkInfo networkInfo;
    private LoadingDialog loadingDialog;

    public static ConteoFragment newInstance() {
        return new ConteoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ConteoFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        addConteo = (Button) view.findViewById(R.id.addConteo);

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        loadingDialog = new LoadingDialog(getActivity());

        addConteo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConteoDialogFragment conteoDialogFragment = new ConteoDialogFragment();
                conteoDialogFragment.show(getActivity().getSupportFragmentManager(), "ConteoDialogFragment");
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ConteoViewModel.class);
        // TODO: Use the ViewModel
    }

}
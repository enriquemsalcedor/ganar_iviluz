package com.example.iviluz_app.ui.lider;

import static android.view.View.*;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.iviluz_app.R;
import com.example.iviluz_app.databinding.LiderFragmentBinding;
import com.example.iviluz_app.dialogos.LiderDialogFragment;

public class LiderFragment extends Fragment {

    private LiderFragmentBinding binding;
    private LiderViewModel mViewModel;
    private Button addLider;

    public static LiderFragment newInstance() {
        return new LiderFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = LiderFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        addLider = (Button) root.findViewById(R.id.addLider);
        Button aux = (Button) root.findViewById(R.id.aux);

        addLider.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Abriendooooo.....", Toast.LENGTH_SHORT).show();
            }
        });

        aux.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Abriendooooo.....!!!", Toast.LENGTH_SHORT).show();
            }
        });

        /*addLider.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Abriendooooo", Toast.LENGTH_SHORT).show();
                //LiderDialogFragment liderDialogFragment = new LiderDialogFragment();
                //liderDialogFragment.show(getActivity().getSupportFragmentManager(), "LiderDialogFragment");
            }
        });*/

        return root;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LiderViewModel.class);
        // TODO: Use the ViewModel
    }

}
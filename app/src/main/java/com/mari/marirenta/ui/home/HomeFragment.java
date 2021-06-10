package com.mari.marirenta.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.mari.marirenta.AddUser;
import com.mari.marirenta.R;
import com.mari.marirenta.databinding.FragmentHomeBinding;
import ViewModels.UserViewModel;

public class HomeFragment extends Fragment implements View.OnClickListener {

private HomeViewModel homeViewModel;
private UserViewModel _user;

public View onCreateView(@NonNull LayoutInflater inflater,
                         ViewGroup container, Bundle savedInstanceState) {
    homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel.class);
    View root = inflater.inflate(R.layout.fragment_home, container, false);
    _user = new UserViewModel(this.getActivity(),root);
    // final TextView textView = root.findViewById(R.id.text_home);
    FloatingActionButton fab = root.findViewById(R.id.fab);
    fab.setOnClickListener(this);
       /* homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
    setHasOptionsMenu(true);
    return root;
}

@Override
public void onClick(View v) {
    startActivity(new Intent(this.requireContext(), AddUser.class));
}
@Override
public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
    inflater.inflate(R.menu.main, menu);
    super.onCreateOptionsMenu(menu,inflater);
    _user.onCreateOptionsMenu(menu);
}
}
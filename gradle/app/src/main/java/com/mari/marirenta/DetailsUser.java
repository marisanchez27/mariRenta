package com.mari.marirenta;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.mari.marirenta.databinding.DetailsUserBinding;

import ViewModels.UserViewModel;

public class DetailsUser extends AppCompatActivity {
private UserViewModel user;
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // setContentView(R.layout.details_user);
    DetailsUserBinding binding = DataBindingUtil.setContentView(this,R.layout.details_user);
    user = new UserViewModel(this,binding);
    binding.setDetails(user);
    Window window = this.getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    window.setStatusBarColor(this.getResources().getColor(R.color.add, null));
}
@Override
public void onBackPressed(){
    startActivity(new Intent(this, MainActivity.class)
                          .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    finish();
}
}


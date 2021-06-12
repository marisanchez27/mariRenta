package com.mari.marirenta;

import android.content.Intent;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import com.mari.marirenta.databinding.VerifyPasswordBinding;

import ViewModels.LoginViewModels;

public class VerifyPassword extends AppCompatActivity {

@Override
protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    // setContentView(R.layout.verify_password);

    VerifyPasswordBinding _bindingPassword = DataBindingUtil.setContentView(this,R.layout.verify_password);
    _bindingPassword.setPasswordModel(new LoginViewModels(this,null,_bindingPassword));
    Window window = this.getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    window.setStatusBarColor(this.getResources().getColor(R.color.use, null));
}
@Override
public void onBackPressed(){
    super.onBackPressed();
    Intent intent= new Intent(this, VerifyEmail.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
}
}


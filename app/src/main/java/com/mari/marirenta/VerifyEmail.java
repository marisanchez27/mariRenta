package com.mari.marirenta;

import android.content.Intent;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mari.marirenta.databinding.VerifyEmailBinding;

import Library.MemoryData;
import ViewModels.LoginViewModels;


public class VerifyEmail extends AppCompatActivity {
private MemoryData memoryData;
private static final int RC_SIGN_IN = 9001;

private FirebaseAuth mAuth;
private GoogleSignInClient mGoogleSignInClient;
private LoginViewModels _login;
private VerifyEmailBinding _bindingEmail;
@Override
protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    memoryData = MemoryData.getInstance(this);
    if (memoryData.getData("user").equals("")){
        //setContentView(R.layout.verify_email);
        _bindingEmail = DataBindingUtil.setContentView(this,R.layout.verify_email);
        _login = new LoginViewModels(this,_bindingEmail,null);
        _bindingEmail.setEmailModel(_login);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.add, null));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("297609012126-qgr87ef20g7e8fum7d4g3hn3optluqh4.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        _bindingEmail.googleSignInButton.setOnClickListener(v -> {
            signIn();
        });
    }else{
        startActivity(new Intent(this, MainActivity.class)
                              .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

}
private void signIn(){
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
}
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data){
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SIGN_IN){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);

            firebaseAuthWithGoogle(account);
        }catch (ApiException e){
            String data2 =e.getMessage();
        }
    }
}
private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
    AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
    mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task ->{
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (! _login.RegisterUser(user.getEmail())){
                        mGoogleSignInClient.signOut();
                        mGoogleSignInClient.revokeAccess();
                    }
                }else{
                    Snackbar.make(_bindingEmail.googleSignInButton, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(this, task ->{
        String data2 =task.getMessage();
    });
}
private void access(){
    startActivity(new Intent(this, MainActivity.class)
                          .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent
                                                                              .FLAG_ACTIVITY_NEW_TASK));
}
}


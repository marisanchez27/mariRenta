package com.mari.marirenta.ui.close;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.mari.marirenta.R;
import com.mari.marirenta.VerifyEmail;


import Library.MemoryData;

public class CloseFragment extends Fragment {
private MemoryData memoryData;
private GoogleSignInClient mGoogleSignInClient;
public View onCreateView(@NonNull LayoutInflater inflater,
                         ViewGroup container, Bundle savedInstanceState){
    FirebaseAuth.getInstance().signOut();

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                      .requestIdToken(getString(R.string.default_web_client_id))
                                      .requestEmail()
                                      .build();
    mGoogleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso);
    mGoogleSignInClient.signOut();
    mGoogleSignInClient.revokeAccess();

    memoryData = MemoryData.getInstance(this.requireContext());
    memoryData.saveData("user","");
    startActivity(new Intent(this.requireContext(), VerifyEmail.class)
                          .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent
                                                                              .FLAG_ACTIVITY_NEW_TASK));
    return null;
}
}


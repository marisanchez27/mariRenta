package ViewModels;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mari.marirenta.MainActivity;
import com.mari.marirenta.R;
import com.mari.marirenta.VerifyPassword;
import com.mari.marirenta.databinding.VerifyEmailBinding;
import com.mari.marirenta.databinding.VerifyPasswordBinding;

import java.util.HashMap;
import java.util.Map;


import Library.MemoryData;
import Library.Multimedia;
import Library.Networks;
import Library.Validate;
import Models.BindableString;
import Models.Collections;
import Models.Pojo.User;
import interfaces.IonClick;

public class LoginViewModels extends ViewModel implements IonClick {
private Activity _activity;
public static String emailData = null;
private static VerifyEmailBinding _bindingEmail;
private static VerifyPasswordBinding _bindingPassword;
public BindableString emailUI = new BindableString();
public BindableString passwordUI = new BindableString();
private FirebaseAuth mAuth;
private MemoryData memoryData;
private FirebaseFirestore _db;
private DocumentReference _documentRef;
private Gson gson = new Gson();
private Multimedia _multimedia;
private StorageReference _storageRef;
private FirebaseStorage _storage;

public LoginViewModels(
        Activity activity,
        VerifyEmailBinding bindingEmail,
        VerifyPasswordBinding bindingPassword) {
    _activity = activity;
    _bindingEmail = bindingEmail;
    _bindingPassword = bindingPassword;
    if (emailData != null){
        emailUI.setValue(emailData);
    }
    mAuth = FirebaseAuth.getInstance();
    _multimedia = new Multimedia(_activity);
    _storage = FirebaseStorage.getInstance();
    _storageRef = _storage.getReference();
    _db = FirebaseFirestore.getInstance();
    memoryData = MemoryData.getInstance(_activity);
}

@Override
public void onClick(View view) {
    switch (view.getId()){
        case R.id.email_sign_in_button:
            VerifyEmail();
            break;
        case R.id.password_sign_in_button:
            login();
            break;
    }
    //Toast.makeText(_activity,emailUI.getValue(), Toast.LENGTH_SHORT).show();
}
private void VerifyEmail(){
    boolean cancel = true;
    _bindingEmail.emailEditText.setError(null);
    if (TextUtils.isEmpty(emailUI.getValue())){
        _bindingEmail.emailEditText.setError(
                _activity.getString(R.string.error_field_required));
        _bindingEmail.emailEditText.requestFocus();
        cancel = false;
    }else if (!Validate.isEmail(emailUI.getValue())){
        _bindingEmail.emailEditText.setError(
                _activity.getString(R.string.error_invalid_email));
        _bindingEmail.emailEditText.requestFocus();
        cancel = false;
    }
    if (cancel){
        emailData = emailUI.getValue();
        _activity.startActivity(new Intent(_activity, VerifyPassword.class));
    }
}
private void login(){
    boolean cancel = true;
    _bindingPassword.passwordEditText.setError(null);
    if (TextUtils.isEmpty(passwordUI.getValue())){
        _bindingPassword.passwordEditText.setError(
                _activity.getString(R.string.error_field_required));
        cancel = false;
    }else if (!isPasswordValid(passwordUI.getValue())){
        _bindingPassword.passwordEditText.setError(
                _activity.getString(R.string.error_invalid_password));
        cancel = false;
    }
    if (cancel){
        if (new Networks(_activity).verificaNetworks()){
            mAuth.signInWithEmailAndPassword(emailData,passwordUI.getValue())
                    .addOnCompleteListener(_activity,(task)->{
                        if (task.isSuccessful()){
                            memoryData = MemoryData.getInstance(_activity);
                            DocumentReference docRef = _db.collection(Collections.User.USERS).document(emailData);
                            docRef.get().addOnCompleteListener(task1 ->{
                                if (task1.isSuccessful()){
                                    DocumentSnapshot document = task1.getResult();
                                    if (document.exists()){
                                        String lastname = document.getData()
                                                                  .get(Collections.User.LASTNAME).toString();
                                        String email = document.getData()
                                                               .get(Collections.User.EMAIL).toString();
                                        String name = document.getData().
                                                                                get(Collections.User.NAME).toString();
                                        String role = document.getData()
                                                              .get(Collections.User.ROLE).toString();
                                        String active = document.getData()
                                                                .get(Collections.User.ACTIVE).toString();
                                        memoryData.saveData("user", gson.toJson(new User(
                                                lastname,
                                                name,
                                                email,
                                                role,
                                                null,
                                                active
                                        )));

                                        _activity.startActivity(new Intent(_activity, MainActivity.class)
                                                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent
                                                                                                                            .FLAG_ACTIVITY_NEW_TASK));
                                    }
                                }
                            });

                        }else{
                            Snackbar.make(_bindingPassword.passwordEditText,
                                    R.string.invalid_credentials, Snackbar.LENGTH_LONG).show();
                        }

                    }).addOnFailureListener(_activity, task ->{
                String data2 =task.getMessage();
            });
        }else{
            Snackbar.make(_bindingPassword.passwordEditText,
                    R.string.networks, Snackbar.LENGTH_LONG).show();
        }
    }
}
private boolean isPasswordValid(String password){
    return password.length() >= 6;
}
private boolean value = false;

public boolean RegisterUser(String email){
    if (new Networks(_activity).verificaNetworks()){
        _db.collection(Collections.User.USERS).document(email).addSnapshotListener((snapshot, e) ->{
            if ( snapshot != null && !snapshot.exists()){
                StorageReference imagesRef = _storageRef.
                                                                child(Collections.User.USERS + "/"
                                                                              + email);
                byte[] data = _multimedia.ImgaByte(R.mipmap.user);
                UploadTask uploadTask = imagesRef.putBytes(data);
                uploadTask.addOnFailureListener((exception) -> {
                    value = false;
                }).addOnSuccessListener((taskSnapshot) ->{
                    _documentRef = _db.collection(Collections.User.USERS)
                                           .document(email);
                    Map<String, Object> user = new HashMap<>();
                    user.put(Collections.User.LASTNAME, email);
                    user.put(Collections.User.EMAIL, email);
                    user.put(Collections.User.NAME, email);
                    user.put(Collections.User.ROLE, "User");
                    user.put(Collections.User.ACTIVE, "true");
                    _documentRef.set(user).addOnCompleteListener((task2) ->{
                        if (task2.isSuccessful()){
                            memoryData.saveData("user", gson.toJson(new User(
                                    email,
                                    email,
                                    email,
                                    "User",
                                    null,
                                    "true"
                            )));
                            _activity.startActivity(new Intent(_activity, MainActivity.class)
                                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent
                                                                                                                .FLAG_ACTIVITY_NEW_TASK));
                        }else{
                            value = false;
                        }
                    });
                });
            }else {
                if ( snapshot != null && snapshot.exists()){
                    String lastname = snapshot.getData()
                                              .get(Collections.User.LASTNAME).toString();
                    String name = snapshot.getData().
                                                            get(Collections.User.NAME).toString();
                    String role = snapshot.getData()
                                          .get(Collections.User.ROLE).toString();
                    String active = snapshot.getData()
                                            .get(Collections.User.ACTIVE).toString();
                    memoryData.saveData("user", gson.toJson(new User(
                            lastname,
                            name,
                            email,
                            role,
                            null,
                            active
                    )));
                    _activity.startActivity(new Intent(_activity, MainActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent
                                                                                                        .FLAG_ACTIVITY_NEW_TASK));
                }

            }
        });
    }
    return value;
}
}



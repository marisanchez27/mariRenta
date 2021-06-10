package ViewModels;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.mari.marirenta.R;
import com.mari.marirenta.databinding.ActivityMainBinding;

import java.lang.reflect.Type;

import Library.MemoryData;
import Library.Networks;
import Models.Collections;
import Models.Pojo.User;

public class MainViewModels {
private Activity _activity;
private View _headerView;
private MemoryData _memoryData;
private NavigationView _navigationView;
private FirebaseFirestore _db;
private FirebaseStorage _storage;
private StorageReference _storageRef;
private ImageView _imageViewUser;
private TextView _textViewName, _textViewLastName;
private Type typeItem = new TypeToken<User>() {
}.getType();
private Gson gson = new Gson();
private static User data;

public MainViewModels(Activity activity, ActivityMainBinding binding) {
    _activity = activity;
    _navigationView = binding.navView;
    _headerView = _navigationView.getHeaderView(0);
    _memoryData = MemoryData.getInstance(activity);
    _db = FirebaseFirestore.getInstance();
    _storage = FirebaseStorage.getInstance();
    _storageRef = _storage.getReference();
    _textViewName = _headerView.findViewById(R.id.textViewName);
    _textViewLastName = _headerView.findViewById(R.id.textViewLastName);
    _imageViewUser = _headerView.findViewById(R.id.imageViewUser);
    GetUser();
}

private void GetUser() {
    final long ONE_MEGABYTE = 1024 * 1024;
    if (new Networks(_activity).verificaNetworks()) {
        data = gson.fromJson(_memoryData.getData("user"), typeItem);
        _textViewName.setText(data.getName());
        _textViewLastName.setText(data.getLastName());
        _storageRef.child(Collections.User.USERS+"/"+data.getEmail())
                .getBytes(ONE_MEGABYTE).addOnSuccessListener((bytes)->{
            Bitmap _selectedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            _imageViewUser.setImageBitmap(_selectedImage);
            _imageViewUser.setScaleType(ImageView.ScaleType.CENTER_CROP);
        });
    }else{
        Snackbar.make(_navigationView, R.string.networks, Snackbar.LENGTH_LONG).show();
    }
}
}

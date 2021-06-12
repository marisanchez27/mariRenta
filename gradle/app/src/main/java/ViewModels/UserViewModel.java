package ViewModels;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mari.marirenta.AddUser;
import com.mari.marirenta.DetailsUser;
import com.mari.marirenta.R;
import com.mari.marirenta.databinding.AddUserBinding;
import com.mari.marirenta.databinding.DetailsUserBinding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import Library.MemoryData;
import Library.Multimedia;
import Library.Networks;
import Library.Permissions;
import Library.Validate;
import Models.BindableString;
import Models.Collections;
import Models.Item;
import Models.Pojo.User;
import ViewModels.Adapter.UserAdapter;
import interfaces.IonClick;

import static android.app.Activity.RESULT_OK;

public class UserViewModel extends ViewModel implements IonClick, UserAdapter.AdapterListener, SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
private Activity _activity;
private AddUserBinding _binding;
private Permissions _permissions;
private Multimedia _multimedia;
private static final int RESQUEST_CODE_CROP_IMAGE = 1;
public static final int REQUEST_CODE_TAKE_PHOTO = 0 ;
private static final String TEMP_PHOYO_FILE = "temporary_img.png";
private MemoryData memoryData;
private FirebaseAuth mAuth;
private FirebaseFirestore _db;
private DocumentReference _documentRef;
private FirebaseStorage _storage;
private StorageReference _storageRef;

public BindableString nameUI = new BindableString();
public BindableString lastnameUI = new BindableString();
public BindableString emailUI = new BindableString();
public BindableString passwordUI = new BindableString();
public Item item = new Item();
private List<User> userList = new ArrayList<>();
private UserAdapter _userAdapter;
private View _root;
private RecyclerView _recycler;
private RecyclerView.LayoutManager _lManager;
private ProgressBar _progressBarUsers;
private SwipeRefreshLayout _swipeRefresh;
private Type typeItem = new TypeToken<User>() {
}.getType();
private static User data;
private DetailsUserBinding _bindingDetails;
private Gson gson = new Gson();

public UserViewModel(Activity activity,View root){
    _activity = activity;
    _root = root;
    _recycler = root.findViewById(R.id.recyclerViewUsers);
    _progressBarUsers = root.findViewById(R.id.progressBarUsers);
    _swipeRefresh = root.findViewById(R.id.swipe_refresh);
    _recycler.setHasFixedSize(true);
    _lManager = new LinearLayoutManager(activity);
    _recycler.setLayoutManager(_lManager);
    _progressBarUsers.setVisibility(ProgressBar.VISIBLE);
    _swipeRefresh.setOnRefreshListener(this);
    StartFirebase();
    CloudFirestore();
    memoryData = MemoryData.getInstance(activity);
    data = gson.fromJson(memoryData.getData("user"), typeItem);
}

public UserViewModel(Activity activity, AddUserBinding binding){
    _activity = activity;
    _binding = binding;
    _binding.progressBar.setVisibility(ProgressBar.INVISIBLE);
    StartFirebase();
    if (_dataUser != null){
        SetUser();
    }
}
public UserViewModel(Activity activity, DetailsUserBinding binding){
    _activity = activity;
    _bindingDetails = binding;
    StartFirebase();
    GetUser();
}
private void StartFirebase(){
    mAuth = FirebaseAuth.getInstance();
    _storage = FirebaseStorage.getInstance();
    _storageRef = _storage.getReference();
    memoryData = MemoryData.getInstance(_activity);
    _permissions = new Permissions(_activity);
    _multimedia = new Multimedia(_activity);
}
@Override
public void onClick(View view) {
    switch (view.getId()){
        case R.id.buttonCamera:
            if (_permissions.CAMERA() && _permissions.STORAGE()){
                _multimedia.dispatchTakePictureIntent();
            }
            break;
        case R.id.buttonGallery:
            if (_permissions.STORAGE()){
                _multimedia.cropCapturedImage(1);
            }
            break;
        case R.id.buttonAddUser:
            AddUser();
            break;
        case R.id.fab_Edit:
            _activity.startActivity(new Intent(_activity, AddUser.class));
            break;
        case R.id.cancel_button:
            CancelUser();
            break;
    }
}
public void onActivityResult(int requestCode, int resultCode, Intent data){
    if (resultCode == RESULT_OK){
        switch (requestCode){
            case REQUEST_CODE_TAKE_PHOTO:
                _multimedia.cropCapturedImage(0);
                break;
            case RESQUEST_CODE_CROP_IMAGE:
                //Este seria el bitmap de nuestra imagen cortada.
                Bitmap imagenCortada = (Bitmap) data.getExtras().get("data");
                if ( imagenCortada == null){
                    String filePath = Environment.getExternalStorageDirectory()+"/"+TEMP_PHOYO_FILE;
                    imagenCortada  = BitmapFactory.decodeFile(filePath);
                }
                _binding.imageViewUser.setImageBitmap(imagenCortada);
                _binding.imageViewUser.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
        }
    }
}
private void AddUser(){
    if (TextUtils.isEmpty(nameUI.getValue())) {
        _binding.nameEditText.setError(_activity.getString(R.string.error_field_required));
        _binding.nameEditText.requestFocus();
    }else {
        if (TextUtils.isEmpty(lastnameUI.getValue())) {
            _binding.lastnameEditText.setError(_activity.getString(R.string.error_field_required));
            _binding.lastnameEditText.requestFocus();
        }else {
            if (TextUtils.isEmpty(emailUI.getValue())) {
                _binding.emailEditText.setError(_activity.getString(R.string.error_field_required));
                _binding.emailEditText.requestFocus();

            }else {
                if (!Validate.isEmail(emailUI.getValue())) {
                    _binding.emailEditText.setError(_activity.getString(R.string.error_invalid_email));
                    _binding.emailEditText.requestFocus();
                }else {
                    if (_dataUser == null) {
                        if (TextUtils.isEmpty(passwordUI.getValue())) {
                            _binding.passwordEditText.setError(_activity.getString(R.string.error_field_required));
                            _binding.passwordEditText.requestFocus();
                        } else {
                            if (!isPasswordValid(passwordUI.getValue())) {
                                _binding.passwordEditText.setError(_activity.getString(R.string.error_invalid_password));
                                _binding.passwordEditText.requestFocus();
                            } else {
                                if (new Networks(_activity).verificaNetworks()) {
                                    insertUser();
                                } else {
                                    Snackbar.make(_binding.passwordEditText, R.string.networks, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    }else{
                        if (new Networks(_activity).verificaNetworks()) {
                            editUser();
                        } else {
                            Snackbar.make(_binding.passwordEditText, R.string.networks, Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }
}
private boolean isPasswordValid(String password) {
    return password.length() >= 6;
}
private void insertUser(){
    _binding.progressBar.setVisibility(ProgressBar.VISIBLE);
    mAuth.createUserWithEmailAndPassword(emailUI.getValue(), passwordUI.getValue())
            .addOnCompleteListener(_activity, (task) ->{
                if (task.isSuccessful()){
                    StorageReference imagesRef = _storageRef.
                                                                    child(Collections.User.USERS + "/"
                                                                                  + emailUI.getValue());
                    byte[] data = _multimedia.ImgaeByte(_binding.imageViewUser);
                    UploadTask uploadTask = imagesRef.putBytes(data);
                    uploadTask.addOnFailureListener((exception) ->{

                    }).addOnSuccessListener((taskSnapshot) ->{
                        //String image = taskSnapshot.getMetadata().getPath();

                        String role = _activity.getResources()
                                              .getStringArray(R.array.item_roles)[item
                                                                                          .getSelectedItemPosition()];
                        _db = FirebaseFirestore.getInstance();
                        _documentRef = _db.collection(Collections.User.USERS)
                                               .document(emailUI.getValue());
                        Map<String, Object> user = new HashMap<>();
                        user.put(Collections.User.LASTNAME, lastnameUI.getValue());
                        user.put(Collections.User.EMAIL, emailUI.getValue());
                        user.put(Collections.User.NAME, nameUI.getValue());
                        user.put(Collections.User.ROLE, role);
                        user.put(Collections.User.ACTIVE, "true");
                        // user.put(Collections.User.IMAGE, image);
                        _documentRef.set(user).addOnCompleteListener((task2) ->{
                            if (task2.isSuccessful()){
                                _activity.finish();
                            }
                        });
                    });
                }else {
                    _binding.progressBar.setVisibility(ProgressBar.INVISIBLE);
                    Snackbar.make(_binding.passwordEditText, R.string.fail_register, Snackbar.LENGTH_LONG).show();
                }
            });
}
private void CloudFirestore(){
    if (new Networks(_activity).verificaNetworks()) {
        final long ONE_MEGABYTE = 1024 * 1024;
        _db = FirebaseFirestore.getInstance();
        _db.collection(Collections.User.USERS).addSnapshotListener((snapshots, e) -> {
            userList = new ArrayList<>();
            if (snapshots != null){
                for (QueryDocumentSnapshot document : snapshots) {
                    String lastname = document.getData().get(Collections.User.LASTNAME).toString();
                    String email = document.getData().get(Collections.User.EMAIL).toString();
                    String name = document.getData().get(Collections.User.NAME).toString();
                    String role = document.getData().get(Collections.User.ROLE).toString();
                    String active = document.getData().get(Collections.User.ACTIVE).toString();
                    //String image = document.getData().get(Collections.User.IMAGE).toString();
                    _storageRef.child(Collections.User.USERS + "/" + email)
                            .getBytes(ONE_MEGABYTE).addOnSuccessListener((bytes) -> {
                        userList.add(new User(lastname, name, email, role, bytes,active));
                        initRecyclerView(userList);
                    });

                }
            }

        });
    }else{
        _progressBarUsers.setVisibility(ProgressBar.INVISIBLE);
        _swipeRefresh.setRefreshing(false);
        Snackbar.make(_swipeRefresh, R.string.networks, Snackbar.LENGTH_LONG).show();
    }
}
private void initRecyclerView(List<User> list){
    _userAdapter = new UserAdapter(list, this);
    _recycler.setAdapter(_userAdapter);
    _progressBarUsers.setVisibility(ProgressBar.INVISIBLE);
    _swipeRefresh.setRefreshing(false);
}
public static User _dataUser;
@Override
public void onUserClicked(User user) {
    _dataUser = user;
    _activity.startActivity(new Intent(_activity, DetailsUser.class));
}

@Override
public void onUserDelete(User user) {
    if (data.getRole().equals("Admin")){
        if (!user.getEmail().equals(data.getEmail())){
            boolean active = Boolean.valueOf(user.getActive());
            int r = active ? R.string.disable : R.string.enable;
            AlertDialog.Builder builder = new AlertDialog.Builder(_activity,R.style.MyDialogTheme);
            builder.setTitle(r)
                    .setIcon(R.mipmap.ic_logo)
                    .setMessage(user.getEmail())
                    .setPositiveButton(r, (dialog, id) ->{
                        boolean value = active ? false : true;
                        disableUser(user,value);
                    });
            builder.show();
        }
    }
}

@Override
public void onRefresh() {
    CloudFirestore();
}
public void onCreateOptionsMenu(Menu menu){
    final MenuItem searchItem = menu.findItem(R.id.action_search);
    final SearchView searchView = (SearchView) searchItem.getActionView();
    searchView.setQueryHint(_activity.getText(R.string.action_search));
    searchView.setOnQueryTextListener(this);
}

@Override
public boolean onQueryTextSubmit(String query) {
    return false;
}

@Override
public boolean onQueryTextChange(String newText) {
    List<User> list =  userList.stream().filter(u -> u.getName().startsWith(newText)
                                                             || u.getLastName().startsWith(newText)).collect(Collectors.toList());
    initRecyclerView(list);
    return false;
}
private void GetUser(){
    byte[]bytes = _dataUser.getImage();
    Bitmap _selectedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    _bindingDetails.imageViewUser1.setImageBitmap(_selectedImage);
    _bindingDetails.imageViewUser1.setScaleType(ImageView.ScaleType.CENTER_CROP);
    _bindingDetails.imageViewUser2.setImageBitmap(_selectedImage);
    _bindingDetails.imageViewUser2.setScaleType(ImageView.ScaleType.CENTER_CROP);
    _bindingDetails.textName.setText(_dataUser.getName());
    _bindingDetails.textLastName.setText(_dataUser.getLastName());
    _bindingDetails.textEmail.setText(_dataUser.getEmail());
    _bindingDetails.textRole.setText(_dataUser.getRole());
}
private void SetUser(){
    byte[]bytes = _dataUser.getImage();
    Bitmap _selectedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    _binding.imageViewUser.setImageBitmap(_selectedImage);
    _binding.imageViewUser.setScaleType(ImageView.ScaleType.CENTER_CROP);
    nameUI.setValue(_dataUser.getName());
    lastnameUI.setValue(_dataUser.getLastName());
    emailUI.setValue(_dataUser.getEmail());
    if (_dataUser.getRole().equals("Admin")){
        item.setSelectedItemPosition(1);
    }else{
        item.setSelectedItemPosition(0);
    }
    _binding.passwordTextInput.setVisibility(View.GONE);
    _binding.emailTextInput.setVisibility(View.GONE);
}
private void editUser(){
    _binding.progressBar.setVisibility(ProgressBar.VISIBLE);
    String role = _activity.getResources()
                          .getStringArray(R.array.item_roles)[item
                                                                      .getSelectedItemPosition()];
    _db = FirebaseFirestore.getInstance();
    _documentRef = _db.collection(Collections.User.USERS ).document(_dataUser.getEmail());
    Map<String, Object> user = new HashMap<>();
    user.put(Collections.User.LASTNAME, lastnameUI.getValue());
    user.put(Collections.User.EMAIL, emailUI.getValue());
    user.put(Collections.User.NAME, nameUI.getValue());
    user.put(Collections.User.ROLE, role);
    user.put(Collections.User.ACTIVE, _dataUser.getActive());
    _documentRef.set(user).addOnCompleteListener((task2)->{
        if (task2.isSuccessful()){
            StorageReference imagesRef = _storageRef.
                                                            child(Collections.User.USERS +"/"
                                                                          +_dataUser.getEmail());
            byte[] data = _multimedia.ImgaeByte(_binding.imageViewUser);
            UploadTask uploadTask = imagesRef.putBytes(data);
            uploadTask.addOnFailureListener((exception)->{

            }).addOnSuccessListener((taskSnapshot)->{
                GetDocumentUser();
            });
        }else{
            _binding.progressBar.setVisibility(ProgressBar.INVISIBLE);
            Snackbar.make(_binding.passwordEditText, R.string.fail_register, Snackbar.LENGTH_LONG).show();
        }
    });
}
private void GetDocumentUser(){
    if (new Networks(_activity).verificaNetworks()){
        _db.collection(Collections.User.USERS)
                .document(_dataUser.getEmail()).addSnapshotListener((snapshot, e) ->{
            if ( snapshot != null && snapshot.exists()){
                String lastname = snapshot.getData().get(Collections.User.LASTNAME).toString();
                String email = snapshot.getData().get(Collections.User.EMAIL).toString();
                String name = snapshot.getData().get(Collections.User.NAME).toString();
                String role = snapshot.getData().get(Collections.User.ROLE).toString();
                String active = snapshot.getData().get(Collections.User.ACTIVE).toString();
                final long ONE_MEGABYTE = 1024 * 1024;
                _storageRef.child(Collections.User.USERS + "/" + _dataUser.getEmail())
                        .getBytes(ONE_MEGABYTE).addOnSuccessListener((bytes) ->{
                    _dataUser=  new User(lastname, name, email, role, bytes,active);
                    _activity.startActivity(new Intent(_activity, DetailsUser.class));
                    _activity.finish();
                });
            }
        });
    }else {
        Snackbar.make(_binding.passwordEditText, R.string.networks, Snackbar.LENGTH_LONG).show();
    }
}
private void disableUser(User user, boolean active){
    if (new Networks(_activity).verificaNetworks()){
        _db = FirebaseFirestore.getInstance();
        _documentRef = _db.collection(Collections.User.USERS ).document(user.getEmail());
        Map<String, Object> userData = new HashMap<>();
        userData.put(Collections.User.LASTNAME, user.getLastName());
        userData.put(Collections.User.EMAIL, user.getEmail());
        userData.put(Collections.User.NAME, user.getName());
        userData.put(Collections.User.ROLE, user.getRole());
        userData.put(Collections.User.ACTIVE, active);
        _documentRef.set(userData).addOnCompleteListener((task2)->{

        });
    }else{
        Snackbar.make(_progressBarUsers, R.string.networks, Snackbar.LENGTH_LONG).show();
    }
}
private void CancelUser(){
    _binding.nameEditText.setText("");
    _binding.lastnameEditText.setText("");
    _binding.emailEditText.setText("");
    _binding.imageViewUser.setImageResource(R.mipmap.user);
    item.setSelectedItemPosition(0);
    _dataUser = null;
    _binding.passwordTextInput.setVisibility(View.VISIBLE);
    _binding.emailTextInput.setVisibility(View.VISIBLE);
}
}


package ViewModels.Adapter;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mari.marirenta.R;
import com.mari.marirenta.databinding.ItemUserBinding;

import java.util.List;

import Models.Pojo.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder>{
private List<User> _userList;
private LayoutInflater _layoutInflater;
private AdapterListener _listener;

public UserAdapter(List<User> userList,AdapterListener listener){
    _userList = userList;
    _listener = listener;
}
public class MyViewHolder extends RecyclerView.ViewHolder{
    private final ItemUserBinding _binding;
    public MyViewHolder(final ItemUserBinding binding){
        super(binding.getRoot());
        _binding = binding;
    }
}
@NonNull
@Override
public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    if (_layoutInflater == null){
        _layoutInflater = LayoutInflater.from(parent.getContext());
    }
    ItemUserBinding binding =
            DataBindingUtil.inflate(_layoutInflater, R.layout.item_user, parent, false);
    return new MyViewHolder(binding);
}
@Override
public void onBindViewHolder(@NonNull MyViewHolder holder, int position){
    User user = _userList.get(position);
    holder._binding.setUser(user);
    byte[]bytes = user.getImage();
    Bitmap _selectedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    holder._binding.thumbnail.setImageBitmap(_selectedImage);
    holder._binding.cardViewUser.setOnClickListener((v)-> {

        if (_listener != null) {
            _listener.onUserClicked(_userList.get(position));
        }

    });
    FloatingActionButton fab = holder._binding.floatingActionButton;
    fab.setOnClickListener((e)->{
        _listener.onUserDelete(user);
    });
    if (!user.getActive().equals("")){
        boolean active = Boolean.valueOf(user.getActive());
        int r = active ? R.color.use : R.color.add;
        fab.setImageTintList(ColorStateList.valueOf(
                fab.getContext().getResources().getColor(r, null)));
    }
}
@Override
public int getItemCount() {
    return _userList.size();
}
public interface AdapterListener{
    void onUserClicked(User user);
    void onUserDelete(User user);
}
}


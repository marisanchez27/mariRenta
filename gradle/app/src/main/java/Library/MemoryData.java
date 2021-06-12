package Library;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class MemoryData extends AppCompatActivity {
private static MemoryData memoryData;
private SharedPreferences sharedPreferences;

public MemoryData(Context context){
    sharedPreferences = context.getSharedPreferences("kema",Context.MODE_PRIVATE);
}
public static MemoryData getInstance(Context context){
    if (memoryData == null){
        memoryData = new MemoryData(context);
    }
    return memoryData;
}
public void saveData(String key,String value){
    SharedPreferences.Editor  prefesEditor = sharedPreferences.edit();
    prefesEditor.putString(key,value);
    prefesEditor.commit();
}
public String getData(String key){
    if (sharedPreferences != null){
        return sharedPreferences.getString(key,"");
    }
    return "";
}
}
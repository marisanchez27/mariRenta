package Models;



import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.mari.marirenta.BR;

import java.util.Objects;

public class BindableString extends BaseObservable {
private String value;
/*BaseObservable que puede ampliar. La clase de datos es responsable de notificar cuándo cambian las propiedades. Esto se hace asignando una anotación @Bindable al captador y notificándolo en el definidor. Este oyente se invoca en cada actualización y actualiza las vistas correspondientes*/
@Bindable
public String getValue(){
    return value != null ? value : "";
}
public void setValue(String value){
    if(!Objects.equals(this.value,value)){
        this.value = value;
        notifyPropertyChanged(BR.value);
    }
}
}
package Models;



import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.mari.marirenta.BR;


public class Item extends BaseObservable {
private int selectedItemPosition;
/*La anotación enlazable debe aplicarse a cualquier método de acceso getter de una Observableclase. Bindable generará un campo en la clase BR para identificar el campo que ha cambiado.*/
@Bindable
public int getSelectedItemPosition(){
    return selectedItemPosition;
}
public void setSelectedItemPosition(int selectedItemPosition){
    this.selectedItemPosition = selectedItemPosition;
    notifyPropertyChanged(BR.selectedItemPosition);
}
}

package utils;

import java.util.function.Consumer;

public class MutRef<E> {
    private E value;

    public MutRef() {
        this.value = null;
    }

    public MutRef(E value) {
        this.value = value;
    }

    public E getValue() {
        return value;
    }

    public void ifHasValue(Consumer<E> then){
        if(hasValue()){
            then.accept(getValue());
        }
    }

    public boolean hasValue(){
        return value != null;
    }

    public void setValue(E value) {
        this.value = value;
    }
}

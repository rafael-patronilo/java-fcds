package utils;

public class Callback {

    public Callback(){
        action = null;
    }
    private Runnable action;
    public void setAction(Runnable action){
        this.action = action;
    }

    public void call(){
        if(action!=null) action.run();
    }
}

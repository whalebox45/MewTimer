package tku2c.mewtimer;

/**
 * Created by whale on 2017/5/25.
 */

public class Tone{
    String name;
    int resID;
    public Tone(int resID,String name){this.name = name; this.resID = resID;}
    public String getName() {return name;}
    public int getResID() {return resID;}
}

package net.ictcampus.sutern.nfcreader;

import android.support.v7.app.AppCompatActivity;

public abstract class parentClass extends AppCompatActivity {

    public static int color;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }


}

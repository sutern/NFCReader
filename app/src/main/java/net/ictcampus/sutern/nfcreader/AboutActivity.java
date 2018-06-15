package net.ictcampus.sutern.nfcreader;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

/**
 * @author glausla
 * @author sutern
 */

public class AboutActivity extends parentClass {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getColor());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}

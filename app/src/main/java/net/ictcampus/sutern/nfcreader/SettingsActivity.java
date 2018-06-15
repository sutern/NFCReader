package net.ictcampus.sutern.nfcreader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

/**
 * @author glausla
 * @author sutern
 */

public class SettingsActivity extends parentClass {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getColor());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button applyButton = (Button) findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                themeChange();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        this.finish();
    }

    public void themeChange() {
        RadioButton sb = (RadioButton) findViewById(R.id.strawberry);
        RadioButton bb = (RadioButton) findViewById(R.id.blueberry);
        RadioButton kw = (RadioButton) findViewById(R.id.kiwi);
        RadioButton hn = (RadioButton) findViewById(R.id.honey);
        RadioButton ct = (RadioButton) findViewById(R.id.clementine);

        if (bb.isChecked()) {
            setColor(R.style.BlueTheme);
        } else if (kw.isChecked()) {
            setColor(R.style.GreenTheme);
        } else if (hn.isChecked()) {
            setColor(R.style.YellowTheme);
        } else if (ct.isChecked()) {
            setColor(R.style.OrangeTheme);
        } else {
            setColor(R.style.AppTheme);
        }

        setContentView(R.layout.activity_settings);
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        this.finish();
    }
}
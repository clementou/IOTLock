package com.clement.iotlock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.clement.iotlock.ssh.CloseTask;
import com.clement.iotlock.ssh.OpenTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch lockSwitch = findViewById(R.id.lockSwitch);
        lockSwitch.setClickable(false);



        lockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) new CloseTask().execute();
                else new OpenTask().execute();
            }
        });
    }
}

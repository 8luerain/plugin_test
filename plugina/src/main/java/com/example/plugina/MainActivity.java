package com.example.plugina;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.baselib.utils.Utils;

public class MainActivity extends AppCompatActivity {
    private TextView txvPlugin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        txvPlugin = findViewById(R.id.plugin_txv);
//        txvPlugin.setText(com.example.baselib.R.string.formlib);
        txvPlugin.setText(Utils.getUtilsString());
    }
}

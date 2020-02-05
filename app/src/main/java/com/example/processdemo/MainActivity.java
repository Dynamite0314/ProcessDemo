package com.example.processdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.annotations.MyAnnotation;
import com.example.apt_library.BindViewTools;

public class MainActivity extends AppCompatActivity {

    @MyAnnotation(id = R.id.tv_name)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindViewTools.bind(this);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("dongp", "hahaha");
            }
        });
    }
}

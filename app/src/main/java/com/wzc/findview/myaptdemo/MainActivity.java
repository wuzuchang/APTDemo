package com.wzc.findview.myaptdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wzc.findview.annotation.BindView;
import com.wzc.findview.api.BindViewHelper;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvTest)
    TextView mTvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getLocalClassName();
        BindViewHelper.bind(this);
        mTvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "wwwww", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
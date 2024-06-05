package com.inhatc.final_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgGoWhymile, imgExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        imgGoWhymile = (ImageView)findViewById(R.id.imgGoWhymile);
        imgExit = (ImageView)findViewById(R.id.imgExit);

        imgGoWhymile.setOnClickListener(this);
        imgExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == imgGoWhymile) {
            startActivity(new Intent(MenuActivity.this, WhymileActivity.class));

        } else if (v == imgExit) {
            // 앱 종료
            finishAffinity(); // 루트 액티비티 종료
            System.runFinalization(); // 작업중인 쓰레드가 다 종료시 종료
            System.exit(0); // 현재 액티비티 종료

        }
    }
}
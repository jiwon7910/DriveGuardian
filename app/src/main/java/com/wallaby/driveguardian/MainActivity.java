package com.wallaby.driveguardian;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //로그아웃 버튼
        Button btn_logout = findViewById(R.id.btn_logout);
        Button btn_start = findViewById(R.id.btn_start);
        btn_logout.setOnClickListener(view -> {

            FirebaseAuth.getInstance().signOut();
            finish();

            //Intent를 새로 만든 후 LoginActivity로 화면전환
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btn_start.setOnClickListener(view -> {


            //Intent를 새로 만든 후 LoginActivity로 화면전환
            Intent intent = new Intent(MainActivity.this, DetectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

    }
}

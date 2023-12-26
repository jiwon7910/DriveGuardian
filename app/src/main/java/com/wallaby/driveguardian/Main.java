package com.wallaby.driveguardian;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 시작하기 버튼
        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(v -> {
            navigateToDetectionActivity();
        });
    }

    private void navigateToDetectionActivity() {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        finish(); // 현재 액티비티를 종료하고 DetectionActivity로 이동
    }
}

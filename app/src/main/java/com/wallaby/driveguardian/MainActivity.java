package com.wallaby.driveguardian;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // onCreate() 메서드에서 FirebaseAuth 인스턴스를 초기화
        auth = FirebaseAuth.getInstance();

        // 회원가입 기능
        Button joinBtn = findViewById(R.id.joinBtn);
        // 사용자가 이메일과 비밀번호를 입력하고 회원가입 버튼을 누를 경우
        joinBtn.setOnClickListener(v -> {
            EditText emailEditText = findViewById(R.id.email);
            EditText passwordEditText = findViewById(R.id.password);

            auth.createUserWithEmailAndPassword(emailEditText.getText().toString(),
                            passwordEditText.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "성공", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "실패", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 로그인 기능
        Button loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(v -> {
            EditText emailEditText = findViewById(R.id.email);
            EditText passwordEditText = findViewById(R.id.password);

            auth.signInWithEmailAndPassword(emailEditText.getText().toString(),
                            passwordEditText.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "로그인 성공", Toast.LENGTH_LONG).show();
                            navigateToMain();
                        } else {
                            Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 비회원 로그인 기능
        Button noLoginBtn = findViewById(R.id.noLoginBtn);
        noLoginBtn.setOnClickListener(v -> {
            auth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            Log.d("MainActivity", user != null ? user.getUid() : null);
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 로그아웃 기능
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "로그아웃", Toast.LENGTH_LONG).show();
        });
    }
    private void navigateToMain() {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        finish(); // 현재 액티비티를 종료하고 Main으로 이동
    }

}

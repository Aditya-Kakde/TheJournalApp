package com.example.thejournalapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    Button loginBTN, createAccountBtn;
    private EditText emailET, passET;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        createAccountBtn = findViewById(R.id.createAccount);

        createAccountBtn.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(i);
        });

        loginBTN = findViewById(R.id.login);
        emailET = findViewById(R.id.email);
        passET = findViewById(R.id.password);

        firebaseAuth = FirebaseAuth.getInstance();

        loginBTN.setOnClickListener(v -> {
            loginEmailPassUser(emailET.getText().toString().trim(),
                    passET.getText().toString().trim());
        });

    }

    private void loginEmailPassUser(String email, String pwd) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)) {
            firebaseAuth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Intent i = new Intent(MainActivity.this, JournalListActivity.class);
                            startActivity(i);
                        } else {
                            firebaseAuth.createUserWithEmailAndPassword(email, pwd)
                                    .addOnCompleteListener(this, createTask -> {
                                        if (createTask.isSuccessful()) {
                                            FirebaseUser user = firebaseAuth.getCurrentUser();
                                            Intent i = new Intent(MainActivity.this, JournalListActivity.class);
                                            startActivity(i);
                                        } else {
                                            Log.e("FirebaseAuth", "Error: " + createTask.getException().getMessage());
                                        }
                                    });
                        }
                    });
        } else {
            Log.e("FirebaseAuth", "Email and Password cannot be empty");
        }
    }
}
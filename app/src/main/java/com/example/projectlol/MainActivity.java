package com.example.projectlol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private ProgressBar progressBar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth.getInstance().signOut();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            checkUserExistsInFirestore(currentUser.getUid());
        } else {
            initViews();
            setupViewModel();
        }
    }

    private void initViews() {
        coordinatorLayout = findViewById(R.id.coordinator);
        editEmail = findViewById(R.id.field_emailToLogin);
        editPassword = findViewById(R.id.field_passwordToLogin);
        progressBar = findViewById(R.id.progressBar);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonToRegisterActivity = findViewById(R.id.buttonToRegisterActivity);

        // Слушатели кнопок
        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonToRegisterActivity.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterView.class));
        });
    }

    private void setupViewModel() {
        MainActivityViewModel vm = new ViewModelProvider(this).get(MainActivityViewModel.class);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
                showErrorSnackbar("Превышено время ожидания");
            }
        }, 10000);

        vm.getLoginSuccess().observe(this, success -> {
            progressBar.setVisibility(View.GONE);
            if (success) {
                startActivity(new Intent(this, profile.class));
                finish();
            }
        });

        vm.getErrorMessage().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            if (error != null && !error.isEmpty()) {
                showErrorSnackbar(error);
            }
        });
    }

    private void attemptLogin() {
        progressBar.setVisibility(View.VISIBLE);
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        MainActivityViewModel vm = new ViewModelProvider(this).get(MainActivityViewModel.class);
        vm.userLogin(email, password);
    }

    private void showErrorSnackbar(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction("Повторить", v -> attemptLogin())
                .setBackgroundTint(ContextCompat.getColor(this, R.color.error_red))
                .setTextColor(ContextCompat.getColor(this, android.R.color.white))
                .show();
    }

    private void checkUserExistsInFirestore(String userId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        startActivity(new Intent(this, profile.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Требуется повторная регистрация", Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        initViews();
                        setupViewModel();
                    }
                });
    }
}
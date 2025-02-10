package com.example.projectlol;

import static com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

public class RegisterView extends AppCompatActivity {

    TextView textViewError;
    EditText editEmail;
    EditText editPassword;
    Button buttonRegister;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_view);
        editEmail = findViewById(R.id.field_emailToRegister);
        editPassword = findViewById(R.id.field_passwordToRegister);
        buttonRegister = findViewById(R.id.buttonRegister);

        RegisterViewModel vm = new ViewModelProvider(this).get(RegisterViewModel.class);


        vm.navigateToMain().observe(this, shouldNavigate ->
        {
            if (shouldNavigate != null && shouldNavigate)
            {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                vm.resetNavigation();
            }
        });


        vm.getErrorMessage().observe(this, errorText ->
        {
            if (errorText != null && !errorText.isEmpty())
            {
                Snackbar.make(findViewById(android.R.id.content), errorText, Snackbar.LENGTH_LONG)
                        .setAnimationMode(ANIMATION_MODE_SLIDE).show();
            }
        });


        buttonRegister.setOnClickListener(v -> {

            String email = editEmail.getText().toString();
            String password = editPassword.getText().toString();
            vm.registerUser(email,password);
        });
    }

}
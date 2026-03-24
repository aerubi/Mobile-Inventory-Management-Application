package com.example.InventoryManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Handles login and account creation using InventoryRepository.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnCreateAccount;

    private InventoryRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = new InventoryRepository(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // LOGIN
        btnLogin.setOnClickListener(v -> handleLogin());

        // CREATE ACCOUNT
        btnCreateAccount.setOnClickListener(v -> handleCreateAccount());
    }

    private void handleLogin() {

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            etUsername.setError("Username required");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        if (repository.loginUser(username, password)) {

            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

                startActivity(new Intent(this, InventoryActivity.class));

            } else {

                startActivity(new Intent(this, SmsPermissionsActivity.class));
            }

        } else {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCreateAccount() {

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            etUsername.setError("Username required");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            etPassword.setError("Password must be at least 4 characters");
            etPassword.requestFocus();
            return;
        }

        boolean created = repository.createUser(username, password);

        if (created) {
            Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
        }
    }
}
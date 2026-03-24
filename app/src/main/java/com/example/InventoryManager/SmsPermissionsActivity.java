package com.example.InventoryManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Explains SMS permission and allows user to enable or skip.
 */
public class SmsPermissionsActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 200;

    private Button btnEnableSMS, btnSkipSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permissions);

        btnEnableSMS = findViewById(R.id.btnEnableSMS);
        btnSkipSMS = findViewById(R.id.btnSkipSMS);

        // Enable SMS button
        btnEnableSMS.setOnClickListener(v -> requestSMSPermission());

        // Skip button
        btnSkipSMS.setOnClickListener(v -> {
            Toast.makeText(this,
                    "SMS Disabled. App will continue normally.",
                    Toast.LENGTH_SHORT).show();

            goToInventory();
        });
    }

    private void requestSMSPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        } else {
            goToInventory();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this,
                        "SMS Permission Granted!",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this,
                        "SMS Permission Denied. App still works.",
                        Toast.LENGTH_SHORT).show();
            }

            goToInventory();
        }
    }

    private void goToInventory() {
        startActivity(new Intent(this, InventoryActivity.class));
        finish();
    }
}
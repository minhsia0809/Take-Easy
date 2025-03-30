package com.example.takeiteasy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class HomeAdmin extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_home_admin);

        ImageButton btnReturnKey = findViewById(R.id.returnButton);
        btnReturnKey.setOnClickListener(v -> finish());

        Button btnEditLocation = findViewById(R.id.editlocationButton);
        btnEditLocation.setOnClickListener(v ->
        {
            Intent i = new Intent(HomeAdmin.this, EditLocation.class);
            startActivity(i);
        });

        Button btnManual2 = findViewById(R.id.manualButton2);
        btnManual2.setOnClickListener(v ->
        {
            Intent i = new Intent(HomeAdmin.this, Manual.class);
            startActivity(i);
        });

        Button btnUserManagement = findViewById(R.id.usermanagementButton);
        btnUserManagement.setOnClickListener(v ->
        {
            Intent i = new Intent(HomeAdmin.this, UserManagement.class);
            startActivity(i);
        });
    }
}
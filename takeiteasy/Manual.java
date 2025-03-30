package com.example.takeiteasy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

public class Manual extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(UidSession.readIsAdmin(this) ? R.layout.act_manual_admin : R.layout.act_manual_user);

        ImageButton BtnReturnKey = findViewById(R.id.returnButton);
        BtnReturnKey.setOnClickListener(v -> finish());
    }
}
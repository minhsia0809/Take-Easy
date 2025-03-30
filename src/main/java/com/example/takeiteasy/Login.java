package com.example.takeiteasy;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.ResultSet;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity
{
    public boolean login(String username, String password)
    {
        try
        {
            String sql = "SELECT user_id, is_admin FROM user WHERE account = BINARY ? AND password = ?;";
            ResultSet rs = MySQLConn.fetch(sql, username, password);
            if (rs.next())
            {
                UidSession.putUserID(this, rs.getInt("user_id"));
                UidSession.putIsAdmin(this, rs.getBoolean("is_admin"));
                return true;
            }
        }
        catch (Exception e)
        {
            Log.e(getString(R.string.tag), e.getMessage());
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        EditText LoginAccount = findViewById(R.id.login_account);
        EditText LoginPassword = findViewById(R.id.login_pwd);

        UidSession.clear(this);

        Button BtnSubmit = findViewById(R.id.submitButton);
        BtnSubmit.setOnClickListener(v ->
        {
            String account = LoginAccount.getText().toString().trim();
            String password = LoginPassword.getText().toString();

            if (login(account, password))
            {
                Toast.makeText(Login.this, "帳戶登入成功", Toast.LENGTH_SHORT).show();
                boolean isAdmin = UidSession.readIsAdmin(Login.this);
                Intent i = new Intent(Login.this, isAdmin ? HomeAdmin.class : HomeUser.class);
                startActivity(i);
            }
            else
            {
                Toast.makeText(Login.this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                LoginPassword.setText("");
            }
        });

        Button BtnReset = findViewById(R.id.resetButton);
        BtnReset.setOnClickListener(v ->
        {
            LoginAccount.setText("");
            LoginPassword.setText("");
        });
    }
}
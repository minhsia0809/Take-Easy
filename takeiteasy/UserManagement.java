package com.example.takeiteasy;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;

public class UserManagement extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_user_management);

        ImageButton btnReturnKey = findViewById(R.id.returnButton);
        btnReturnKey.setOnClickListener(v -> finish());

        TableLayout tableLayout = findViewById(R.id.tablelayout);
        TableLayout tableAdmin = findViewById(R.id.table_admin);

        try
        {
            ResultSet rs = MySQLConn.fetch("SELECT * FROM user ORDER BY user_id;");
            while (rs.next())
            {
                int userID = rs.getInt("user_id");
                String account = rs.getString("account");
                String name = URLDecoder.decode(rs.getString("name"));
                boolean isAdmin = rs.getBoolean("is_admin");

                TableRow tableRow1 = new TableRow(this);
                tableRow1.setGravity(Gravity.CENTER);

                for (int i = 0; i < 3; i ++)
                {
                    TextView textView1 = new TextView(this);
                    textView1.setTextSize(16);
                    textView1.setPadding(30, 0, 30, 0);
                    textView1.setTextColor(Color.BLACK);
                    textView1.setGravity(Gravity.CENTER);
                    tableRow1.addView(textView1);
                }

                ((TextView) tableRow1.getChildAt(0)).setText(String.valueOf(userID));
                ((TextView) tableRow1.getChildAt(1)).setText(account);
                ((TextView) tableRow1.getChildAt(2)).setText(name);

                Button editBtn = new Button(this);
                editBtn.setText("詳細");
                editBtn.setTextSize(16);
                tableRow1.addView(editBtn);

                if (isAdmin)
                {
                    editBtn.setOnClickListener(v -> Toast.makeText(UserManagement.this, "管理者不得修改", Toast.LENGTH_SHORT).show());
                    tableAdmin.addView(tableRow1);
                }
                else
                {
                    editBtn.setOnClickListener(v ->
                    {
                        Intent i = new Intent(UserManagement.this, Profile.class);
                        i.putExtra("user_id", userID);
                        startActivity(i);
                    });
                    tableLayout.addView(tableRow1);
                }
            }
        }
        catch (Exception e)
        {
            Log.e(getString(R.string.tag), e.getMessage());
        }

        Button insertBtn = findViewById(R.id.insertButton);
        insertBtn.setOnClickListener(v -> showAddDialog());
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        this.recreate();
    }

    private void showAddDialog()
    {
        Dialog dialog = new Dialog(this);
        View viewDialog = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        dialog.setContentView(viewDialog);
        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                                     ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        EditText editAccount = viewDialog.findViewById(R.id.account);
        EditText editPassword = viewDialog.findViewById(R.id.password);
        EditText editPassword2 = viewDialog.findViewById(R.id.password_confirm);
        EditText editName = viewDialog.findViewById(R.id.name);

        Button confirmBtn = viewDialog.findViewById(R.id.editButton);
        confirmBtn.setText("確認");
        confirmBtn.setOnClickListener(v ->
        {
            String account = editAccount.getText().toString().trim();
            String password = editPassword.getText().toString();
            String password2 = editPassword2.getText().toString();
            String name = editName.getText().toString().trim();

            if (account.isEmpty() || password.isEmpty() || name.isEmpty())
                Toast.makeText(UserManagement.this, "資料不得為空", Toast.LENGTH_SHORT).show();
            else if (!password.equals(password2))
                Toast.makeText(UserManagement.this, "密碼不一致", Toast.LENGTH_SHORT).show();
            else
            {
                try
                {
                    MySQLConn.alternate("INSERT INTO user (account, password, name) VALUES (?, ?, ?);",
                                        account, password, URLEncoder.encode(name));
                    Toast.makeText(UserManagement.this, "寫入資料成功", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
                }
                catch (Exception e)
                {
                    Toast.makeText(UserManagement.this, "寫入資料失敗", Toast.LENGTH_SHORT).show();
                    Log.e(getString(R.string.tag), e.getMessage());
                }
                dialog.dismiss();
            }
        });

        Button cancelBtn = viewDialog.findViewById(R.id.removeButton);
        cancelBtn.setText("清除");
        cancelBtn.setOnClickListener(v ->
        {
            editAccount.setText("");
            editPassword.setText("");
            editPassword2.setText("");
            editName.setText("");
        });
    }
}

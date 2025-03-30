package com.example.takeiteasy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;

public class EditLocation extends AppCompatActivity
{
    private int pixelWidth;
    private int pixelHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_edit_location);

        ImageButton BtnReturnKey = findViewById(R.id.returnButton);
        BtnReturnKey.setOnClickListener(v -> finish());

        TableLayout tableLayout = findViewById(R.id.tablelayout);
        RelativeLayout relativeLayout = findViewById(R.id.relativelayout);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.env_lib_2);
        relativeLayout.addView(imageView);

        try
        {
            ResultSet rs = MySQLConn.fetch("SELECT * FROM location;");
            while (rs.next())
            {
                int locationID = rs.getInt("location_id");
                String site = URLDecoder.decode(rs.getString("site"));
                int cor_x = rs.getInt("x");
                int cor_y = rs.getInt("y");
                boolean available = rs.getBoolean("available");

                TableRow row = new TableRow(this);
                row.setGravity(Gravity.CENTER);

                for (int i = 0; i < 3; i++)
                {
                    TextView textView = new TextView(this);
                    textView.setTextSize(16);
                    textView.setPadding(30, 0, 30, 0);
                    if (available)
                        textView.setTextColor(Color.BLACK);
                    else
                    {
                        textView.setTextColor(Color.GRAY);
                        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    textView.setGravity(Gravity.CENTER);
                    row.addView(textView);
                }

                ((TextView) row.getChildAt(0)).setText(String.valueOf(locationID));
                ((TextView) row.getChildAt(1)).setText(site);
                ((TextView) row.getChildAt(2)).setText("(" + cor_x + ", " + cor_y + ")");

                Button editBtn = new Button(this);
                editBtn.setText("編輯");
                editBtn.setTextSize(16);
                editBtn.setOnClickListener(v -> showModifyDialog(locationID, site, cor_x, cor_y, available));
                row.addView(editBtn);
                tableLayout.addView(row);
            }
        }
        catch (Exception e)
        {
            Log.e(getString(R.string.tag), e.getMessage());
        }

        imageView.post(() ->
        {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inScaled = false;
            Bitmap bb = BitmapFactory.decodeResource(getResources(), R.drawable.env_lib_2, opt);
            EditLocation.this.pixelWidth = bb.getWidth();
            EditLocation.this.pixelHeight = bb.getHeight();
            int labelSize = 60;

            try
            {
                ResultSet rs = MySQLConn.fetch("SELECT * FROM location;");
                while (rs.next())
                {
                    int x = (int) ((double) rs.getInt("x") * imageView.getWidth() / EditLocation.this.pixelWidth);
                    int y = (int) ((double) rs.getInt("y") * imageView.getHeight() / EditLocation.this.pixelHeight);

                    TextView textLabel = new TextView(EditLocation.this);
                    textLabel.setText(URLDecoder.decode(rs.getString("site")));
                    textLabel.post(() ->
                    {
                        textLabel.setX((int) (x - textLabel.getWidth() / 2.0));
                        textLabel.setY((int) (y + textLabel.getHeight() / 2.0));
                    });
                    textLabel.setBackgroundColor(Color.parseColor("#CFFFFFFF"));
                    relativeLayout.addView(textLabel);

                    ImageView imageLabel = new ImageView(EditLocation.this);
                    imageLabel.setBackgroundResource(rs.getBoolean("available") ? R.drawable.circle_green : R.drawable.circle_red);
                    imageLabel.post(() ->
                    {
                        ViewGroup.LayoutParams param = imageLabel.getLayoutParams();
                        param.width = labelSize;
                        param.height = labelSize;
                        imageLabel.setLayoutParams(param);
                    });
                    imageLabel.setX(x - labelSize / 2);
                    imageLabel.setY(y - labelSize / 2);
                    relativeLayout.addView(imageLabel);
                }
            }
            catch (Exception e)
            {
                Log.e(getString(R.string.tag), e.getMessage());
            }
        });

        Button insertBtn = findViewById(R.id.insertButton);
        insertBtn.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog()
    {
        Dialog dialog = new Dialog(this);
        View viewDialog = getLayoutInflater().inflate(R.layout.dialog_edit_location, null);
        dialog.setContentView(viewDialog);
        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                                     ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        TextView title = viewDialog.findViewById(R.id.textTitle);
        title.setText("新增");

        EditText location = viewDialog.findViewById(R.id.location_2);
        EditText coordinateX = viewDialog.findViewById(R.id.coordinate_x_2);
        EditText coordinateY = viewDialog.findViewById(R.id.coordinate_y_2);
        CheckBox availableCheck = viewDialog.findViewById(R.id.available_2);

        Button confirmBtn = viewDialog.findViewById(R.id.editButton);
        confirmBtn.setText("確認");
        confirmBtn.setOnClickListener(v ->
        {
            try
            {
                if (location.getText().toString().trim().isEmpty())
                    throw new Exception("");

                String sql = "INSERT INTO location (site, x, y, available) " +
                             "SELECT ?, ?, ?, ? WHERE (? BETWEEN 0 AND ?) AND (? BETWEEN 0 AND ?);";
                int updated = MySQLConn.alternate(sql,
                                                  URLEncoder.encode(location.getText().toString()),
                                                  coordinateX.getText(),
                                                  coordinateY.getText(),
                                                  availableCheck.isChecked() ? 1 : 0,
                                                  coordinateX.getText(),
                                                  EditLocation.this.pixelWidth,
                                                  coordinateY.getText(),
                                                  EditLocation.this.pixelHeight);

                if (updated == 0)
                    throw new Exception("");

                Toast.makeText(EditLocation.this, "寫入資料成功", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
            catch (Exception e)
            {
                Toast.makeText(EditLocation.this, "寫入資料失敗", Toast.LENGTH_SHORT).show();
                Log.e(getString(R.string.tag), e.getMessage());
            }
            dialog.dismiss();
        });

        Button cancelBtn = viewDialog.findViewById(R.id.removeButton);
        cancelBtn.setText("清除");
        cancelBtn.setOnClickListener(v ->
        {
            location.setText("");
            coordinateX.setText("");
            coordinateY.setText("");
        });
    }

    private void showModifyDialog(int locationID, String site, int x, int y, boolean available)
    {
        Dialog dialog = new Dialog(this);
        View  viewDialog = getLayoutInflater().inflate(R.layout.dialog_edit_location, null);
        dialog.setContentView(viewDialog);
        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                                     ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        TextView title = viewDialog.findViewById(R.id.textTitle);
        title.setText("編輯");

        EditText location = viewDialog.findViewById(R.id.location_2);
        EditText coordinateX = viewDialog.findViewById(R.id.coordinate_x_2);
        EditText coordinateY = viewDialog.findViewById(R.id.coordinate_y_2);
        CheckBox availableCheck = viewDialog.findViewById(R.id.available_2);

        location.setText(site);
        coordinateX.setText(String.valueOf(x));
        coordinateY.setText(String.valueOf(y));
        availableCheck.setChecked(available);

        Button editBtn = viewDialog.findViewById(R.id.editButton);
        editBtn.setText("修改");
        editBtn.setOnClickListener(v ->
        {
            try
            {
                if (location.getText().toString().trim().isEmpty())
                    throw new Exception("");

                String sql = "UPDATE location SET site = ?, x = ?, y = ?, available = ? " +
                             "WHERE location_id = ? AND (? BETWEEN 0 AND ?) AND (? BETWEEN 0 AND ?);";
                int updated = MySQLConn.alternate(sql,
                                                  URLEncoder.encode(location.getText().toString()),
                                                  coordinateX.getText(),
                                                  coordinateY.getText(),
                                                  availableCheck.isChecked() ? 1 : 0,
                                                  locationID,
                                                  coordinateX.getText(),
                                                  EditLocation.this.pixelWidth,
                                                  coordinateY.getText(),
                                                  EditLocation.this.pixelHeight);

                if (updated == 0)
                    throw new Exception("");

                Toast.makeText(EditLocation.this, "修改資料成功", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
            catch (Exception e)
            {
                Toast.makeText(EditLocation.this, "修改資料失敗", Toast.LENGTH_SHORT).show();
                Log.e(getString(R.string.tag), e.getMessage());
            }
            dialog.dismiss();
        });

        Button removeBtn = viewDialog.findViewById(R.id.removeButton);
        removeBtn.setText("刪除");
        removeBtn.setOnClickListener(v ->
        {
            AlertDialog.Builder dialog2 = new AlertDialog.Builder(EditLocation.this);
            dialog2.setTitle("刪除地點");
            dialog2.setMessage("是否要刪除該地點");
            dialog2.setPositiveButton("確認", (dialogInterface, i) ->
            {
                try
                {
                    MySQLConn.alternate("DELETE FROM location WHERE location_id = ?;", locationID);
                    Toast.makeText(EditLocation.this, "刪除資料成功", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
                }
                catch (Exception e)
                {
                    Toast.makeText(EditLocation.this, "刪除資料失敗", Toast.LENGTH_SHORT).show();
                    Log.e(getString(R.string.tag), e.getMessage());
                }
                dialog.dismiss();
            });
            dialog2.setNegativeButton("取消", null);
            dialog2.create().show();
        });
    }
}
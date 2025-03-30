package com.example.takeiteasy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONStringer;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;

public class Profile extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_profile);

        ImageButton btnReturnKey = findViewById(R.id.returnButton);
        btnReturnKey.setOnClickListener(v -> finish());

        int userID = getIntent().getIntExtra("user_id", -1);

        try
        {
            ResultSet rs = MySQLConn.fetch("SELECT * FROM user WHERE user_id = ?;", userID);
            if (rs.next())
            {
                TextView textViewName = findViewById(R.id.textView_name);
                EditText textViewHeight = findViewById(R.id.textView_height);
                EditText textViewWeight = findViewById(R.id.textView_weight);
                textViewName.setText(URLDecoder.decode(rs.getString("name")));
                textViewHeight.setText(String.valueOf(rs.getFloat("height")));
                textViewWeight.setText(String.valueOf(rs.getFloat("weight")));

                Button editButtonHeight = findViewById(R.id.editButtonHeight);
                Button editButtonWeight = findViewById(R.id.editButtonWeight);
                Button deleteButton = findViewById(R.id.delete_account);

                if (!UidSession.readIsAdmin(this))
                {
                    textViewHeight.setEnabled(false);
                    textViewHeight.setBackgroundResource(R.color.transparent);
                    textViewWeight.setEnabled(false);
                    textViewWeight.setBackgroundResource(R.color.transparent);

                    editButtonHeight.setVisibility(View.GONE);
                    editButtonWeight.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                }
                else
                {
                    editButtonHeight.setOnClickListener(v ->
                    {
                        try
                        {
                            MySQLConn.alternate("UPDATE user SET height = ? WHERE user_id = ?;",
                                                textViewHeight.getText(), userID);
                            Toast.makeText(Profile.this, "修改資料成功", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(getIntent());
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(Profile.this, "修改資料失敗", Toast.LENGTH_SHORT).show();
                            Log.e(getString(R.string.tag), e.getMessage());
                        }
                    });

                    editButtonWeight.setOnClickListener(v ->
                    {
                        try
                        {
                            MySQLConn.alternate("UPDATE user SET weight = ? WHERE user_id = ?;",
                                                textViewWeight.getText(), userID);
                            Toast.makeText(Profile.this, "修改資料成功", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(getIntent());
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(Profile.this, "修改資料失敗", Toast.LENGTH_SHORT).show();
                            Log.e(getString(R.string.tag), e.getMessage());
                        }
                    });

                    deleteButton.setOnClickListener(v ->
                    {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(Profile.this);
                        dialog.setTitle("刪除帳戶");
                        dialog.setMessage("是否要刪除該帳戶");
                        dialog.setPositiveButton("確認", (dialogInterface, i) ->
                        {
                            try
                            {
                                MySQLConn.alternate("DELETE FROM user WHERE user_id = ?;", userID);
                                Toast.makeText(Profile.this, "刪除資料成功", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(Profile.this, "刪除資料失敗", Toast.LENGTH_SHORT).show();
                                Log.e(getString(R.string.tag), e.getMessage());
                            }
                        });
                        dialog.setNegativeButton("取消", null);
                        dialog.create().show();
                    });
                }
            }
        }
        catch (Exception e)
        {
            Log.e(getString(R.string.tag), e.getMessage());
        }

        ArrayList<String> date = new ArrayList<>();

        try
        {
            ResultSet rs = MySQLConn.fetch("SELECT DISTINCT day FROM schedule WHERE user_id = ? ORDER BY day;", userID);
            while (rs.next())
                date.add(rs.getDate("day").toString());
        }
        catch (Exception e)
        {
            Log.e(getString(R.string.tag), e.getMessage());
        }

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, date);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                LinearLayout textViewSchedule = findViewById(R.id.textView_schedule);
                textViewSchedule.removeAllViews();

                try
                {
                    TextView schedule;
                    String sql = "SELECT * FROM schedule s, location l " +
                                 "WHERE s.location_id = l.location_id AND day = ? AND s.user_id = ?;";
                    ResultSet rs = MySQLConn.fetch(sql, adapter.getItem(position), userID);
                    while (rs.next())
                    {
                        int scheduleID = rs.getInt("schedule_id");
                        int locationID = rs.getInt("location_id");
                        Date day = rs.getDate("day");
                        Time startTime = rs.getTime("start_time");
                        Time endTime = rs.getTime("end_time");
                        String name = URLDecoder.decode(rs.getString("name"));
                        String site = URLDecoder.decode(rs.getString("site"));
                        String notice = rs.getString("notice") == null ? "" : URLDecoder.decode(rs.getString("notice"));

                        LinearLayout weed = new LinearLayout(Profile.this);
                        weed.setOrientation(LinearLayout.VERTICAL);

                        schedule = new TextView(Profile.this);
                        schedule.setText(rs.getTime("start_time") + " ~ " + endTime);
                        schedule.setTextColor(Color.BLACK);
                        schedule.setTextSize(16);
                        weed.addView(schedule);

                        schedule = new TextView(Profile.this);
                        schedule.setText("[" + site + "] " + name);
                        schedule.setTextColor(Color.BLACK);
                        schedule.setTypeface(null, Typeface.BOLD);
                        schedule.setTextSize(18);
                        weed.addView(schedule);

                        schedule = new TextView(Profile.this);
                        schedule.setText(notice);
                        schedule.setTextSize(16);
                        schedule.setPadding(0, 0, 0, 20);
                        weed.addView(schedule);

                        if (UidSession.readIsAdmin(Profile.this))
                        {
                            weed.setOnClickListener(v ->
                            {
                                showModifyDialog(scheduleID, name, locationID, day, startTime, endTime, notice);
                            });
                        }
                        textViewSchedule.addView(weed);
                    }
                }
                catch (Exception e)
                {
                    Log.e(getString(R.string.tag), e.getMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
            }
        });

        Button insertBtn = findViewById(R.id.insertButton);
        if (UidSession.readIsAdmin(this))
            insertBtn.setOnClickListener(v -> showAddDialog(userID));
        else
            insertBtn.setVisibility(View.GONE);

    }

    private void showAddDialog(int userID)
    {
        Dialog dialog = new Dialog(this);
        View viewDialog = getLayoutInflater().inflate(R.layout.dialog_edit_schedule, null);
        dialog.setContentView(viewDialog);
        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                                     ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        TextView title = viewDialog.findViewById(R.id.textTitle);
        title.setText("新增");

        EditText location = viewDialog.findViewById(R.id.location_2);
        EditText coordinateX = viewDialog.findViewById(R.id.coordinate_x_2);
        Button datePicker = viewDialog.findViewById(R.id.date);
        Button startTimePicker = viewDialog.findViewById(R.id.start_time);
        Button endTimePicker = viewDialog.findViewById(R.id.end_time);

        ArrayList<String> sites = new ArrayList<>();
        ArrayList<Integer> locationIDs = new ArrayList<>();
        try
        {
            ResultSet rs = MySQLConn.fetch("SELECT location_id, site FROM location;");
            while (rs.next())
            {
                sites.add(String.format("(%d) %s", rs.getInt("location_id"), URLDecoder.decode(rs.getString("site"))));
                locationIDs.add(rs.getInt("location_id"));
            }
        }
        catch (Exception e)
        {
            Log.e(getString(R.string.tag), e.getMessage());
        }

        Spinner spinner2 = viewDialog.findViewById(R.id.coordinate_y_2);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sites);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter);

        Date day = new Date(System.currentTimeMillis());
        datePicker.setText(day.toString());
        datePicker.setOnClickListener(v ->
        {
            String[] dateInfo = datePicker.getText().toString().split("-");
            DatePickerDialog dpd = new DatePickerDialog(Profile.this, (datePicker1, i, i1, i2) ->
            {
                datePicker.setText(String.format("%02d-%02d-%02d", i, i1 + 1, i2));
            }, Integer.parseInt(dateInfo[0]), Integer.parseInt(dateInfo[1]) - 1, Integer.parseInt(dateInfo[2]));
            dpd.show();
        });

        startTimePicker.setText("00:00:00");
        startTimePicker.setOnClickListener(v ->
        {
            String[] timeInfo = startTimePicker.getText().toString().split(":");
            TimePickerDialog tpd = new TimePickerDialog(Profile.this, (timePicker, i, i1) ->
            {
                startTimePicker.setText(String.format("%02d:%02d:00", i, i1));
            }, Integer.parseInt(timeInfo[0]), Integer.parseInt(timeInfo[1]), true);
            tpd.show();
        });

        endTimePicker.setText("00:00:00");
        endTimePicker.setOnClickListener(v ->
        {
            String[] timeInfo = endTimePicker.getText().toString().split(":");
            TimePickerDialog tpd = new TimePickerDialog(Profile.this, (timePicker, i, i1) ->
            {
                endTimePicker.setText(String.format("%02d:%02d:00", i, i1));
            }, Integer.parseInt(timeInfo[0]), Integer.parseInt(timeInfo[1]), true);
            tpd.show();
        });

        Button editBtn = viewDialog.findViewById(R.id.editButton);
        editBtn.setText("確認");
        editBtn.setOnClickListener(v ->
        {
            try
            {
                if (location.getText().toString().trim().isEmpty())
                    throw new Exception("");

                String sql = "INSERT INTO schedule (name, user_id, location_id, day, start_time, end_time, notice) " +
                             "SELECT ?, ?, ?, ?, ?, ?, ? WHERE ? < ?;";
                int updated = MySQLConn.alternate(sql,
                                                  URLEncoder.encode(location.getText().toString()),
                                                  userID,
                                                  locationIDs.get(spinner2.getSelectedItemPosition()),
                                                  datePicker.getText(),
                                                  startTimePicker.getText(),
                                                  endTimePicker.getText(),
                                                  URLEncoder.encode(coordinateX.getText().toString()),
                                                  startTimePicker.getText(),
                                                  endTimePicker.getText());

                if (updated == 0)
                    throw new Exception("");

                Toast.makeText(Profile.this, "寫入資料成功", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
            catch (Exception e)
            {
                Toast.makeText(Profile.this, "寫入資料失敗", Toast.LENGTH_SHORT).show();
                Log.e(getString(R.string.tag), e.getMessage());
            }
            dialog.dismiss();
        });

        Button removeBtn = viewDialog.findViewById(R.id.removeButton);
        removeBtn.setText("清除");
        removeBtn.setOnClickListener(v ->
        {
            location.setText("");
            spinner2.setSelection(0);
            datePicker.setText(day.toString());
            startTimePicker.setText("00:00:00");
            endTimePicker.setText("00:00:00");
            coordinateX.setText("");
        });
    }

    private void showModifyDialog(int scheduleID, String name, int locationID, Date day, Time startTime, Time endTime, String notice)
    {
        Dialog dialog = new Dialog(this);
        View viewDialog = getLayoutInflater().inflate(R.layout.dialog_edit_schedule, null);
        dialog.setContentView(viewDialog);
        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                                     ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        TextView title = viewDialog.findViewById(R.id.textTitle);
        title.setText("編輯");

        EditText location = viewDialog.findViewById(R.id.location_2);
        EditText coordinateX = viewDialog.findViewById(R.id.coordinate_x_2);
        Button datePicker = viewDialog.findViewById(R.id.date);
        Button startTimePicker = viewDialog.findViewById(R.id.start_time);
        Button endTimePicker = viewDialog.findViewById(R.id.end_time);

        ArrayList<String> sites = new ArrayList<>();
        ArrayList<Integer> locationIDs = new ArrayList<>();
        try
        {
            ResultSet rs = MySQLConn.fetch("SELECT location_id, site FROM location;");
            while (rs.next())
            {
                sites.add(String.format("(%d) %s", rs.getInt("location_id"), URLDecoder.decode(rs.getString("site"))));
                locationIDs.add(rs.getInt("location_id"));
            }
        }
        catch (Exception e)
        {
            Log.e(getString(R.string.tag), e.getMessage());
        }

        Spinner spinner2 = viewDialog.findViewById(R.id.coordinate_y_2);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sites);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter);
        spinner2.setSelection(locationIDs.indexOf(locationID));

        location.setText(name);
        coordinateX.setText(notice);

        datePicker.setText(day.toString());
        datePicker.setOnClickListener(v ->
        {
            String[] dateInfo = datePicker.getText().toString().split("-");
            DatePickerDialog dpd = new DatePickerDialog(Profile.this, (datePicker1, i, i1, i2) ->
            {
                datePicker.setText(String.format("%02d-%02d-%02d", i, i1 + 1, i2));
            }, Integer.parseInt(dateInfo[0]), Integer.parseInt(dateInfo[1]) - 1, Integer.parseInt(dateInfo[2]));
            dpd.show();
        });

        startTimePicker.setText(startTime.toString());
        startTimePicker.setOnClickListener(v ->
        {
            String[] timeInfo = startTimePicker.getText().toString().split(":");
            TimePickerDialog tpd = new TimePickerDialog(Profile.this, (timePicker, i, i1) ->
            {
                startTimePicker.setText(String.format("%02d:%02d:00", i, i1));
            }, Integer.parseInt(timeInfo[0]), Integer.parseInt(timeInfo[1]), true);
            tpd.show();
        });

        endTimePicker.setText(endTime.toString());
        endTimePicker.setOnClickListener(v ->
        {
            String[] timeInfo = endTimePicker.getText().toString().split(":");
            TimePickerDialog tpd = new TimePickerDialog(Profile.this, (timePicker, i, i1) ->
            {
                endTimePicker.setText(String.format("%02d:%02d:00", i, i1));
            }, Integer.parseInt(timeInfo[0]), Integer.parseInt(timeInfo[1]), true);
            tpd.show();
        });

        Button editBtn = viewDialog.findViewById(R.id.editButton);
        editBtn.setText("修改");
        editBtn.setOnClickListener(v ->
        {
            try
            {
                if (location.getText().toString().trim().isEmpty())
                    throw new Exception("");

                String sql = "UPDATE schedule SET name = ?, location_id = ?, day = ?, start_time = ?, end_time = ?, notice = ? " +
                             "WHERE schedule_id = ? AND ? < ?;";
                int updated = MySQLConn.alternate(sql,
                                                  URLEncoder.encode(location.getText().toString()),
                                                  locationIDs.get(spinner2.getSelectedItemPosition()),
                                                  datePicker.getText(),
                                                  startTimePicker.getText(),
                                                  endTimePicker.getText(),
                                                  URLEncoder.encode(coordinateX.getText().toString()),
                                                  scheduleID,
                                                  startTimePicker.getText(),
                                                  endTimePicker.getText());

                if (updated == 0)
                    throw new Exception("");

                Toast.makeText(Profile.this, "修改資料成功", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
            catch (Exception e)
            {
                Toast.makeText(Profile.this, "修改資料失敗", Toast.LENGTH_SHORT).show();
                Log.e(getString(R.string.tag), e.getMessage());
            }
            dialog.dismiss();
        });

        Button removeBtn = viewDialog.findViewById(R.id.removeButton);
        removeBtn.setText("刪除");
        removeBtn.setOnClickListener(v ->
        {
            AlertDialog.Builder dialog2 = new AlertDialog.Builder(Profile.this);
            dialog2.setTitle("刪除行程");
            dialog2.setMessage("是否要刪除該行程");
            dialog2.setPositiveButton("確認", (dialogInterface, i) ->
            {
                try
                {
                    MySQLConn.alternate("DELETE FROM schedule WHERE schedule_id = ?;", scheduleID);
                    Toast.makeText(Profile.this, "刪除資料成功", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
                }
                catch (Exception e)
                {
                    Toast.makeText(Profile.this, "刪除資料失敗", Toast.LENGTH_SHORT).show();
                    Log.e(getString(R.string.tag), e.getMessage());
                }
                dialog.dismiss();
            });
            dialog2.setNegativeButton("取消", null);
            dialog2.create().show();
        });
    }
}
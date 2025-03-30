package com.example.takeiteasy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URLDecoder;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.text.SimpleDateFormat;

public class HomeUser extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_home_user);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ImageButton BtnReturnKey = findViewById(R.id.returnButton);
        BtnReturnKey.setOnClickListener(v -> finish());

        Button BtnCurrentPos = findViewById(R.id.currentposButton);
        BtnCurrentPos.setOnClickListener(v ->
        {
            Intent i = new Intent(HomeUser.this, CurrentPosition.class);
            startActivity(i);
        });

        Button BtnProfile = findViewById(R.id.profileButton);
        BtnProfile.setOnClickListener(v ->
        {
            Intent i = new Intent(HomeUser.this, Profile.class);
            i.putExtra("user_id", UidSession.readUserID(HomeUser.this));
            startActivity(i);
        });

        Button BtnManual = findViewById(R.id.manualButton);
        BtnManual.setOnClickListener(v ->
        {
            Intent i = new Intent(HomeUser.this, Manual.class);
            startActivity(i);
        });

        try
        {
            LinearLayout text_view = findViewById(R.id.textView_schedule);
            String sql = "SELECT * FROM schedule s, location l " +
                         "WHERE s.location_id = l.location_id AND s.user_id = ? "+
                         "AND TIMESTAMP(s.day, s.start_time) > CURRENT_TIMESTAMP() " +
                         "ORDER BY s.day, s.start_time ASC;";
            ResultSet rs = MySQLConn.fetch(sql, UidSession.readUserID(HomeUser.this));
            while (rs.next())
            {
                String name = URLDecoder.decode(rs.getString("name"));
                String site = URLDecoder.decode(rs.getString("site"));
                String notice = rs.getString("notice") == null ? "-" : URLDecoder.decode(rs.getString("notice"));
                Date day = rs.getDate("day");
                Time startTime = rs.getTime("start_time");
                Time endTime = rs.getTime("end_time");

                if (rs.getRow() <= 2)
                {
                    TextView schedule = new TextView(this);
                    schedule.setText(day + "\n" + startTime + " ~ " + endTime);
                    text_view.addView(schedule);
                    schedule.setTextColor(Color.BLACK);
                    schedule.setTextSize(16);

                    TextView schedule2 = new TextView(this);
                    schedule2.setText("[" + site + "] " + name);
                    schedule2.setTextColor(Color.BLACK);
                    schedule2.setTypeface(null, Typeface.BOLD);
                    schedule2.setTextSize(18);
                    schedule2.setPadding(0, 0, 0, 20);
                    text_view.addView(schedule2);
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                long t = dateFormat.parse(day + " " + startTime).getTime();

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent i = new Intent(this, ScheduleNotifier.class);
                i.putExtra("title", name);
                i.putExtra("msg", notice);
                PendingIntent pi = PendingIntent.getBroadcast(this, (int) t, i, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, t, pi);
            }
        }
        catch (Exception e)
        {
            Log.e(getString(R.string.tag), e.getMessage());
        }
    }
}
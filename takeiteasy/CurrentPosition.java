package com.example.takeiteasy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONStringer;

import java.net.URLDecoder;
import java.sql.ResultSet;

public class CurrentPosition extends AppCompatActivity
{
    private double xScalar, yScalar;
    private MQTTClient mqtt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_current_position);

        ImageButton BtnReturnKey = findViewById(R.id.returnButton);
        BtnReturnKey.setOnClickListener(v -> finish());

        this.mqtt = new MQTTClient(this.getApplicationContext());

        RelativeLayout relativeLayout = findViewById(R.id.relativelayout);
        ImageView ivCanvas = new AppCompatImageView(this)
        {
            @Override
            protected void onDraw(Canvas canvas)
            {
                super.onDraw(canvas);

                Paint paintPoint = new Paint();
                paintPoint.setColor(Color.RED);
                paintPoint.setStyle(Paint.Style.FILL);
                int x = (int) (mqtt.x * xScalar) - 20;
                int y = (int) (mqtt.y * yScalar) - 20;
                canvas.drawCircle(x, y, 20, paintPoint);
                invalidate(); // refresh
            }
        };

        ivCanvas.setImageResource(R.drawable.env_lib_2);
        relativeLayout.addView(ivCanvas);

        ivCanvas.post(() ->
        {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inScaled = false;
            Bitmap bb = BitmapFactory.decodeResource(getResources(), R.drawable.env_lib_2, opt);
            int labelSize = 60;

            CurrentPosition.this.xScalar = (double) ivCanvas.getWidth() / bb.getWidth();
            CurrentPosition.this.yScalar = (double) ivCanvas.getHeight() / bb.getHeight();

            try
            {
                ResultSet rs = MySQLConn.fetch("SELECT * FROM location WHERE available;");
                while (rs.next())
                {
                    String site = URLDecoder.decode(rs.getString("site"));
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");

                    int xDrawing = (int) (x * CurrentPosition.this.xScalar);
                    int yDrawing = (int) (y * CurrentPosition.this.yScalar);

                    TextView textLabel = new TextView(CurrentPosition.this);
                    textLabel.setText(site);
                    textLabel.post(() ->
                    {
                        textLabel.setX((int) (xDrawing - textLabel.getWidth() / 2.0));
                        textLabel.setY((int) (yDrawing + textLabel.getHeight() / 2.0));
                    });
                    textLabel.setBackgroundColor(Color.parseColor("#CFFFFFFF"));
                    relativeLayout.addView(textLabel);

                    Button imageLabel = new Button(CurrentPosition.this);
                    imageLabel.setBackgroundResource(R.drawable.circle_green);
                    imageLabel.setOnClickListener(v ->
                    {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(CurrentPosition.this);
                        dialog.setTitle("前往地點");
                        dialog.setMessage("即將前往以下地點：" + site);
                        dialog.setPositiveButton("確認", (dialogInterface, i) ->
                        {
                            if (CurrentPosition.this.mqtt.available)
                            {
                                try
                                {
                                    JSONStringer payload = new JSONStringer()
                                            .object()
                                            .key("x").value(x)
                                            .key("y").value(y)
                                            .key("shutdown").value(false)
                                            .endObject();
                                    CurrentPosition.this.mqtt.publishMessage(payload.toString());
                                    Toast.makeText(CurrentPosition.this, "確認成功，開始前往地點", Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e)
                                {
                                    Log.e(getString(R.string.tag), e.getMessage());
                                    Toast.makeText(CurrentPosition.this, "系統尚未連接至輪椅", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else if (mqtt.isSubscriberConnected())
                                Toast.makeText(CurrentPosition.this, "輪椅尚在移動中", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(CurrentPosition.this, "系統尚未連接至輪椅", Toast.LENGTH_SHORT).show();
                        });
                        dialog.setNegativeButton("取消", null);
                        dialog.create().show();
                    });

                    imageLabel.post(() ->
                    {
                        ViewGroup.LayoutParams param = imageLabel.getLayoutParams();
                        param.width = labelSize;
                        param.height = labelSize;
                        imageLabel.setLayoutParams(param);
                    });
                    imageLabel.setX(xDrawing - labelSize / 2);
                    imageLabel.setY(yDrawing - labelSize / 2);
                    relativeLayout.addView(imageLabel);
                }
            }
            catch (Exception e)
            {
                Log.e(getString(R.string.tag), e.getMessage());
            }
        });

        Button buttonPublish = findViewById(R.id.publish);
        buttonPublish.setOnClickListener(v ->
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(CurrentPosition.this);
            dialog.setTitle("緊急停止");
            dialog.setMessage("若需要立即停止車輛，請按確認");
            dialog.setPositiveButton("確認", (dialogInterface, i) ->
            {
                boolean available = CurrentPosition.this.mqtt.available;

                try
                {
                    JSONStringer payload = new JSONStringer()
                            .object()
                            .key("shutdown").value(true)
                            .endObject();
                    CurrentPosition.this.mqtt.publishMessage(payload.toString());
                    if (available)
                        Toast.makeText(CurrentPosition.this, "輪椅並未移動", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(CurrentPosition.this, "已中斷輪椅行程", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    Log.e(getString(R.string.tag), e.getMessage());
                    Toast.makeText(CurrentPosition.this, "系統錯誤", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.setNegativeButton("取消", null);
            dialog.create().show();
        });
    }

    @Override
    public void finish()
    {
        super.finish();

        try
        {
            this.mqtt.disconnect();
        }
        catch (Exception ignored) { }
    }
}
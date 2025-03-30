package com.example.takeiteasy;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class MQTTClient
{
    private final Context context;
    private final MqttConnectOptions options;

    private final String url = "tcp://192.168.43.59:1883";
    private final String subscriberTopic = "takeiteasy";
    private final String publisherTopic = "takeiteasyMQTT/1";

    private final String subscriberID;
    private final String publisherID;
    private MqttAndroidClient subscriber = null;
    private MqttAndroidClient publisher = null;

    private Thread connector;
    private boolean connecting;

    public int x = -1;
    public int y = -1;
    public boolean available = false;

    public MQTTClient(Context context)
    {
        this.context = context;
        this.options = new MqttConnectOptions();
        this.options.setConnectionTimeout(30);

        this.subscriberID = "sub_" + this.hashCode();
        this.publisherID = "pub_" + this.hashCode();

        this.connecting = true;
        this.connector = new Thread(() ->
        {
            boolean subscribing = false;

            while (MQTTClient.this.connecting)
            {
                try
                {
                    if (!this.isSubscriberConnected())
                        this.connectSubscriber();
                    else if (!subscribing)
                    {
                        subscribing = true;
                        this.subscribeMessage();
                    }

                    if (!this.isPublisherConnected())
                        this.connectPublisher();

                    Thread.sleep(2500);
                }
                catch (Exception e)
                {
                    Log.e(context.getString(R.string.tag), e.getMessage());
                }
            }
        });
        this.connector.start();
    }

    public void connectSubscriber() throws MqttException
    {
        this.subscriber = new MqttAndroidClient(this.context, this.url, this.subscriberID);

        IMqttToken token = this.subscriber.connect(this.options);
        token.setActionCallback(new IMqttActionListener()
        {
            @Override
            public void onSuccess(IMqttToken asyncActionToken)
            {
                Log.i(MQTTClient.this.context.getString(R.string.tag), "Subscriber connected: " + MQTTClient.this.subscriberID);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception)
            {
                Log.e(MQTTClient.this.context.getString(R.string.tag), exception.getMessage());
            }
        });
    }

    public void connectPublisher() throws MqttException
    {
        this.publisher = new MqttAndroidClient(this.context, this.url, this.publisherID);

        IMqttToken token = this.publisher.connect(this.options);
        token.setActionCallback(new IMqttActionListener()
        {
            @Override
            public void onSuccess(IMqttToken asyncActionToken)
            {
                Log.i(MQTTClient.this.context.getString(R.string.tag), "Publisher connected: " + MQTTClient.this.publisherID);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception)
            {
                Log.e(MQTTClient.this.context.getString(R.string.tag), exception.getMessage());
            }
        });
    }

    public boolean isSubscriberConnected()
    {
        return this.subscriber != null && this.subscriber.isConnected();
    }

    public boolean isPublisherConnected()
    {
        return this.publisher != null && this.publisher.isConnected();
    }

    public void subscribeMessage() throws MqttException
    {
        this.subscriber.subscribe(this.subscriberTopic, 1);
        this.subscriber.setCallback(new MqttCallback()
        {
            @Override
            public void messageArrived(String topic, MqttMessage message)
            {
                String[] coordinate = new String(message.getPayload()).split(" ");
                MQTTClient.this.x = (int) Float.parseFloat(coordinate[0]);
                MQTTClient.this.y = (int) Float.parseFloat(coordinate[1]);
                MQTTClient.this.available = Boolean.parseBoolean(coordinate[2]);
            }

            @Override
            public void connectionLost(Throwable cause)
            {
                MQTTClient.this.available = false;
                Log.w(MQTTClient.this.context.getString(R.string.tag), cause.getMessage());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token)
            {
            }
        });
    }

    public void publishMessage(String payload) throws MqttException
    {
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(0);

        this.publisher.publish(this.publisherTopic, message, null, new IMqttActionListener()
        {
            @Override
            public void onSuccess(IMqttToken asyncActionToken)
            {
                Log.i(MQTTClient.this.context.getString(R.string.tag), "Successfully published message: " + payload);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception)
            {
                Log.e(MQTTClient.this.context.getString(R.string.tag), "Failed to publish message");
            }
        });
    }

    public void disconnect() throws MqttException
    {
        try
        {
            this.connecting = false;
            this.connector.join();
        }
        catch (InterruptedException ignored) { }

        if (this.isSubscriberConnected())
        {
            subscriber.unsubscribe(this.subscriberTopic);
            subscriber.disconnect();
            subscriber.unregisterResources();
            subscriber = null;
        }

        if (this.isPublisherConnected())
        {
            publisher.unsubscribe(this.publisherTopic);
            publisher.disconnect();
            publisher.unregisterResources();
            publisher = null;
        }
    }
}

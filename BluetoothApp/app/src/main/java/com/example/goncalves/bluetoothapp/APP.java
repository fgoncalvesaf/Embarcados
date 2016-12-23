package com.example.goncalves.bluetoothapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

public class APP extends AppCompatActivity {

    String mac;
    TextView address;
    static TextView message;
    static TextView cuidado;
    static RelativeLayout layout;
    static String dist = "";
    static final int CAREFUL_LENGTH = 100;
    static final int DANGEROUS_LENGTH = 50;
    static final int VAI_BATER = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent intent = this.getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            //String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            mac = intent.getStringExtra(Intent.EXTRA_TEXT);
            address = (TextView) findViewById(R.id.distancia_text);
            address.setText(mac);
        }

        message = (TextView) findViewById(R.id.distancia);
        cuidado = (TextView) findViewById(R.id.cuidado);
        layout = (RelativeLayout) findViewById(R.id.layoutId);
    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            String dataString = new String(data);
            System.out.println(dataString + " TAMANHO DO ARRAY DE BYTE:" + data.length);
            float distancia = Float.parseFloat(dataString);
            if(distancia <= DANGEROUS_LENGTH){
                layout.setBackgroundColor(Color.RED);
                if (distancia <= VAI_BATER)
                    cuidado.setText("CUIDADO!");
            } else if (distancia < CAREFUL_LENGTH && distancia > DANGEROUS_LENGTH){
                layout.setBackgroundColor(Color.YELLOW);
                cuidado.setText("");
            } else {
                layout.setBackgroundColor(Color.GREEN);
                cuidado.setText("");
            }

            DecimalFormat formatar = new DecimalFormat("0.00");
            if(distancia > CAREFUL_LENGTH)
                message.setText(String.valueOf(formatar.format(distancia/100.00)) + " m");
            else
                message.setText(dataString + " cm");
        }
    };
}

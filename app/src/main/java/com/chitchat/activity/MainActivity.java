package com.chitchat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.chitchat.R;
import com.chitchat.encryption.RSA;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RSA rsa = new RSA();
        byte[] value;
        try {
            value = rsa.getPrivateKey(Base64.getDecoder().decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA9P8E0ZpymxNHUjLY5D3V3mBxHREBGFygVLgM+5FBVQfUXxO9uK6s1EtZCxMyf0RRxlCn7RgnLewQFpVFJL2OEMuH/bp6JRVbZHfTTH0Lshhf2b4z/mPx/3/RZz+7p0JTbdguRdNo/LisKS0zfIgjwbNIV7KQYHQjmdzdqAOuuyIWhwy7by3LkuwnkOcHY24tTmgE698ET3Clvkt4ZW/J4uoy3TTm+pcvHj9TNU+iNE3FbnxhbObW67Zt7IUoWdH+cRlKw3PPFrphbL5xDdmWGoaIqUsq/wQ9s2v7Ib7HGjgXaav9U9ulltqRz8LlFDQeK/cTdA0+Gbi9/jqIX6K5QQIDAQAB"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        Log.w("PrivateKEy",Base64.getEncoder().encodeToString(value));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        },4000);
    }
}
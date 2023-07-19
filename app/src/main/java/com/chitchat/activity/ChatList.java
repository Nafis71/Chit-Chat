package com.chitchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.chitchat.R;
import com.chitchat.database.messageModel;
import com.chitchat.database.pageAdapter;
import com.chitchat.encryption.AES;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.NoSuchPaddingException;

public class ChatList extends AppCompatActivity {
    ImageView profilePicture;
    String userId,photoUrl;
    ViewPager2 vPager;
    TabLayout tabs;
    Bitmap image;
    RelativeLayout parentLayout;
    String decryptedMessage = null;
    byte[] dbPublicKey,privateKey,publicKey,secretKey;
    boolean isPaused;
    private static  final  String CHANNEL_ID = "Message Channel";
    private static  final  int NOTIFICATION_ID = 100;
    private final  String [] titles = new String[]{"Chats","Requests","Add Friends"};
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getIntent().getStringExtra("userId");
        setContentView(R.layout.activity_chat_list);
        try {
            loadKeys();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        viewBinding();
        Thread notificationThread = new Thread(new Runnable() {
            @Override
            public void run() {

                loadIncomingMessage();

            }
        });
        notificationThread.start();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadProfileData();
            }
        });
        thread.start();
        pageAdapter adapter = new pageAdapter(ChatList.this,userId);
        vPager.setAdapter(adapter);
        new TabLayoutMediator(tabs,vPager,((tab, position) ->tab.setText(titles[position]))).attach();
        vPager.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }
    public void loadKeys() throws NoSuchPaddingException, NoSuchAlgorithmException {
        SharedPreferences preferences = getSharedPreferences("secretKey",Context.MODE_PRIVATE);
        secretKey = Base64.getDecoder().decode(preferences.getString("secretKey","null"));
        preferences = getSharedPreferences("RSA",Context.MODE_PRIVATE);
        privateKey = Base64.getDecoder().decode(preferences.getString("privateKey","null"));
        publicKey = Base64.getDecoder().decode(preferences.getString("publicKey","null"));
    }
    public void viewBinding()
    {
        profilePicture = findViewById(R.id.profilePicture);
        vPager = findViewById(R.id.vPager);
        tabs =  findViewById(R.id.tabs);
        parentLayout = findViewById(R.id.parentLayout);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#075e54"));
    }
    public void loadProfileData(){
        DatabaseReference userReference = database.getReference("users");
        Log.w("userId",userId);
        userReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    photoUrl = String.valueOf(snapshot.child("photoUrl").getValue());
                    Glide.with(getApplicationContext()).load(photoUrl).into(profilePicture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
    public void loadIncomingMessage(){
        DatabaseReference reference = database.getReference("chats");
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot snap: snapshot.getChildren())
                    {
                        loadSenderMessage(snap.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
    public void loadSenderMessage(String senderId){
        DatabaseReference reference = database.getReference("chats");
        reference.child(userId).child(senderId).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        messageModel messageModel = snap.getValue(com.chitchat.database.messageModel.class);
                        assert messageModel != null;
                        if(!messageModel.getSenderId().equals(userId))
                        {
                            if(isPaused)
                            {
                                loadSenderData(senderId,messageModel.getMessage(),messageModel.getSecretKey());
                            }

                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void loadSenderData(String senderId,String message, String secretKey){
        DatabaseReference userReference =  database.getReference("users");
        userReference.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String profilePicture = String.valueOf(snapshot.child("photoUrl").getValue());
                    String userName = String.valueOf(snapshot.child("fullName").getValue());
                    processData(userName,profilePicture,message,secretKey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void processData(String userName, String profilePicture, String message, String secretKey) {
        SharedPreferences preferences = getSharedPreferences("RSA",MODE_PRIVATE);
        byte[] privateKey = Base64.getDecoder().decode(preferences.getString("privateKey","null"));
        AES aes = new AES();

        try {
            decryptedMessage = aes.decryption(message,secretKey,privateKey);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Thread notificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                notificationManager(userName,profilePicture,decryptedMessage);
            }
        });
        notificationThread.start();

    }
    public void notificationManager(String userName,String profilePicture,String message) {
        try {
            URL url = new URL(profilePicture);
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        } catch (IOException e) {
            System.out.println(e);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this)
                .setLargeIcon(image)
                .setSmallIcon(R.drawable.logo)
                .setContentText(message)
                .setSubText(userName + " sent you a message")
                .setChannelId(CHANNEL_ID)
                .build();
        notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,"Messaging Channel",NotificationManager.IMPORTANCE_HIGH));
        notificationManager.notify(NOTIFICATION_ID,notification);
        if(isPaused)
        {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
        }

    }

    public void onStart(){
        super.onStart();
        DatabaseReference reference = database.getReference("users");
        reference.child(userId).child("onlineStatus").setValue(1);
        isPaused = false;
    }

    public void onResume(){
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        DatabaseReference reference = database.getReference("users");
        reference.child(userId).child("onlineStatus").setValue(0);
        isPaused = true;
    }
}
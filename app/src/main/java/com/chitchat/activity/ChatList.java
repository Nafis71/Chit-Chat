package com.chitchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chitchat.R;
import com.chitchat.database.pageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatList extends AppCompatActivity {
    ImageView profilePicture;
    String userId,photoUrl,fullName;
    ViewPager2 vPager;
    TabLayout tabs;
    private final  String [] titles = new String[]{"Chats","Requests","Add Friends"};
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getIntent().getStringExtra("userId");
        setContentView(R.layout.activity_chat_list);
        viewBinding();
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
    public void viewBinding()
    {
        profilePicture = findViewById(R.id.profilePicture);
        vPager = findViewById(R.id.vPager);
        tabs =  findViewById(R.id.tabs);
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

    public void onStart(){
        super.onStart();
        DatabaseReference reference = database.getReference("users");
        reference.child(userId).child("onlineStatus").setValue(1);
    }


    @Override
    protected void onPause() {
        super.onPause();
        DatabaseReference reference = database.getReference("users");
        reference.child(userId).child("onlineStatus").setValue(0);
    }
}
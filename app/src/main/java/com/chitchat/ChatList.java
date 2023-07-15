package com.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Base64;

public class ChatList extends AppCompatActivity {
    ImageView profilePicture;
    RecyclerView recyclerView;
    ArrayList<userListModel> list;
    userListAdapter adapter;
    String userId,photoUrl,fullName;
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
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatList.this));
        list = new ArrayList<>();
        adapter = new userListAdapter(this,list,userId);
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollToPosition(adapter.getItemCount());
        DatabaseReference reference = database.getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        userListModel model = snap.getValue(userListModel.class);
                        if(!model.getUserId().equals(userId))
                        {
                            list.add(model);
                        }
                    }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }
    public void viewBinding()
    {
        profilePicture = findViewById(R.id.profilePicture);
    }
    public void loadProfileData(){
        DatabaseReference USerreference = database.getReference("users");
        Log.w("userId",userId);
        USerreference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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
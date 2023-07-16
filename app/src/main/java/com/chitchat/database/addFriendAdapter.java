package com.chitchat.database;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chitchat.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Handler;

public class addFriendAdapter extends RecyclerView.Adapter<addFriendAdapter.myViewHolder> {
    Context context;
    ArrayList<userListModel> model;
    String userId;
    RelativeLayout parentLayout;
    String receiverId,senderId;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");

    public addFriendAdapter(Context context, ArrayList<userListModel> model,String userId,RelativeLayout parentLayout) {
        this.context = context;
        this.model = model;
        this.userId = userId;
        this.parentLayout = parentLayout;
    }


    @NonNull
    @Override
    public addFriendAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.single_row_deisgn_add_friend,parent,false);
       return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull addFriendAdapter.myViewHolder holder, int position) {
        userListModel dbmodel = model.get(position);
        holder.userName.setText(dbmodel.getFullName());
        Glide.with(context).load(dbmodel.getPhotoUrl()).into(holder.profilePicture);
        DatabaseReference reference = database.getReference("requests");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (dbmodel.getUserId().equals(snap.child("receiverId").getValue())) {
                            receiverId = String.valueOf(snap.child("receiverId").getValue());
                            senderId = String.valueOf(snap.child("senderId").getValue());
                            holder.addFriend.setImageResource(R.drawable.pending);
                            holder.addFriend.setClickable(false);
                        }
                    }
                }else{
                    holder.addFriend.setImageResource(R.drawable.adduser);
                    holder.addFriend.setClickable(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        DatabaseReference friendListReference = database.getReference("friendList");
        friendListReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot snap: snapshot.getChildren())
                    {
                        if(dbmodel.getUserId().equals(snap.child("friendId").getValue()))
                        {
                            holder.addFriend.setImageResource(R.drawable.useradded);
                            holder.addFriend.setClickable(false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw  error.toException();
            }
        });

        holder.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference addFriendReference = database.getReference("requests");
                HashMap<String,String> hashMap = new HashMap<>();
                String uniqueKey = uniqueKey();
                hashMap.put("senderId",userId); hashMap.put("receiverId", dbmodel.getUserId());
                addFriendReference.child(uniqueKey).setValue(hashMap);
                holder.addFriend.setImageResource(R.drawable.pending);
                Snackbar.make(parentLayout,"Message Request Sent to "+dbmodel.getFullName(),Snackbar.LENGTH_LONG).show();
            }
        });

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture,addFriend;
        TextView userName;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            addFriend = itemView.findViewById(R.id.addFriend);
            userName = itemView.findViewById(R.id.userName);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
    public String uniqueKey(){
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }
}

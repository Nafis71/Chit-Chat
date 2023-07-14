package com.chitchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class userListAdapter extends RecyclerView.Adapter<userListAdapter.myViewHolder> {
    Context context;
    ArrayList<userListModel> model;

    public userListAdapter(Context context, ArrayList<userListModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public userListAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_user_list,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userListAdapter.myViewHolder holder, int position) {
                userListModel dbmodel = model.get(position);
                Glide.with(context).load(dbmodel.getPhotoUrl()).into(holder.profilePicture);
                holder.userName.setText(dbmodel.getFullName());
                FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");
                DatabaseReference reference = database.getReference("users");
                reference.child(dbmodel.getUserId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String status =  String.valueOf(snapshot.child("onlineStatus").getValue());
                            if(Integer.parseInt(status) != 0 )
                            {
                                holder.onlineStatus.setText("Online");
                                holder.onlineStatus.setTextColor(Color.parseColor("#28B463"));
                            }
                            else
                            {
                                holder.onlineStatus.setText("Offline");
                                holder.onlineStatus.setTextColor(Color.parseColor("#666666"));
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        throw error.toException();
                    }
                });
                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ChatRoom.class);
                        intent.putExtra("userId",dbmodel.getUserId());
                        intent.putExtra("photoUrl",dbmodel.getPhotoUrl());
                        intent.putExtra("fullName",dbmodel.getFullName());
                        context.startActivity(intent);
                    }
                });

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView userName,onlineStatus;
        CardView card;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            userName = itemView.findViewById(R.id.userName);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
            card = itemView.findViewById(R.id.card);
        }
    }
    @Override
    public int getItemCount() {
        return model.size();
    }
}

package com.chitchat.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chitchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class messageAdapter extends RecyclerView.Adapter<messageAdapter.myViewHolder> {
    final private int LEFT_CHAT = 0;
    final private  int RIGHT_CHAT = 1;
    Context context;
    ArrayList<messageModel> model;

    public messageAdapter(Context context, ArrayList<messageModel> model) {
        this.context = context;
        this.model = model;

    }

    @NonNull
    @Override
    public messageAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == RIGHT_CHAT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);
            return new myViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);
            return new myViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull messageAdapter.myViewHolder holder, int position) {
        messageModel dbmodel = model.get(position);
        holder.showMessage.setText(dbmodel.getMessage());
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = database.getReference("users");
        reference.child(dbmodel.getSenderId()).child("photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Glide.with(context).load(String.valueOf(snapshot.getValue())).into(holder.profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView showMessage;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            showMessage = itemView.findViewById(R.id.show_message);
        }
    }
    @Override
    public int getItemCount() {
        return model.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        assert fuser != null;
        if(model.get(position).getSenderId().equals(fuser.getUid()))
        {
            return RIGHT_CHAT;
        }
        else
        {
            return LEFT_CHAT;
        }
    }
}

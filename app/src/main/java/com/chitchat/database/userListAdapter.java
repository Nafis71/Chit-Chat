package com.chitchat.database;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chitchat.encryption.AES;
import com.chitchat.activity.ChatRoom;
import com.chitchat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.NoSuchPaddingException;

public class userListAdapter extends RecyclerView.Adapter<userListAdapter.myViewHolder> {
    Context context;
    ArrayList<userListModel> model;
    AES aes = new AES();
    byte[] privateKey,secretKey;
    String userId,chatKey,senderId;
    int counter =0;

    public userListAdapter(Context context, ArrayList<userListModel> model,String userId) {
        this.context = context;
        this.model = model;
        this.userId = userId;
    }

    @NonNull
    @Override
    public userListAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_user_list,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userListAdapter.myViewHolder holder, int position) {
                loadKeys();
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
                DatabaseReference chatReference = database.getReference("chats");
                chatReference.limitToLast(1).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                chatKey = snap.getKey();
                                counter = 0;
                                messageModel model = snap.getValue(messageModel.class);
                                assert model != null;
                                senderId = model.getSenderId();
                                if(model.getSenderId().equals(dbmodel.getUserId()))
                                {
                                    String decryptedMessage = null;
                                    try {
                                       decryptedMessage =  aes.decryption(model.getMessage(),model.getSecretKey(),privateKey);
                                    } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                                        holder.message.setText("No messages");
                                    }
                                    if(decryptedMessage != null)
                                    {
                                        if(model.getSeenStatus().equals("unseen"))
                                        {
                                            holder.message.setTypeface(Typeface.DEFAULT_BOLD);
                                            String message = decryptedMessage + " - New message";
                                            holder.message.setText(message);
                                        }
                                        else{
                                            holder.message.setTypeface(Typeface.DEFAULT);
                                            holder.message.setText(decryptedMessage);
                                        }
                                    }else{
                                        holder.message.setText("No messages");
                                    }


                                }
                                else if(!model.getReceiverId().equals(userId) && dbmodel.getUserId().equals(model.getReceiverId()))
                                {
                                    String decryptedMessage = null;
                                    try {
                                        decryptedMessage = aes.decryptionSender(model.getMessage(),secretKey);
                                    } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                                        throw new RuntimeException(e);
                                    }
                                    if(decryptedMessage!= null)
                                    {
                                        String message = "You: " +decryptedMessage;
                                        holder.message.setText(message);
                                    }else{
                                        holder.message.setText("No messages");
                                    }

                                }
                                else {
                                    holder.message.setText("No Messages");
                                }
                            }
                        }else{
                            holder.message.setText("No Messages");
                            counter = 1;
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
                        holder.message.setTypeface(Typeface.DEFAULT);
                        if(counter == 0 && !senderId.equals(userId))
                        {
                            DatabaseReference reference = database.getReference("chats");
                            reference.child(chatKey).child("seenStatus").setValue("seen");
                        }
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
        TextView userName,onlineStatus,message;
        CardView card;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            userName = itemView.findViewById(R.id.userName);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
            card = itemView.findViewById(R.id.card);
            message = itemView.findViewById(R.id.message);
        }
    }
    @Override
    public int getItemCount() {
        return model.size();
    }

    public void loadKeys(){
        SharedPreferences preferences = context.getSharedPreferences("RSA",Context.MODE_PRIVATE);
        privateKey = Base64.getDecoder().decode(preferences.getString("privateKey","null"));
        Log.w("PrivateKEy",Base64.getEncoder().encodeToString(privateKey));
        preferences = context.getSharedPreferences("secretKey",Context.MODE_PRIVATE);
        secretKey = Base64.getDecoder().decode(preferences.getString("secretKey","null"));
    }
}

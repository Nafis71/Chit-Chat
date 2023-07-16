package com.chitchat.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;
import android.os.Handler;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.logging.LogRecord;

public class messageRequestAdapter extends RecyclerView.Adapter<messageRequestAdapter.myViewHolder> {
    Context context;
    ArrayList<checkRequest> model;
    String userId,keyValue,fullName;
    RelativeLayout parentLayout;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");

    public messageRequestAdapter(Context context, ArrayList<checkRequest> model, String userId, RelativeLayout parentLayout) {
        this.context = context;
        this.model = model;
        this.userId = userId;
        this.parentLayout = parentLayout;
    }

    @NonNull
    @Override
    public messageRequestAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_message_request,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull messageRequestAdapter.myViewHolder holder, @SuppressLint("RecyclerView") int position) {
        checkRequest dbmodel = model.get(position);
        DatabaseReference reference = database.getReference("users");
        reference.child(dbmodel.getSenderId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    fullName = String.valueOf(snapshot.child("fullName").getValue());
                    holder.userName.setText(fullName);
                    Glide.with(context).load(String.valueOf(snapshot.child("photoUrl").getValue())).into(holder.profilePicture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw  error.toException();
            }
        });

        holder.decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = database.getReference("requests");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                checkRequest request = snap.getValue(checkRequest.class);
                                if(dbmodel.getSenderId().equals(request.getSenderId()) && request.getReceiverId().equals(dbmodel.getReceiverId())){
                                    keyValue = snap.getKey();
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        throw error.toException();
                    }
                });
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseReference reference = database.getReference("requests");
                        reference.child(keyValue).removeValue();
                        model.remove(model.get(position));
                        notifyItemRemoved(position);
                        Snackbar.make(parentLayout,fullName + "'s Request Declined Successfully",Snackbar.LENGTH_LONG).show();
                    }
                },2000);

            }
        });
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = database.getReference("requests");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                checkRequest request = snap.getValue(checkRequest.class);
                                if(dbmodel.getSenderId().equals(request.getSenderId()) && request.getReceiverId().equals(dbmodel.getReceiverId())){
                                    DatabaseReference acceptReference = database.getReference("friendList");
                                    acceptReference.child(userId).push().child("friendId").setValue(dbmodel.getSenderId());
                                    acceptReference.child(dbmodel.getSenderId()).push().child("friendId").setValue(userId);
                                    keyValue = snap.getKey();
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        throw error.toException();
                    }
                });
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseReference reference = database.getReference("requests");
                        reference.child(keyValue).removeValue();
                        model.remove(model.get(position));
                        notifyItemRemoved(position);
                        Snackbar.make(parentLayout,fullName + "'s Request Accepted Successfully",Snackbar.LENGTH_LONG).show();
                    }
                },2000);
            }
        });


    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture,accept,decline;
        TextView userName;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            accept = itemView.findViewById(R.id.accept);
            decline = itemView.findViewById(R.id.decline);
            userName = itemView.findViewById(R.id.userName);
        }
    }


    @Override
    public int getItemCount() {
        return model.size();
    }
}

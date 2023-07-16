package com.chitchat.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chitchat.R;
import com.chitchat.database.userListAdapter;
import com.chitchat.database.userListModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ChatListFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<userListModel> list;
    userListAdapter adapter;
    String userId;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");

    public ChatListFragment() {
        // Required empty public constructor
    }
    public ChatListFragment(String userId) {
        this.userId = userId;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = requireView().findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        list = new ArrayList<>();
        adapter = new userListAdapter(requireContext(),list,userId);
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollToPosition(adapter.getItemCount());
        ArrayList<String> friendIds = new ArrayList<>();
        DatabaseReference reference = database.getReference("friendList");
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if(snapshot.exists())
                {
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        String friendId = String.valueOf(snap.child("friendId").getValue());
                        Log.w("frienIDUSerlist",friendId);
                        addUser(friendId);
                    }
                }else{
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }
    public void addUser(String id)
    {
                    DatabaseReference userReference = database.getReference("users");
                    userReference.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                DataSnapshot snapshot = task.getResult();
                                {
                                    userListModel model = new userListModel();
                                    model.setFullName(String.valueOf(snapshot.child("fullName").getValue()));
                                    model.setUserId(String.valueOf(snapshot.child("userId").getValue()));
                                    model.setPhotoUrl(String.valueOf(snapshot.child("photoUrl").getValue()));
                                    model.setOnlineStatus(Integer.parseInt(String.valueOf(snapshot.child("onlineStatus").getValue())));
                                    list.add(model);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }
}
package com.chitchat.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chitchat.R;
import com.chitchat.database.addFriendAdapter;
import com.chitchat.database.userListModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class AddFriendsFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<userListModel> list = new ArrayList<>();
    ArrayList<userListModel> secondModel = new ArrayList<>();
    addFriendAdapter adapter;

    String userId;
    RelativeLayout parentLayout;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");

    public AddFriendsFragment() {
        // Required empty public constructor
    }

    public  AddFriendsFragment(String userId)
    {
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
        parentLayout = requireView().findViewById(R.id.parentLayout);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new addFriendAdapter(requireContext(),list,userId,parentLayout);
        recyclerView.setAdapter(adapter);
        DatabaseReference userListReference = database.getReference("users");
        userListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if(snapshot.exists())
                {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        userListModel model = snap.getValue(userListModel.class);
                        assert model != null;
                        if (!model.getUserId().equals(userId)) {
                            list.add(model);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_friends, container, false);
    }
}
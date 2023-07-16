package com.chitchat.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chitchat.R;
import com.chitchat.database.checkRequest;
import com.chitchat.database.messageRequestAdapter;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MessageRequestFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<checkRequest> list = new ArrayList<>();
    messageRequestAdapter adapter;
    RelativeLayout parentLayout;
    LinearProgressIndicator progressBar;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");
    String userId;
    public MessageRequestFragment() {
        // Required empty public constructor
    }

    public MessageRequestFragment( String userId) {
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
        progressBar = recyclerView.findViewById(R.id.progressBar);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        DatabaseReference reference = database.getReference("requests");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if(snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                       checkRequest request = snap.getValue(checkRequest.class);
                        assert request != null;
                        if(request.getReceiverId().equals(userId))
                        {
                           list.add(request);
                        }
                        adapter = new messageRequestAdapter(requireContext(),list,userId,parentLayout);
                        recyclerView.setAdapter(adapter);
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_request, container, false);
    }
}
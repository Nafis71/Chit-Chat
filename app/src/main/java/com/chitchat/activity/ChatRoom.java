package com.chitchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.chitchat.R;
import com.chitchat.database.messageAdapter;
import com.chitchat.database.messageModel;
import com.chitchat.encryption.AES;
import com.chitchat.encryption.RSA;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ChatRoom extends AppCompatActivity {
    EditText textBox;
    ImageView sendButton,profilePicture;
    TextView userName,noConversationText,onlineStatus,seenStatus;
    LottieAnimationView noConversationAnimation;
    RecyclerView recyclerView;
    ArrayList<messageModel> list = new ArrayList<>();
    messageAdapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");

    DatabaseReference reference = database.getReference("chats");
    FirebaseAuth auth = FirebaseAuth.getInstance();
    ValueEventListener listener;
    FirebaseUser senderId = auth.getCurrentUser();
    String receiverId,receiverName,receiverPhoto;
    RelativeLayout parentLayout;
    byte[] publicKey,privateKey,encryptionKey,decryptionKey,secretKey,dbPublicKey,senderPublicKey;
//    byte [] encryptionKey = {9,115,51,86,105,4,-31,-23,-68,88,17,20,3,-105,119,-53};
    RSA rsa = new RSA();
    AES aes = new AES();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("fullName");
        receiverPhoto = getIntent().getStringExtra("photoUrl");
        dbPublicKey = Base64.getDecoder().decode(getIntent().getStringExtra("dbPublicKey"));
        senderPublicKey = Base64.getDecoder().decode(getIntent().getStringExtra("senderPublicKey"));
        loadReceiverPublicKey();
        viewBinding();
        try {
            getRSAKeys();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            loadSecretKey();
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeySpecException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                  readMessages();
            }
        });
        thread.start();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendMessage();
                } catch (NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeySpecException |
                         InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public void loadReceiverPublicKey(){
        DatabaseReference reference = database.getReference("users");
        reference.child(receiverId).child("publicKey").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    DataSnapshot  snapshot = task.getResult();
                    publicKey = Base64.getDecoder().decode(String.valueOf(snapshot.getValue()));
                }
            }
        });
    }
    public void viewBinding(){
        textBox = findViewById(R.id.textBox);
        sendButton = findViewById(R.id.sendButton);
        recyclerView = findViewById(R.id.recyclerView);
        profilePicture = findViewById(R.id.profilePicture);
        userName = findViewById(R.id.userName);
        onlineStatus = findViewById(R.id.onlineStatus);
        noConversationAnimation = findViewById(R.id.noConversationAnimation);
        noConversationText = findViewById(R.id.noConversationText);
        seenStatus = findViewById(R.id.seenStatus);
        Glide.with(this).load(receiverPhoto).into(profilePicture);
        userName.setText(receiverName);
        parentLayout = findViewById(R.id.parentLayout);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#075e54"));
//        parentLayout.getBackground().setAlpha(100);
    }
    public void loadSecretKey() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        SharedPreferences preferences = getSharedPreferences("secretKey",MODE_PRIVATE);
        String storedSecretKey = preferences.getString("secretKey","null");
        secretKey = Base64.getDecoder().decode(storedSecretKey);
    }
    public void getRSAKeys() throws NoSuchPaddingException, NoSuchAlgorithmException {
        SharedPreferences preferences = getSharedPreferences("RSA",MODE_PRIVATE);
        privateKey = Base64.getDecoder().decode(preferences.getString("privateKey",""));
    }
    public void readMessages(){
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        listener = reference.child(senderId.getUid()).child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                int count = 0;
                for(DataSnapshot snap : snapshot.getChildren())
                {
                    messageModel model = snap.getValue(messageModel.class);
                    Log.w("Key",snap.getValue().toString());
                    assert model != null;
                    if(model.getReceiverId().equals(receiverId) && model.getSenderId().equals(senderId.getUid())&& Base64.getEncoder().encodeToString(dbPublicKey).equals(Base64.getEncoder().encodeToString(senderPublicKey))){
                        String decryptedMessage = null;
                        try {
                            decryptedMessage = aes.decryptionSender(model.getMessage(),secretKey);

                        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        noConversationAnimation.setVisibility(View.GONE);
                        noConversationText.setVisibility(View.GONE);

                        model.setMessage(decryptedMessage);
                        list.add(model);
                        if(model.getSeenStatus().equals("seen")) {
                            seenStatus.setVisibility(View.VISIBLE);
                        }else{
                            seenStatus.setVisibility(View.GONE);
                        }
                    }
                    else if(model.getReceiverId().equals(senderId.getUid()) && model.getSenderId().equals(receiverId) && Base64.getEncoder().encodeToString(dbPublicKey).equals(Base64.getEncoder().encodeToString(senderPublicKey)))
                    {
                        String decryptedMessage = null;
                        try {
                            decryptedMessage = aes.decryption(model.getMessage(),model.getSecretKey(),privateKey);

                        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        noConversationAnimation.setVisibility(View.GONE);
                        noConversationText.setVisibility(View.GONE);

                        model.setMessage(decryptedMessage);
                        list.add(model);
                        DatabaseReference seenReference = database.getReference("chats");
                        seenReference.child(senderId.getUid()).child(receiverId).child(snap.getKey()).child("seenStatus").setValue("seen");
                        if(model.getSeenStatus().equals("seen")) {
                            seenStatus.setVisibility(View.GONE);
                        }else{
                            seenStatus.setVisibility(View.GONE);
                        }
                    }
                    else {
                        noConversationAnimation.setVisibility(View.VISIBLE);
                        noConversationText.setVisibility(View.VISIBLE);
                    }
                    adapter = new messageAdapter(ChatRoom.this,list);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendMessage() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        String message = textBox.getText().toString().trim();
        if(message.isEmpty())
        {
            return;
        }
        String encryptedMessage = aes.encryption(message,secretKey);
        encryptionKey = rsa.encrypt(secretKey,publicKey);
        HashMap<String,String> hashMap = new HashMap<>();
        assert senderId != null;
        hashMap.put("receiverId",receiverId);hashMap.put("senderId",senderId.getUid());
        hashMap.put("message",encryptedMessage);hashMap.put("secretKey",Base64.getEncoder().encodeToString(encryptionKey));
        hashMap.put("seenStatus","unseen");
        DatabaseReference reference = database.getReference();
        reference.child("chats").child(senderId.getUid()).child(receiverId).push().setValue(hashMap);
        reference.child("chats").child(receiverId).child(senderId.getUid()).push().setValue(hashMap);
        textBox.setText(null);
    }
    public void onStart(){
        super.onStart();
        DatabaseReference reference = database.getReference("users");
        reference.child(senderId.getUid()).child("onlineStatus").setValue(1);
        reference.child(receiverId).child("onlineStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = String.valueOf(snapshot.getValue());
                if(Integer.parseInt(status) > 0)
                {
                    onlineStatus.setText("Online");
                }
                else
                {
                    onlineStatus.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onBackPressed()
    {
        reference.removeEventListener(listener);
        finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        DatabaseReference reference = database.getReference("users");
        reference.child(senderId.getUid()).child("onlineStatus").setValue(0);
    }
}
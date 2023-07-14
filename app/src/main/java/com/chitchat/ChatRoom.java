package com.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class ChatRoom extends AppCompatActivity {
    EditText textBox;
    ImageView sendButton,profilePicture;
    TextView userName,noConversationText;
    LottieAnimationView noConversationAnimation;
    RecyclerView recyclerView;
    ArrayList<messageModel> list = new ArrayList<>();
    messageAdapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser senderId = auth.getCurrentUser();
    String receiverId,receiverName,receiverPhoto;
    byte [] encryptionKey = {9,115,51,86,105,4,-31,-23,-68,88,17,20,3,-105,119,-53};
    Cipher cipher, decipher;
    SecretKeySpec secretKeySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("fullName");
        receiverPhoto = getIntent().getStringExtra("photoUrl");
        viewBinding();
        encryptionSetup();
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
                sendMessage();
            }
        });
    }
    public void viewBinding(){
        textBox = findViewById(R.id.textBox);
        sendButton = findViewById(R.id.sendButton);
        recyclerView = findViewById(R.id.recyclerView);
        profilePicture = findViewById(R.id.profilePicture);
        userName = findViewById(R.id.userName);
        noConversationAnimation = findViewById(R.id.noConversationAnimation);
        noConversationText = findViewById(R.id.noConversationText);
        Glide.with(this).load(receiverPhoto).into(profilePicture);
        userName.setText(receiverName);
    }
    public void encryptionSetup(){
        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        secretKeySpec = new SecretKeySpec(encryptionKey,"AES");
    }
    public void readMessages(){
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        DatabaseReference reference = database.getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                int count = 0;
                for(DataSnapshot snap : snapshot.getChildren())
                {
                    messageModel model = snap.getValue(messageModel.class);
                    if(model.getReceiverId().equals(senderId.getUid()) && model.getSenderId().equals(receiverId) ||
                        model.getReceiverId().equals(receiverId) && model.getSenderId().equals(senderId.getUid())){
                        String decryptedMessage = AESDecryption(model.getMessage());
                        if(count == 0)
                        {
                            noConversationAnimation.setVisibility(View.GONE);
                            noConversationText.setVisibility(View.GONE);
                        }
                        count = 1;
                        model.setMessage(decryptedMessage);
                        list.add(model);
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
    private void sendMessage(){
        String message = textBox.getText().toString().trim();
        String encryptedMessage = AESEncryption(message);
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("receiverId",receiverId);
        assert senderId != null;
        hashMap.put("senderId",senderId.getUid());
        hashMap.put("message",encryptedMessage);
        DatabaseReference reference = database.getReference();
        reference.child("chats").push().setValue(hashMap);
    }
    private String AESEncryption(String message){
        byte[] stringByte = message.getBytes();
        byte[] encryptedByte; //The number of consecutive (non-overlapping) bytes in a byte string.
        try{
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte); //DoFinal() Finishes a multiple-part encryption or decryption operation, depending on how this cipher was initialized. DoFinal(Byte[]) Encrypts or decrypts data in a single-part operation, or finishes a multiple-part operation
        }catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw  new RuntimeException(e);
        }
        return new String(encryptedByte, StandardCharsets.ISO_8859_1);
    }
    private String AESDecryption(String message){
        byte[] encryptedByte = message.getBytes(StandardCharsets.ISO_8859_1);
        String decryptedString;
        byte[] decryption;
        try {
            decipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
            decryption = decipher.doFinal(encryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return decryptedString;

    }
}
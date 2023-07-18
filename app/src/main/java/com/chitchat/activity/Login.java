package com.chitchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.chitchat.R;
import com.chitchat.encryption.AES;
import com.chitchat.encryption.RSA;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
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
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Login extends AppCompatActivity {
    Button signUp,signIn;
    TextInputLayout email,password;
    TextInputEditText emailText, passwordText;
    LinearProgressIndicator progressBar;
    LinearLayout parentLayout;
    byte[] publicKey,privateKey;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;


    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");

    public void onStart() {
        super.onStart();
        user = auth.getCurrentUser();
        if(user != null)
        {
            Intent intent = new Intent(Login.this, ChatList.class);
            intent.putExtra("userId",user.getUid());
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        viewBinding();
        stateListener();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateEmail() |! validatePassword())
                {
                    return;
                }
                login();
            }
        });
    }
    public void viewBinding()
    {
        signUp = findViewById(R.id.signUp);
        progressBar = findViewById(R.id.progressBar);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signIn = findViewById(R.id.signIn);
        parentLayout = findViewById(R.id.parentLayout);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
    }
    public void login(){
        progressBar.setVisibility(View.VISIBLE);
        String userEmail = email.getEditText().getText().toString().trim();
        String userPassword = password.getEditText().getText().toString().trim();
        auth.signInWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Handler handler = new Handler();
                    user = auth.getCurrentUser();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(user.isEmailVerified())
                            {

                                try {
                                    AdvanceEncryptionStandardSetup();
                                } catch (NoSuchPaddingException | IllegalBlockSizeException |
                                         NoSuchAlgorithmException | BadPaddingException |
                                         InvalidKeySpecException | InvalidKeyException e) {
                                    throw new RuntimeException(e);
                                }
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(Login.this,ChatList.class);
                                intent.putExtra("userId",user.getUid());
                                startActivity(intent);
                                finish();
                            }
                            else {
                                progressBar.setVisibility(View.GONE);
                                Snackbar snackbar =  Snackbar.make(parentLayout, "Text",Snackbar.LENGTH_INDEFINITE);
                                snackbar.setText("Email is not verified!").setAction("Resend Email", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                snackbar.dismiss();
                                            }
                                        });
                                    }
                                });
                                snackbar.show();
                            }
                        }
                    },6000);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(parentLayout,"Wrong Credentials",Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });

    }
    public void AdvanceEncryptionStandardSetup() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        SharedPreferences preferences = getSharedPreferences("secretKey",MODE_PRIVATE);
        String StoredSecretKey = preferences.getString("secretKey","null");
        if(StoredSecretKey.equals("null")){
            AES aes = new AES();
            byte[] secretKey = aes.init(256);
            SharedPreferences preferences2 = getSharedPreferences("secretKey",MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences2.edit();
            editor.putString("secretKey", Base64.getEncoder().encodeToString(secretKey));
            editor.apply();
            loadRSA();
        }else{
            loadRSA();
        }
    }
    private void loadRSA() {
        SharedPreferences preferences = getSharedPreferences("RSA",MODE_PRIVATE);
        String StoredPublicKey = preferences.getString("publicKey","null");
        if(StoredPublicKey.equals("null"))
        {
            RSA rsa = new RSA();
            rsa.init(2048);
            publicKey = rsa.getPublicKey();
            privateKey = rsa.getPrivateKey();
            SharedPreferences StorePreferences = getSharedPreferences("RSA",MODE_PRIVATE);
            SharedPreferences.Editor editor = StorePreferences.edit();
            editor.putString("privateKey", Base64.getEncoder().encodeToString(privateKey));
            editor.putString("publicKey", Base64.getEncoder().encodeToString(publicKey));
            editor.apply();
            DatabaseReference deleteReference = database.getReference("chats");
            deleteReference.child(user.getUid()).removeValue();
            DatabaseReference reference = database.getReference("users");
            reference.child(user.getUid()).child("publicKey").setValue(Base64.getEncoder().encodeToString(publicKey));
        }
    }
    public void stateListener(){
        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                password.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    private boolean validateEmail(){
        String value = email.getEditText().getText().toString().trim();
        String emailPattern = "[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+"; //regex expression
        if(value.isEmpty())
        {
            email.setError("Field can't be empty");
            return false;
        }
        else if(!value.matches(emailPattern))
        {
            email.setError("Invalid email address");
            return false;
        }
        else
        {
            return true;
        }
    }
    private boolean validatePassword()
    {
        String pass = password.getEditText().getText().toString();
        String passwordSpecial = "^" +"(?=.*[@#$%^&+=])"+".{3,}"+"$";
        if(pass.isEmpty())
        {
            password.setError("Password field can't be empty");
            return false;
        }
        else if(pass.length() <= 6)
        {
            password.setError("Password needs to be atleast 6 characters");
            return false;
        }
        else if(!pass.matches(passwordSpecial))
        {
            password.setError("Password is too weak. Add at least 1 special Character");
            return false;
        }
        else
        {
            return true;
        }
    }
}
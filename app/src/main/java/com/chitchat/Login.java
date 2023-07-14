package com.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {
    Button signUp,signIn;
    TextInputLayout email,password;
    TextInputEditText emailText, passwordText;
    LinearProgressIndicator progressBar;
    LinearLayout parentLayout;
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
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(Login.this,ChatList.class);
                                intent.putExtra("userId",user.getUid());
                                startActivity(intent);
                                finishAndRemoveTask();
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
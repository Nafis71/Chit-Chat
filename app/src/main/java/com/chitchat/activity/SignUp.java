package com.chitchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.chitchat.R;
import com.chitchat.encryption.AES;
import com.chitchat.encryption.RSA;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.NoSuchPaddingException;

public class SignUp extends AppCompatActivity {
    TextInputLayout fullName,email,password,confirmPassword;
    TextInputEditText fullNameText,emailText,passwordText,confirmPasswordText;
    Button next,gallary,signUp;
    TextView yourInfoText,emailVerificationText,redirectText;
    ImageView profilePicture,signupImage;
    LinearProgressIndicator progressBar;
    Uri filepath;
    RelativeLayout parentLayout;
    LottieAnimationView registrationSuccessful,emailVerification;
    Bitmap bitmap;
    String photoUrl;
    byte[] publicKey,privateKey;
    FirebaseUser user;
    FirebaseAuth auth;
    private static final byte[] secretKey ={-65, 112, -102, -101, 88, 78, -119, 66, -46, -108, -92, 56, 64, 54, -75, -43, -23, -22, 43, 101, 16, 113, -31, 61, -92, 37, -77, 78, 81, -100, 19, 8};
    //256bit

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://chit-chat-118c1-default-rtdb.asia-southeast1.firebasedatabase.app/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        viewBinding();
        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stateListener();
            }
        });
        backgroundThread.start();
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateEmail()|!validateFullName()|!validatePassword()|!validateConfirmPassword()){
                    return;
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    saveData();
                }
            }
        });
    }
    public void viewBinding(){
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        next = findViewById(R.id.next);
        profilePicture = findViewById(R.id.profilePicture);
        gallary = findViewById(R.id.gallary);
        signUp = findViewById(R.id.SignUp);
        progressBar = findViewById(R.id.progressBar);
        fullNameText = findViewById(R.id.fullNameText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        confirmPasswordText = findViewById(R.id.confirmPasswordText);
        yourInfoText = findViewById(R.id.yourInfoText);
        parentLayout = findViewById(R.id.parentLayout);
        registrationSuccessful = findViewById(R.id.registrationSuccessful);
        signupImage = findViewById(R.id.signupImage);
        emailVerification = findViewById(R.id.emailVerification);
        emailVerificationText = findViewById(R.id.emailVerificationText);
        redirectText = findViewById(R.id.redirectText);
    }
    public void saveData(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String userName = fullName.getEditText().getText().toString().trim();
                String userEmail = email.getEditText().getText().toString().trim();
                String userPassword = password.getEditText().getText().toString().trim();
                fullName.setVisibility(View.GONE);email.setVisibility(View.GONE);password.setVisibility(View.GONE);
                confirmPassword.setVisibility(View.GONE);next.setVisibility(View.GONE); profilePicture.setVisibility(View.VISIBLE);
                gallary.setVisibility(View.VISIBLE);progressBar.setVisibility(View.GONE);yourInfoText.setText("Set a profile picture for your account");
                gallary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chooseImage();
                    }
                });
                signUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signUp.setClickable(false);
                        progressBar.setVisibility(View.VISIBLE);
                        auth = FirebaseAuth.getInstance();
                        auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    user = auth.getCurrentUser();
                                    uploadImage(user.getUid(),userName,userEmail);
                                }
                                else
                                {
                                    progressBar.setVisibility(View.GONE);
                                    signUp.setClickable(true);
                                    Snackbar.make(parentLayout,"Connection Error Please try again",Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(parentLayout,"Connection Error Please try again",Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        },2000);

    }
    public void chooseImage(){
        ImagePicker.with(this)
                .crop(1f, 1f)	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        assert data != null;
        if(resultCode == RESULT_OK)
        {
            filepath = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                profilePicture.setImageBitmap(bitmap);
                signUp.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
    public void uploadImage(String uId,String userName, String userEmail){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference uploader = storage.getReference("profileImage");
        uploader.child(uId).putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploader.child(uId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        photoUrl = uri.toString();
                        yourInfoText.setVisibility(View.GONE);
                        signupImage.setVisibility(View.GONE);
                        profilePicture.setVisibility(View.GONE);
                        gallary.setVisibility(View.GONE);
                        signUp.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        registrationSuccessful.setVisibility(View.VISIBLE);
                        HashMap<String,Object> userData = new HashMap<>();
                        userData.put("fullName",userName); userData.put("email",userEmail);
                        userData.put("photoUrl",photoUrl); userData.put("userId",uId);
                        userData.put("onlineStatus",0);
                        DatabaseReference reference = database.getReference("users");
                        reference.child(uId).setValue(userData);
                        Handler newHandler = new Handler();
                        newHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendEmailVerificationLink();
                            }
                        },3000);

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(parentLayout,"Failed to upload Profile Picture",Snackbar.LENGTH_LONG).show();
            }
        });
    }
    public void sendEmailVerificationLink(){
        progressBar.setVisibility(View.VISIBLE);
        registrationSuccessful.setVisibility(View.GONE);
        emailVerification.setVisibility(View.VISIBLE);
        emailVerificationText.setVisibility(View.VISIBLE);
        redirectText.setVisibility(View.VISIBLE);
        assert user != null;
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(task.isSuccessful())
                        {
                            AdvanceEncryptionStandardSetup();
                            auth.signOut();
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(SignUp.this, Login.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                },4000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(parentLayout,"Connection Error",Snackbar.LENGTH_LONG).show();
            }
        });
    }


    public void AdvanceEncryptionStandardSetup() {
        SharedPreferences preferences = getSharedPreferences("secretKey",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("secretKey", Base64.getEncoder().encodeToString(secretKey));
        editor.apply();
        generateRSA();
    }
    private void generateRSA(){
        AES aes = new AES();
        RSA rsa = new RSA();
        rsa.init(2048);
        publicKey = rsa.getPublicKey();
        privateKey = rsa.getPrivateKey();
        SharedPreferences preferences = getSharedPreferences("RSA",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("privateKey", Base64.getEncoder().encodeToString(privateKey));
        editor.apply();
        byte[] encryptedPrivateKey = null;
        try {
            encryptedPrivateKey = Base64.getDecoder().decode(aes.encryptionRSA(Base64.getEncoder().encodeToString(privateKey),secretKey));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        DatabaseReference reference = database.getReference("users");
        reference.child(user.getUid()).child("publicKey").setValue(Base64.getEncoder().encodeToString(publicKey));
        reference.child(user.getUid()).child("privateKey").setValue(Base64.getEncoder().encodeToString(encryptedPrivateKey));
    }
    private void stateListener(){
        fullNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                fullName.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
        confirmPasswordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                confirmPassword.setError(null);
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
    private boolean validateConfirmPassword()
    {
        String confirmPass = confirmPassword.getEditText().getText().toString();
        if(confirmPass.isEmpty())
        {
            confirmPassword.setError("Confirm password field can't be empty");
            return false;
        }
        else if(!password.getEditText().getText().toString().matches(confirmPassword.getEditText().getText().toString()))
        {
            confirmPassword.setError("Password Didn't match");
            return false;
        }
        else
        {
            return true;
        }

    }
    private boolean validateFullName()
    {
        String value = fullName.getEditText().getText().toString();
        String check = "^[a-zA-Z\\u0020]*$";
        if(value.isEmpty())
        {
            fullName.setError("Field can't be empty");
            return false;
        }
        else if(!value.matches(check))
        {
            fullName.setError("Invalid Name");
            return false;
        }
        else
        {
            return true;
        }
    }
}
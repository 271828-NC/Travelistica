package com.example.travelistica;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    EditText  email, password1, password2;
    TextView goLogin;
    ImageView banner1, banner2;
    ProgressBar progressBar;
    Button register;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Creating the  connection between java and the UI component
        setContentView(R.layout.activity_register);
        email=findViewById(R.id.et_email);
        progressBar=findViewById(R.id.progressBar);
        password1=findViewById(R.id.et_password1);
        password2=findViewById(R.id.et_password2);
        register=findViewById(R.id.btn_register);
        banner1=findViewById(R.id.iv_banner1);
        banner2=findViewById(R.id.iv_banner2);
        goLogin=findViewById(R.id.tv_goLogin);
        register.setOnClickListener(this);
        banner1.setOnClickListener(this);
        banner2.setOnClickListener(this);
        mAuth=FirebaseAuth.getInstance();

        SpannableString ss=new SpannableString(goLogin.getText());//adding the text to the spannable string
        ClickableSpan cs=new ClickableSpan() {
            @Override//this method will change the activity to Login
            public void onClick(@NonNull View view) {
                //if clicked on the underlined part of the textview move it to login activity
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);//Change colour of the clickable part of the string
            }
        };
        ss.setSpan(cs,25,32,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//The "Sign in" part of the TextView is selected
        goLogin.setText(ss);
        goLogin.setMovementMethod(LinkMovementMethod.getInstance());//The "Sign in" part of the TextView is clickable
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.iv_banner1:
            case R.id.iv_banner2:
                startActivity(new Intent(this, LandingActivity.class));
                break;
            case R.id.btn_register:
                registerUser();
                break;

        }
    }
    private void registerUser(){
         String mail = email.getText().toString().trim();
         String pwd1 = password1.getText().toString().trim();
         String pwd2 = password2.getText().toString().trim();
         if(mail.isEmpty()) {//verify if email field is empty
             email.setError("Email is required");
             email.requestFocus();
             return;
         }
         if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches())
         {//verify email pattern
             email.setError("Please provide a valid email");
             email.requestFocus();
             return;
         }
        if(pwd1.isEmpty()) {//verify if password field is empty
            password1.setError("Password is required");
            password1.requestFocus();
            return;
        }
        if(pwd1.length()<6){//verify if password is smaller than 6
            password1.setError("Password should be at least 6 characters");
            password1.requestFocus();
            return;
        }
        if(pwd2.compareTo(pwd1)!=0){//verify if passwords match
            password2.setError("Passwords do not match, try again");
            password2.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);//make progress bar visible
        mAuth.createUserWithEmailAndPassword(mail,pwd1)//library method used to create a user in firebase
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                      if(task.isSuccessful()){
                          User user=new User(mail);//create object user
                          FirebaseDatabase.getInstance().getReference("Users")
                                  //get Firebase instance in which we select the path where the user details will be stored
                                  .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                  .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task) {
                                  if(task.isSuccessful()) {
                                      FirebaseAuth.getInstance().signOut();//sign out
                                      Toast.makeText(RegisterActivity.this, "User has been registered successfully",
                                              Toast.LENGTH_LONG).show();
                                      //start login activity
                                      startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                  }
                                  else
                                      Toast.makeText(RegisterActivity.this,"Failed to register,try again",
                                              Toast.LENGTH_LONG).show();
                                  progressBar.setVisibility(View.GONE);
                              }
                          });

                      }
                      else{
                          Toast.makeText(RegisterActivity.this,"Failed to register,try again",
                                  Toast.LENGTH_LONG).show();
                          progressBar.setVisibility(View.GONE);
                      }
                    }
                });
    }
}
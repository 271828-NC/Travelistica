package com.example.travelistica;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    //objects declaration
    private EditText log_email, log_pwd;
    private ProgressBar progressBar;
    //Entry point for firebase authentication
    private FirebaseAuth mAuth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (user != null) {
            // User is signed in

            Intent i = new Intent(LoginActivity.this,MapsActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);


            }



        log_email = findViewById(R.id.et_email);
        progressBar = findViewById(R.id.progressBar);
        log_pwd = findViewById(R.id.et_password);
        Button login = findViewById(R.id.btn_login);
        ImageView banner1 = findViewById(R.id.iv_banner1);
        ImageView banner2 = findViewById(R.id.iv_banner2);
        TextView resetPass = findViewById(R.id.tv_resetPass);
        TextView goRegister = findViewById(R.id.tv_goRegister);
        login.setOnClickListener(this);
        banner1.setOnClickListener(this);
        banner2.setOnClickListener(this);
        resetPass.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

        SpannableString ss=new SpannableString(goRegister.getText());
        ClickableSpan cs=new ClickableSpan() {
            @Override//this method will change the activity to Register
            public void onClick(@NonNull View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);//Change colour of the clickable part of the string
            }
        };
        ss.setSpan(cs,23,31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//The "Register" part of the TextView is selected
        goRegister.setText(ss);
        goRegister.setMovementMethod(LinkMovementMethod.getInstance());//The "Register" part of the TextView is clickable
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_banner1:
            case R.id.iv_banner2:
                //return to landing page
                startActivity(new Intent(this, LandingActivity.class));
                break;
            case R.id.btn_login:
                //attempt to login
                loginUser();
                break;
            case R.id.tv_resetPass:
                //start forget password activity
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                break;

        }
    }
        private void loginUser() {
            String mail = log_email.getText().toString().trim();
            String pwd = log_pwd.getText().toString().trim();
            if(mail.isEmpty()) {//verify if email field is empty
                log_email.setError("Email is required");
                log_email.requestFocus();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches())
            {//verify email pattern
                log_email.setError("Please provide a valid email");
                log_email.requestFocus();
                return;
            }
            if(pwd.isEmpty()) {//verify if password field is empty
                log_pwd.setError("Password is required");
                log_pwd.requestFocus();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(mail,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                            startActivity(new Intent(LoginActivity.this,MapsActivity.class));


                    }
                    else Toast.makeText(LoginActivity.this,"Failed to login! Please verify your credentials "
                            ,Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
}

package com.example.travelistica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText email;
    ImageView banner;
    Button reset;
    FirebaseAuth auth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        email=findViewById(R.id.et_email);
        banner=findViewById(R.id.iv_banner);
        reset=findViewById(R.id.btn_reset);
        progressBar=findViewById(R.id.progressBar);
        auth=FirebaseAuth.getInstance();
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }
    private void resetPassword(){
        String mail = email.getText().toString().trim();
        if(mail.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches())
        {
            email.setError("Please provide a valid email");
            email.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this,"Check your email for the reset link",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(ForgotPasswordActivity.this,"An error occurred, try again",
                            Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
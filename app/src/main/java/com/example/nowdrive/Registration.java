package com.example.nowdrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registration extends AppCompatActivity implements View.OnClickListener{

    private ImageView nowDrive_logo;
    private EditText reg_email, reg_pass, reg_pass_two;
    private ProgressBar progressBar;
    private Button reg_btn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        nowDrive_logo = (ImageView) findViewById(R.id.nowDrive_logo);
        nowDrive_logo.setOnClickListener(this);

        reg_email = (EditText) findViewById(R.id.reg_email);
        reg_pass = (EditText) findViewById(R.id.reg_pass);
        reg_pass_two = (EditText) findViewById(R.id.reg_pass_two);

        reg_btn = (Button) findViewById(R.id.register_btn);
        reg_btn.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.nowDrive_logo:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.register_btn:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String email = reg_email.getText().toString().trim();
        String password = reg_pass.getText().toString().trim();
        String passwordTwo = reg_pass_two.getText().toString().trim();

        if(email.isEmpty()){
            reg_email.setError("Email is required");
            reg_email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            reg_email.setError("Please provide valid email");
            reg_email.requestFocus();
            return;
        }

        if(password.isEmpty()){
            reg_pass.setError("Password is required");
            reg_pass.requestFocus();
            return;
        }

        if(password.length()<6){
            reg_pass.setError("Password must be greater than 6 characters");
            reg_pass.requestFocus();
            return;
        }
        Pattern password_pattern = Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=]).{6,}$");
        Matcher matcher = password_pattern.matcher(password);
        if(!matcher.matches()){
            reg_pass.setError("Password must contain a Number, a Capital letter and a symbol");
            reg_pass.requestFocus();
            return;
        }

        if(!passwordTwo.equals(password)){
            reg_pass_two.setError("Passwords must match");
            reg_pass_two.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        User user = new User(email,password);

                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    mAuth.signInWithEmailAndPassword(email,password);
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    mAuth.signOut();
                                    user.sendEmailVerification();
                                    Toast.makeText(Registration.this, "User has been registered successfully! Please check your email", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    finish();
                                } else {
                                    Toast.makeText(Registration.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
                    } else {
                        Toast.makeText(Registration.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
    }
}
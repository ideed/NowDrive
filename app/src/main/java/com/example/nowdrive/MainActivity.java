package com.example.nowdrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView register, forgot_password;
    private EditText login_email,login_pass;
    private Button login_btn;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        register = (TextView) findViewById(R.id.register_text);
        forgot_password = (TextView) findViewById(R.id.forgot_password);
        register.setOnClickListener(this);
        forgot_password.setOnClickListener(this);

        login_btn = (Button) findViewById(R.id.login_btn);
        login_btn.setOnClickListener(this);

        login_email = (EditText) findViewById(R.id.login_email);
        login_pass = (EditText)  findViewById(R.id.login_pass);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.register_text:
                startActivity(new Intent(this, Registration.class));
                break;
            case R.id.login_btn:
                userLogin();
                break;
            case R.id.forgot_password:
                startActivity(new Intent(this,ForgotPassword.class));
                break;
        }
    }

    private void userLogin() {
        String email = login_email.getText().toString().trim();
        String password = login_pass.getText().toString().trim();

        if(email.isEmpty()){
            login_email.setError("Email is required!");
            login_email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            login_email.setError("Please provide valid email");
            login_email.requestFocus();
            return;
        }

        if(password.isEmpty()){
            login_pass.setError("Password is required");
            login_pass.requestFocus();
            return;
        }

        if(password.length()<6){
            login_pass.setError("Password must be greater than 6 characters");
            login_pass.requestFocus();
            return;
        }

        progressBar.setVisibility((View.VISIBLE));

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified()){
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(MainActivity.this, HomePage.class));
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        user.sendEmailVerification();
                        Toast.makeText(MainActivity.this,"Check your email to verify your account!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
package com.example.nowdrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener{

    private EditText fp_email;
    private Button fp_button;
    private ProgressBar fp_progressBar;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        fp_email = (EditText) findViewById(R.id.fp_email);

        fp_button = (Button) findViewById(R.id.fp_btn);
        fp_button.setOnClickListener(this);

        fp_progressBar = (ProgressBar) findViewById(R.id.fp_progressbar);

        auth = FirebaseAuth.getInstance();
    }
    @Override
    public void onClick(View v){
        resetPassword();
    }

    private void resetPassword() {
        String email = fp_email.getText().toString().trim();

        if(email.isEmpty()){
            fp_email.setError("Email is required!");
            fp_email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            fp_email.setError("Please provide a valid email");
            fp_email.requestFocus();
            return;
        }

        fp_progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this,"Check your email to reset your password!", Toast.LENGTH_LONG).show();
                    fp_progressBar.setVisibility(View.GONE);
                    finish();
                } else{
                    Toast.makeText(ForgotPassword.this,"Something went wrong! Please make sure your email is correct", Toast.LENGTH_LONG).show();
                    fp_progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
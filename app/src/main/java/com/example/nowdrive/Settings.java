package com.example.nowdrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Settings extends AppCompatActivity implements View.OnClickListener{
    private EditText set_email, set_pass_1, set_pass_2, ver_pass, ver_email;
    private Button set_pass_btn, set_email_btn, set_acc_btn, ver_btn;
    private ImageButton set_back_btn;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String changeType;
    private String changedPass;
    private String changedEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        set_email = (EditText) findViewById(R.id.set_email);
        set_pass_1 = (EditText) findViewById(R.id.set_pass_1);
        set_pass_2 = (EditText) findViewById(R.id.set_pass_2);
        ver_email = (EditText) findViewById(R.id.ver_email);
        ver_pass = (EditText) findViewById(R.id.ver_pass);

        set_pass_btn = (Button) findViewById(R.id.set_pass_btn);
        set_pass_btn.setOnClickListener(this);
        set_email_btn = (Button) findViewById(R.id.set_email_btn);
        set_email_btn.setOnClickListener(this);
        set_acc_btn = (Button) findViewById(R.id.set_acc_btn);
        set_acc_btn.setOnClickListener(this);
        ver_btn = (Button) findViewById(R.id.ver_btn);
        ver_btn.setOnClickListener(this);

        set_back_btn = (ImageButton) findViewById(R.id.set_back_btn);
        set_back_btn.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.set_pass_btn:
                changePassword();
                break;
            case R.id.set_email_btn:
                changeEmail();
                break;
            case R.id.set_acc_btn:
                deleteAccount();
                break;
            case R.id.ver_btn:
                verifyAccount();
                break;
            case R.id.set_back_btn:
                finish();
                break;
        }
    }

    private void verifyAccount() {
        String email = ver_email.getText().toString().trim();
        String pass = ver_pass.getText().toString().trim();
        AuthCredential credential = EmailAuthProvider.getCredential(email,pass);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    if(changeType.equals("pass")){
                        user.updatePassword(changedPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                                    HashMap hashMap=new HashMap();
                                    hashMap.put("password",changedPass);
                                    ref.child(user.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                        @Override
                                        public void onSuccess(@NonNull Object o) {
                                            Toast.makeText(Settings.this,"Password data successfully updated!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Toast.makeText(Settings.this,"Password Successfully updated!", Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(Settings.this,MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Toast.makeText(Settings.this,"An Error has occurred updating the password", Toast.LENGTH_LONG).show();
                                }
                                ver_email.setVisibility(View.GONE);
                                ver_pass.setVisibility(View.GONE);
                                ver_btn.setVisibility(View.GONE);
                                return;
                            }
                        });
                    }
                    if(changeType.equals("email")){
                        user.updateEmail(changedEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                                    HashMap hashMap=new HashMap();
                                    hashMap.put("email",changedEmail);
                                    ref.child(user.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                        @Override
                                        public void onSuccess(@NonNull Object o) {
                                            Toast.makeText(Settings.this,"Email data successfully updated!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Toast.makeText(Settings.this,"Email Successfully updated!", Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(Settings.this,MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Toast.makeText(Settings.this,"An Error has occurred updating the email", Toast.LENGTH_LONG).show();
                                }
                                ver_email.setVisibility(View.GONE);
                                ver_pass.setVisibility(View.GONE);
                                ver_btn.setVisibility(View.GONE);
                                return;
                            }
                        });
                    }
                    if(changeType.equals("acc")){
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                    Query usersQuery = ref.child("Users").orderByChild("email").equalTo(email);
                                    usersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot userSnapshot: snapshot.getChildren()){
                                                userSnapshot.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(Settings.this,"An Error has occurred while removing account", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Toast.makeText(Settings.this,"Account successfully removed!", Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(Settings.this,MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Toast.makeText(Settings.this,"An Error has occurred while removing account", Toast.LENGTH_LONG).show();
                                }
                                ver_email.setVisibility(View.GONE);
                                ver_pass.setVisibility(View.GONE);
                                ver_btn.setVisibility(View.GONE);
                                return;
                            }
                        });
                    }
                } else {
                    Toast.makeText(Settings.this,"Failed to verify, please try again", Toast.LENGTH_LONG).show();
                    ver_email.setVisibility(View.GONE);
                    ver_pass.setVisibility(View.GONE);
                    ver_btn.setVisibility(View.GONE);
                    return;
                }
            }
        });
    }

    private void deleteAccount() {
        ver_email.setVisibility(View.VISIBLE);
        ver_pass.setVisibility(View.VISIBLE);
        ver_btn.setVisibility(View.VISIBLE);

        ver_email.requestFocus();
        changeType = "acc";
        ver_email.setError("Please Verify your account to remove account.");
    }

    private void changeEmail() {
        String email = set_email.getText().toString().trim();
        if(email.isEmpty()){
            set_email.setError("An email must be entered to replace old one");
            set_email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            set_email.setError("Must have a valid email format");
            set_email.requestFocus();
            return;
        }
        ver_email.setVisibility(View.VISIBLE);
        ver_pass.setVisibility(View.VISIBLE);
        ver_btn.setVisibility(View.VISIBLE);

        ver_email.requestFocus();
        changeType="email";
        changedEmail=email;
        ver_email.setError("Please Verify your account to change email.");
    }

    private void changePassword() {
        String password_one = set_pass_1.getText().toString().trim();
        String password_two = set_pass_1.getText().toString().trim();
        if(password_one.isEmpty()){
            set_pass_1.setError("New password is required");
            set_pass_1.requestFocus();
            return;
        }
        if(password_one.length()<6){
            set_pass_1.setError("Password must be greater than 6 characters");
            set_pass_1.requestFocus();
            return;
        }
        Pattern password_pattern = Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=]).{6,}$");
        Matcher matcher = password_pattern.matcher(password_one);
        if(!matcher.matches()){
            set_pass_1.setError("Password must contain a Number, a Capital letter and a symbol");
            set_pass_1.requestFocus();
            return;
        }
        if(!password_one.equals(password_two)){
            set_pass_2.setError("Passwords must match");
            set_pass_2.requestFocus();
            return;
        }

        ver_email.setVisibility(View.VISIBLE);
        ver_pass.setVisibility(View.VISIBLE);
        ver_btn.setVisibility(View.VISIBLE);

        ver_email.requestFocus();
        changeType = "pass";
        changedPass = password_one;
        ver_email.setError("Please Verify your account to change password.");
    }
}
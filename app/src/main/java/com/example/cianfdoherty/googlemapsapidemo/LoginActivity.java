package com.example.cianfdoherty.googlemapsapidemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Admin.AdminHomeActivity;
import com.example.Customer.MapsActivity;
import com.example.Driver.DriverTrackerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etEmail;
    private EditText etPassword;
    private Button bLogin;
    private TextView tvRegisterLink;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String RegisteredUserID = currentUser.getUid();
            DatabaseReference jLoginDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(RegisteredUserID);

            jLoginDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userType = dataSnapshot.child("type").getValue().toString();
                    if (userType.equals("admin")) {
                        startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
                        finish();
                    } else if (userType.equals("driver")) {
                        startActivity(new Intent(getApplicationContext(), DriverTrackerActivity.class));
                        finish();
                    } else if (userType.equals("customer")) {
                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        tvRegisterLink = (TextView) findViewById(R.id.tvRegester);

        progDialog = new ProgressDialog(this);

        bLogin.setOnClickListener(this);
        tvRegisterLink.setOnClickListener(this);
    }

    private void userLogin(){
       String email = etEmail.getText().toString().trim();
       String password = etPassword.getText().toString().trim();
       final String type = "";
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        return;
    }

        progDialog.setMessage("Logging in...");
        progDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progDialog.dismiss();

                        if(task.isSuccessful()){

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String RegisteredUserID = currentUser.getUid();

                            DatabaseReference jLoginDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(RegisteredUserID);

                            jLoginDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String userType = dataSnapshot.child("type").getValue().toString();
                                    if(userType.equals("admin")){
                                        startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
                                        finish();
                                    }else if(userType.equals("driver")){
                                        startActivity(new Intent(getApplicationContext(), DriverTrackerActivity.class));
                                        finish();
                                    }else if(userType.equals("customer")){
                                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                                        finish();
                                    }else{
                                        Toast.makeText(LoginActivity.this, "Failed Login. Please Try Again", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {


                                }
                            });
                        } else {
//                            Toast.makeText(LoginActivity.this, "Failed Login. Please Try Again test", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(LoginActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseAuthInvalidUserException) {
                    Toast.makeText(LoginActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == bLogin){
            userLogin();
        }
        if(v == tvRegisterLink){
            finish();
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }
}

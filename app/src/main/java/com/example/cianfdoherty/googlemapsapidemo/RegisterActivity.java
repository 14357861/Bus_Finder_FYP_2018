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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Customer.MapsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvLoginLink;
    private EditText etPassword;
    private Button bRegister;
    private EditText etUserName;
    private EditText etEmail;
    private Switch sUserType;

    private ProgressDialog progDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        progDialog = new ProgressDialog(this);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bRegister = (Button) findViewById(R.id.bRegister);
        tvLoginLink = (TextView) findViewById(R.id.tvLogin);
//        sUserType = (Switch) findViewById(R.id.sUserType);

        bRegister.setOnClickListener(this);
        tvLoginLink.setOnClickListener(this);
    }


    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        final String userName = etUserName.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progDialog.setMessage("Registering User...");
        progDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progDialog.dismiss();

                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userName).build();
                            user.updateProfile(profileUpdates);

                            finish();

                            startActivity(new Intent(getApplicationContext(), MapsActivity.class));

                        } else {

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthWeakPasswordException) {
                    Toast.makeText(RegisterActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                    Toast.makeText(RegisterActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(RegisterActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onClick(View view) {
        if (view == bRegister) {
            registerUser();
        }
        if (view == tvLoginLink) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}

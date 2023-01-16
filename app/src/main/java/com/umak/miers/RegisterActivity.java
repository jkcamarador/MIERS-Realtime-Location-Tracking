package com.umak.miers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText email_input_su, password_input_su, fullname_input_su, address_input_su;
    TextView already_text;
    Button signup_button;

    ProgressDialog signup_progress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();

        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAccount();
            }
        });

        already_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });
    }

    private void init() {
        email_input_su = findViewById(R.id.editTextTextEmailAddress1);
        password_input_su = findViewById(R.id.editTextTextPassword1);
        fullname_input_su = findViewById(R.id.editTextFullName);
        address_input_su = findViewById(R.id.editTextAddress);
        already_text = findViewById(R.id.textViewAlreadyAccount);
        signup_button = findViewById(R.id.buttonSignUp);
        signup_progress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
    }

    private void registerAccount() {
        String email = email_input_su.getText().toString().trim();
        String password = password_input_su.getText().toString().trim();
        String fullname = fullname_input_su.getText().toString().trim();
        String address = address_input_su.getText().toString().trim();

        if (email.isEmpty()) {
            email_input_su.setError("Email is required!");
            email_input_su.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_input_su.setError("Please provide a valid email!");
            email_input_su.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            password_input_su.setError("Password is required!");
            password_input_su.requestFocus();
            return;
        }
        if (password.length() < 6) {
            password_input_su.setError("Minimum password length should be 6 characters!");
            password_input_su.requestFocus();
            return;
        }

        if (fullname.isEmpty()) {
            fullname_input_su.setError("Full name is required!");
            fullname_input_su.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            address_input_su.setError("Address is required!");
            address_input_su.requestFocus();
            return;
        }

        signup_progress.setTitle("Signing Up");
        signup_progress.setMessage("Please Wait");
        signup_progress.setCanceledOnTouchOutside(false);
        signup_progress.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    User user = new User(email, fullname, address);

                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    signup_progress.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "User has been registered successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Failed to registered! Try again!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    //send email verification function
                    FirebaseUser userhey = mAuth.getCurrentUser();
                    userhey.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Verification email has been sent!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Email not sent!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    signup_progress.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed to registered! Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToLogin() {
        finish();
    }
}
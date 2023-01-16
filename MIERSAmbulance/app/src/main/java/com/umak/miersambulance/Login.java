package com.umak.miersambulance;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    EditText email_input, password_input;
    TextView forgotpass_text, signup_text;
    Button login_button;
    ProgressDialog loginprogress, loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMap();
            }
        });

        signup_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignUp();
            }
        });

        forgotpass_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });
    }

    private void init() {
        email_input = findViewById(R.id.editTextTextEmailAddress1);
        password_input = findViewById(R.id.editTextTextPassword1);
        forgotpass_text = findViewById(R.id.textViewAlreadyAccount);
        login_button = findViewById(R.id.buttonSignUp);
        signup_text = findViewById(R.id.textViewSignUp);

        loginprogress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
    }

    private void goToMap() {
        String email = email_input.getText().toString().trim();
        String password = password_input.getText().toString().trim();

        if (email.isEmpty()) {
            email_input.setError("Email is required!");
            email_input.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_input.setError("Please provide a valid email!");
            email_input.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            password_input.setError("Password is required!");
            password_input.requestFocus();
            return;
        }
        if (password.length() < 6) {
            password_input.setError("Minimum password length should be 6 characters!");
            password_input.requestFocus();
            return;
        }

        loading();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("AmbulanceUsers").child(user.getUid());

                    if (user.isEmailVerified()) {

                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                loginprogress.dismiss();
                                finish();
                                Intent intent = new Intent(Login.this, AmbuMapActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    } else {
                        user.sendEmailVerification();
                        loginprogress.dismiss();
                        Toast.makeText(getApplicationContext(), "Check your email to verify your account!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    loginprogress.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed to login! Please check your credentials!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToSignUp() {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }

    private void forgotPassword() {
        showRecoverPasswordDialog();
    }

    private void loading() {
        loginprogress.setTitle("Logging In");
        loginprogress.setMessage("Please Wait");
        loginprogress.setCanceledOnTouchOutside(false);
        loginprogress.show();
    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailet = new EditText(this);

        emailet.setHint("Enter email here...");
        emailet.setMinEms(16);
        emailet.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailet);
        linearLayout.setPadding(55, 10, 55, 10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailet.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Email is required!", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(getApplicationContext(), "Please provide a valid email!", Toast.LENGTH_SHORT).show();
                } else {
                    beginRecovery(email);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this, "Reset option sent to you email!", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(LoginActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(Login.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
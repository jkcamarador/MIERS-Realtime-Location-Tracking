package com.umak.miers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText email_input, password_input;
    TextView forgotpass_text, signup_text;
    Button login_button;
    ProgressDialog loginprogress, loadingBar;

    SignInButton btSignIn;
    GoogleSignInClient googleSignInClient;
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

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, 100);
            }
        });
    }

    private void init() {
        email_input = findViewById(R.id.editTextTextEmailAddress1);
        password_input = findViewById(R.id.editTextTextPassword1);
        forgotpass_text = findViewById(R.id.textViewAlreadyAccount);
        signup_text = findViewById(R.id.textViewSignUp);
        login_button = findViewById(R.id.buttonSignUp);
        btSignIn = findViewById(R.id.bt_sign_in);
        loginprogress = new ProgressDialog(this);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("360770310230-rebh17gqq3h8ii67h0hf5l4v9jimoaa4.apps.googleusercontent.com").requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, googleSignInOptions);

        mAuth = FirebaseAuth.getInstance();
    }

    private void goToMap() {
        //login authentication
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
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

                    if (user.isEmailVerified()) {

                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                loginprogress.dismiss();
                                Intent intent = new Intent(LoginActivity.this, MapActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("logintype", "customemail");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (signInAccountTask.isSuccessful()) {
                try {
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                    if (googleSignInAccount != null) {
                        loading();
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        mAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            loginprogress.dismiss();
                                            Intent intent = new Intent(LoginActivity.this, MapActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("logintype", "googleaccount");
                                            startActivity(intent);
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void goToSignUp() {
        Intent intent = new Intent(this, RegisterActivity.class);
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
                    Toast.makeText(LoginActivity.this, "Reset option sent to you email!", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(LoginActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.umak.miers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    Button logout_button, onetap_button, emergency_button, firstaid_button, feedback_button, donation_button;
    TextView fullname_text, email_text;
    ImageButton back_button;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        fetchUserData();

        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        onetap_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToOneTap();
            }
        });

        emergency_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToEmergency();
            }
        });

        firstaid_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFirstAid();
            }
        });

        feedback_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFeedback();
            }
        });

        donation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                donation();
            }
        });
    }

    private void init() {
        fullname_text = findViewById(R.id.textViewDevsReply);
        email_text = findViewById(R.id.textViewEmail);

        logout_button = findViewById(R.id.buttonLogout);
        onetap_button = findViewById(R.id.buttonOneTap);
        emergency_button = findViewById(R.id.buttonEmergency);
        firstaid_button = findViewById(R.id.buttonFirstAid);
        feedback_button = findViewById(R.id.buttonFeedback);
        donation_button = findViewById(R.id.buttonDonation);
        back_button = findViewById(R.id.imageButtonBack);
    }

    private void fetchUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                String fullname = userProfile.fullname;
                String email = userProfile.email;

                fullname_text.setText("Name: " + fullname);
                email_text.setText("Email: " + email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void goToOneTap() {
        Intent intent = new Intent(this, OneTapActivity.class);
        startActivity(intent);
    }

    private void goToEmergency() {
        Intent intent = new Intent(this, EmergencyActivity.class);
        startActivity(intent);
    }

    private void goToFirstAid() {
        Intent intent = new Intent(this, FirstAidActivity.class);
        startActivity(intent);
    }

    private void goToFeedback() {
        Intent intent = new Intent(this, FeedBackActivity.class);
        intent.putExtra("userid", user.getUid());
        startActivity(intent);
    }

    private void donation() {
        Intent intent = new Intent(this, DonationActivity.class);
        startActivity(intent);
    }

    private void logout() {

        if (getIntent().getStringExtra("logintype").equals("googleaccount")) {
            googleSignInClient = GoogleSignIn.getClient(HomeActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
            googleSignInClient.signOut();
        } else {
            FirebaseAuth.getInstance().signOut();
        }
        finishAffinity();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void back() {
        finish();
    }
}
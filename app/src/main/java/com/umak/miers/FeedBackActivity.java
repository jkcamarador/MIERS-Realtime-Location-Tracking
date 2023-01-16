package com.umak.miers;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FeedBackActivity extends AppCompatActivity {

    ImageButton back_button, send_button;
    EditText feedback_text;
    CardView autoReplyContainer;
    TextView feedback_title;

    String email, fullname, address, feedback;
    String userid;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);

        init();
        userid = getIntent().getStringExtra("userid");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
        fetchUserData();

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeybaord(view);
                sendFeedback();
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });
    }

    private void init() {
        back_button = findViewById(R.id.imageButtonBack3);
        send_button = findViewById(R.id.imageButtonSend);
        feedback_text = findViewById(R.id.editTextTextMultiLineFeedback);
        autoReplyContainer = findViewById(R.id.cardViewReply);
        feedback_title = findViewById(R.id.textViewTitleFeedback);
        autoReplyContainer.setVisibility(View.INVISIBLE);
    }

    private void fetchUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                fullname = userProfile.fullname;
                email = userProfile.email;
                address = userProfile.address;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendFeedback() {
        feedback = feedback_text.getText().toString().trim();

        if (feedback.isEmpty()) {
            feedback_text.setError("Feedback is required!");
            feedback_text.requestFocus();
            return;
        } else {
            autoReplyContainer.setVisibility(View.VISIBLE);
            send_button.setVisibility(View.INVISIBLE);
            feedback_title.setText("Feedback sent!");
            feedback_title.setGravity(Gravity.CENTER);
            feedback_text.setVisibility(View.INVISIBLE);

            User user = new User(feedback);
            FirebaseDatabase.getInstance().getReference("Feedbacks").child(userid).setValue(user);
        }
    }

    private void hideKeybaord(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }

    private void back() {
        finish();
    }
}
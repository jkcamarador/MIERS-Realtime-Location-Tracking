package com.umak.miers;

import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class FirstAidImageActivity extends AppCompatActivity {

    TextView firstaid_type_text;
    ImageView firstaid_type_image;
    ImageButton back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_aid_image);

        init();
        showImage();

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void init() {
        firstaid_type_text = findViewById(R.id.textViewFirstaidTitle);
        firstaid_type_image = findViewById(R.id.imageViewFirstAidZoom);
        back_button = findViewById(R.id.imageButtonBack4);
    }

    private void showImage() {
        firstaid_type_text.setText(getIntent().getStringExtra("type"));
        Picasso.get().load(getIntent().getStringExtra("image")).into(firstaid_type_image);
    }
}
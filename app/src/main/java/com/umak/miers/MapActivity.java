package com.umak.miers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    Button proceed_button;
    String login_type = "";

    private FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;
    private DatabaseReference reference;

    private GoogleMap mMap;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 12000;

    static int PERMISSION_CODE = 639;

    String ambu_contact_text = "";
    String ambu_contact = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        init();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        login_type = getIntent().getStringExtra("logintype");

        if (login_type.equals("googleaccount")) {
            googleAccount();
        }

        proceed_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToHome();
            }
        });

        reference = FirebaseDatabase.getInstance().getReference().child("AmbulanceLocation");

        fetchAllAmbulanceData();
    }

    private void init() {
        proceed_button = findViewById(R.id.buttonProceed);

        mAuth = FirebaseAuth.getInstance();
    }

    private void googleAccount() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userid = firebaseUser.getUid();
        String fullname = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();

        User user = new User(email, fullname);
        FirebaseDatabase.getInstance().getReference("Users").child(userid).setValue(user);
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("logintype", login_type);
        startActivity(intent);
    }

    private void back() {
        showDialogMessage();
    }

    @Override
    public void onBackPressed() {
        showDialogMessage();
    }

    private void showDialogMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);

        builder.setMessage("Do you really want to logout?");

        builder.setTitle("Logout?");

        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            if (login_type.equals("googleaccount")) {
                googleSignInClient = GoogleSignIn.getClient(MapActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

                googleSignInClient.signOut();
            } else {
                FirebaseAuth.getInstance().signOut();
            }
            finish();
        });

        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void fetchAllAmbulanceData() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> ambulanceID = snapshot.getChildren();

                mMap.clear();
                for (DataSnapshot ambulance : ambulanceID) {
                    if (ambulance.child("status").getValue(String.class).equals("online") && ambulance.child("location").child("latitude").getValue(Double.class) != null) {
                        String contact1 = ambulance.child("contact").getValue(String.class);

                        Double latitude1 = ambulance.child("location").child("latitude").getValue(Double.class);
                        Double longitude1 = ambulance.child("location").child("longitude").getValue(Double.class);

                        LatLng location1 = new LatLng(latitude1, longitude1);

                        mMap.addMarker(new MarkerOptions().position(location1).title("MIERS Ambulance")
                                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_baseline_directions_car_24))).setTag(contact1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng umak = new LatLng(14.561855088627992, 121.05527872626675);
        mMap.setMinZoomPreference(11);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(umak));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                ambu_contact_text = marker.getTag().toString();
                ambu_contact = ("tel:" + marker.getTag());

                if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) MapActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
                } else {
                    showCallDialog();
                }

                return false;
            }
        });
    }

    private void showCallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("MIERS Ambulance");
        builder.setMessage("Do you have an emergency?\n" + "Contact Number: " + ambu_contact_text);

        builder.setCancelable(false);

        builder.setPositiveButton("Call", (DialogInterface.OnClickListener) (dialog, which) -> {
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse(ambu_contact));
            startActivity(i);
        });

        builder.setNegativeButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                fetchAllAmbulanceData();
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }
}
package com.umak.miersambulance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

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

public class AmbuMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private Switch serviceMode;

    private GoogleMap mMap;

    FirebaseUser user;
    private DatabaseReference reference, userReference;
    private LocationManager manager;

    Marker myMarker;

    private final int MIN_TIME = 3000;
    private final float MIN_DISTANCE = 0f;

    ProgressDialog locationFinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambu_map);

        serviceMode = findViewById(R.id.switchSerbisyo);
        locationFinding = new ProgressDialog(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("AmbulanceLocation").child(user.getUid());

        reference.child("status").setValue("offline");
        getAmbulanceContact();

        serviceMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    reference.child("status").setValue("online");
                } else {
                    reference.child("status").setValue("offline");
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        getLocationUpdates();
        readChanges();
    }

    private void getAmbulanceContact() {
        userReference = FirebaseDatabase.getInstance().getReference().child("AmbulanceUsers").child(user.getUid());
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String contact = snapshot.child("contact").getValue(String.class);
                reference.child("contact").setValue(contact);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readChanges() {
        reference.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if (location != null) {
                            locationFinding.dismiss();
                            myMarker.setVisible(true);
                            myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));

                            LatLng myloc = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
                        }
                    } catch (Exception e) {
                        Toast.makeText(AmbuMapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocationUpdates() {
        if (manager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else {
                    Toast.makeText(this, "No Provider Enabled", Toast.LENGTH_SHORT).show();
                }
                findLocationLoading();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationUpdates();
            } else {
                Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng osmak = new LatLng(14.54694935444354, 121.0618939848562);
        myMarker = mMap.addMarker(new MarkerOptions().position(osmak).title("MIERS Ambulance")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_baseline_directions_car_24)));
        mMap.setMinZoomPreference(12);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(osmak));
        myMarker.setVisible(false);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
            saveLocation(location);
        } else {
            Toast.makeText(getApplicationContext(), "No Location", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLocation(Location location) {
        reference.child("location").setValue(location);
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void findLocationLoading() {
        locationFinding.setTitle("Finding your location");
        locationFinding.setMessage("Please wait, make sure your GPS or Location is ON");
        locationFinding.setCanceledOnTouchOutside(false);
        locationFinding.show();
    }

    @Override
    protected void onDestroy() {
        reference.child("status").setValue("offline");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (serviceMode.isChecked()){
            Toast.makeText(this, "Turn off your service mode first!", Toast.LENGTH_SHORT).show();
        } else {
            reference.child("status").setValue("offline");
            System.exit(0);
            super.onBackPressed();
        }
    }
}
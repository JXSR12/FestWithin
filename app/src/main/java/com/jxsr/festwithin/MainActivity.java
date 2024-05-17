package com.jxsr.festwithin;

import static android.provider.CallLog.Locations.LATITUDE;
import static android.provider.CallLog.Locations.LONGITUDE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jxsr.festwithin.databinding.ActivityMainBinding;
import com.jxsr.festwithin.models.User;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
    MainMapView map;
    TicketsFragment myTickets;
    EventsFragment events;
    ImageView profileImg;
    ActivityMainBinding binding;
    DBHelper db;
    User user;

    private static final int FINE_LOCATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isLoggedIn = PreferenceData.getUserLoggedInStatus(getApplicationContext()) && PreferenceData.getLoggedInUserId(getApplicationContext()).length() > 0;
        db = new DBHelper(getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(getApplicationContext()));
        if(!isLoggedIn || user == null){
            Intent login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        }else{
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_PERMISSION_CODE);
            map = new MainMapView();
            myTickets = new TicketsFragment();
            events = new EventsFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mapFrame, map)
                    .commit();

            setupBottomMenu();

            String imageUri = user.getUserProfileImgSrc();

            profileImg = (ImageView) findViewById(R.id.profileImg);
            Picasso.get().load(imageUri).into(profileImg);

            profileImg.setOnClickListener(x->{
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });


        }
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }else {
            //Permission already granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == FINE_LOCATION_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Location Permission Granted", Toast.LENGTH_SHORT) .show();
            }else {
                Toast.makeText(MainActivity.this, "Location Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isLoggedIn = PreferenceData.getUserLoggedInStatus(getApplicationContext()) && PreferenceData.getLoggedInUserId(getApplicationContext()).length() > 0;
        if(!isLoggedIn){
            Intent login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        }
    }

    public void setupBottomMenu(){
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item ->{
            switch (item.getItemId()){
                case R.id.bnmNearby:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mapFrame, map)
                            .commit();
                    return true;
                case R.id.bnmPopular:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mapFrame, events)
                            .commit();
                    return true;
                case R.id.bnmProfile:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mapFrame, myTickets)
                            .commit();
                    return true;
                case R.id.bnmProfileR:
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                default:
                    return super.onOptionsItemSelected(item);
            }
        });
    }
}
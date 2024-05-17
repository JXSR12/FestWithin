package com.jxsr.festwithin;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.imageview.ShapeableImageView;
import com.jxsr.festwithin.models.User;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

public class ProfileActivity extends AppCompatActivity {
    ShapeableImageView profileImg;
//    ImageView ivEditName;
    EditText etDisplayName;
    LinearLayout organizerToggle;
    FrameLayout miniFrag;
    TextView tvEmail, tvPhone, tvGender, tvDOB, tvSettingMaxRange, tvSettingTimeZone, orgStatus, orgStatusLink;
    DBHelper db;
    User user;
    Button logOutBtn, setTimeZone, setMaxRange, changePw;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DBHelper(getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(getApplicationContext()));

//        ivEditName = findViewById(R.id.imageViewEditNameIcon);
        etDisplayName = findViewById(R.id.editTextTextPersonName);
        profileImg = findViewById(R.id.profileImgDetail);
        tvEmail = findViewById(R.id.textEmail);
        tvDOB = findViewById(R.id.userDOB);
        tvPhone = findViewById(R.id.userPhone);
        tvGender = findViewById(R.id.userGender);

        orgStatus = findViewById(R.id.textOrganizerStatus);
        orgStatusLink = findViewById(R.id.textOrganizerClick);
        organizerToggle = findViewById(R.id.organizerToggle);

        changePw = findViewById(R.id.setting_changePw);
        logOutBtn = findViewById(R.id.buttonLogOut);
        setMaxRange = findViewById(R.id.setting_MaxRangeChange);
        setTimeZone = findViewById(R.id.setting_TimeZoneChange);
        tvSettingMaxRange = findViewById(R.id.setting_MaxRangeText);
        tvSettingTimeZone = findViewById(R.id.setting_TimeZoneText);

        miniFrag = findViewById(R.id.walletMiniFragmentFl);

        changePw.setOnClickListener(x->{
            EditText oldPass = new EditText(this);
            oldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            EditText newPass = new EditText(this);
            newPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            EditText cNewPass = new EditText(this);
            cNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            AlertDialog.Builder a = new AlertDialog.Builder(this);
            a.setTitle("Change Password");
            a.setMessage("Enter your old password");
            if(oldPass.getParent() != null){
                ((ViewGroup)oldPass.getParent()).removeView(oldPass);
            }
            a.setView(oldPass);
            a.setCancelable(true);
            a.setPositiveButton("Next", (arg0, arg1) -> {
                if(oldPass.getText().toString().equals(user.getUserPassword())){
                    a.setMessage("Enter your new password");
                    if(newPass.getParent() != null){
                        ((ViewGroup)newPass.getParent()).removeView(newPass);
                    }
                    a.setView(newPass);
                    a.setPositiveButton("Next", (arg2, arg3) -> {
                        if(newPass.getText().toString().length() >= 6){
                            a.setMessage("Confirm your new password");
                            if(cNewPass.getParent() != null){
                                ((ViewGroup)cNewPass.getParent()).removeView(cNewPass);
                            }
                            a.setView(cNewPass);
                            a.setPositiveButton("Next", (arg4, arg5) -> {
                                if(cNewPass.getText().toString().equals(newPass.getText().toString())){
                                    db.updateUserPassword(cNewPass.getText().toString(), user.getUserID());
                                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    user = db.getUserFromID(user.getUserID());
                                }else{
                                    Toast.makeText(this, "New password doesn't match", Toast.LENGTH_SHORT).show();
                                }
                            });
                            a.show();
                        }else{
                            Toast.makeText(this, "Password must be >= 6 chars", Toast.LENGTH_SHORT).show();
                        }
                    });
                    a.show();
                }else{
                    Toast.makeText(this, "Invalid old password", Toast.LENGTH_SHORT).show();
                }

            });
            a.setNegativeButton("Cancel", (arg0, arg1) -> {});
            a.show();
        });

        if(user.isUserIsOrganizer()){
            orgStatus.setText("Organizer");
            orgStatusLink.setText("Go to dashboard");
            organizerToggle.setOnClickListener(x->{
                Intent org = new Intent(ProfileActivity.this, OrganizerMainActivity.class);
                startActivity(org);
            });
        }else{
            orgStatus.setText("Non Organizer");
            orgStatusLink.setText("Become an organizer");
            organizerToggle.setOnClickListener(x->{
                if(db.getUserWallet(user.getUserID()) != null){
                    EditText orgNameEditor = new EditText(this);
                    AlertDialog.Builder a = new AlertDialog.Builder(this);
                    a.setTitle("Enter your name as Organizer");
                    a.setMessage("Leave it blank to use your user name");
                    if(orgNameEditor.getParent() != null){
                        ((ViewGroup)orgNameEditor.getParent()).removeView(orgNameEditor);
                    }
                    a.setView(orgNameEditor);
                    a.setCancelable(true);
                    a.setPositiveButton("Continue", (arg0, arg1)->{
                        String organizerName = "";
                        if(orgNameEditor.getText().toString().isEmpty()){
                            organizerName = user.getUserDisplayName();
                        }else{
                            organizerName = orgNameEditor.getText().toString();
                        }
                        db.makeUserOrganizer(user.getUserID(),organizerName);
                        user = db.getUserFromID(PreferenceData.getLoggedInUserId(getApplicationContext()));
                        orgStatus.setText("Organizer");
                        orgStatusLink.setText("Go to dashboard");
                        organizerToggle.setOnClickListener(y->{
                            Intent org = new Intent(ProfileActivity.this, OrganizerMainActivity.class);
                            startActivity(org);
                        });
                        Intent org = new Intent(ProfileActivity.this, OrganizerMainActivity.class);
                        startActivity(org);
                    });
                    a.setNegativeButton("Cancel", (arg2, arg3) ->{});
                    a.show();
                }else{
                    Toast.makeText(this, "You must setup a wallet first to be an organizer!", Toast.LENGTH_SHORT).show();
                }

            });
        }

        etDisplayName.setText(user.getUserDisplayName());
        Drawable originalDrawable = etDisplayName.getBackground();
        etDisplayName.setBackground(null);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy");
        tvDOB.setText(sdf.format(user.getUserDOB()*1000L));
        tvGender.setText(user.getUserGender());
        tvPhone.setText(user.getUserPhone());
        tvSettingMaxRange.setText(PreferenceData.getMaxRangeKM(getApplicationContext()) + " KM");
        tvSettingTimeZone.setText(PreferenceData.getUserTimezone(getApplicationContext()));
        tvEmail.setText(user.getUserEmail());

        String[] timeZones = {"GMT+0:00","GMT+01:00","GMT+02:00","GMT+03:00", "GMT+03:30", "GMT+04:00", "GMT+05:00", "GMT+06:00", "GMT+07:00", "GMT+08:00", "GMT+09:00", "GMT+09:30", "GMT+10:00", "GMT+11:00", "GMT+12:00", "GMT-11:00", "GMT-10:00", "GMT-09:00", "GMT-08:00", "GMT-07:00", "GMT-06:00", "GMT-05:00", "GMT-04:00", "GMT-3:30", "GMT-03:00", "GMT-01:00"};

        setTimeZone.setOnClickListener(x->{
            final String[] selectedTZ = {""};
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Select desired time zone");
            selectedTZ[0] = timeZones[0];
            b.setSingleChoiceItems(timeZones, 0, (dialog, sel) -> {
                dialog.dismiss();
                selectedTZ[0] = timeZones[sel];
            });
            b.setOnDismissListener(y->{
                PreferenceData.setUserTimezone(getApplicationContext(), selectedTZ[0]);
                tvSettingTimeZone.setText(PreferenceData.getUserTimezone(getApplicationContext()));
            });
            b.show();
        });

        setMaxRange.setOnClickListener(x->{
            final NumberPicker rangePicker = new NumberPicker(this);
            rangePicker.setMaxValue(5000);
            rangePicker.setMinValue(5);
            AlertDialog.Builder c = new AlertDialog.Builder(this);
            c.setTitle("Change max events range (KM)");
            c.setMessage("Pick the desired value");
            if(rangePicker.getParent() != null){
                ((ViewGroup)rangePicker.getParent()).removeView(rangePicker);
            }
            c.setView(rangePicker);
            rangePicker.setValue(PreferenceData.getMaxRangeKM(getApplicationContext()));
            c.setOnDismissListener(y->{
                PreferenceData.setMaxRangeKM(getApplicationContext(), rangePicker.getValue());
                tvSettingMaxRange.setText(PreferenceData.getMaxRangeKM(getApplicationContext()) + " KM");
            });
            c.show();
        });

        Picasso.get().load(user.getUserProfileImgSrc()).into(profileImg);


        etDisplayName.setOnFocusChangeListener((view, hasFocus) -> {
            if(!hasFocus){
                if(!etDisplayName.getText().toString().equals(user.getUserDisplayName())){
                    if(etDisplayName.getText().toString().length() < 4){
                        Toast.makeText(getApplicationContext(), "Display name must be at least 4 characters", Toast.LENGTH_SHORT).show();
                    }else{
                        db.changeUserDisplayName(user.getUserID(), etDisplayName.getText().toString());
                        user.setUserDisplayName(etDisplayName.getText().toString());
                        PreferenceData.setLoggedInUserUsername(getApplicationContext(), etDisplayName.getText().toString());
                        Toast.makeText(getApplicationContext(), "Successfully changed display name", Toast.LENGTH_SHORT).show();
                    }
                }
                etDisplayName.setText(user.getUserDisplayName());
                etDisplayName.setInputType(InputType.TYPE_NULL);
                etDisplayName.setTextColor(ContextCompat.getColor(getApplicationContext(), androidx.cardview.R.color.cardview_dark_background));
                etDisplayName.setBackground(null);
            }else{
                etDisplayName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                etDisplayName.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.logo_red));
                etDisplayName.setBackground(originalDrawable);
            }
        });



        logOutBtn.setOnClickListener(x->{
            PreferenceData.clearLoggedInUser(getApplicationContext());
            finish();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.walletMiniFragmentFl, new FestWalletMiniFragment())
                .commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
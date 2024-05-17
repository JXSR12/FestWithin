package com.jxsr.festwithin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    EditText inputEmail, inputPassword, inputPhone;
    TextInputLayout tilEmail, tilPassword, tilPhone;
    RadioGroup selectLoginMethod;
    RadioButton selectPhone, selectEmail;
    TextView goToRegister;
    Button loginBtn;
    ImageView closeBtn;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DBHelper(getApplicationContext());

        inputEmail = findViewById(R.id.editTextEmail);
        inputPhone = findViewById(R.id.editTextPhone);
        inputPassword = findViewById(R.id.editTextPassword);

        tilEmail = findViewById(R.id.textInputEmail);
        tilPhone = findViewById(R.id.textInputPhone);
        tilPassword = findViewById(R.id.textInputPassword);

        selectLoginMethod = findViewById(R.id.radioGroupLoginMethod);
        selectPhone = findViewById(R.id.radioBtnPhone);
        selectEmail = findViewById(R.id.radioBtnEmail);

        goToRegister = findViewById(R.id.loginGoToRegisterText);
        loginBtn = findViewById(R.id.cirLoginButton);

        closeBtn = findViewById(R.id.loginClose);

        selectEmail.setChecked(true);
        inputPhone.setVisibility(View.GONE);
        tilPhone.setVisibility(View.GONE);

        selectLoginMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                View radioButton = selectLoginMethod.findViewById(checkedId);
                int index = selectLoginMethod.indexOfChild(radioButton);
                switch (index) {
                    case 0:
                        selectPhone.setChecked(true);
                        inputPhone.setVisibility(View.VISIBLE);
                        tilPhone.setVisibility(View.VISIBLE);
                        inputEmail.setVisibility(View.GONE);
                        tilEmail.setVisibility(View.GONE);
                        break;
                    case 1:
                        selectEmail.setChecked(true);
                        inputEmail.setVisibility(View.VISIBLE);
                        tilEmail.setVisibility(View.VISIBLE);
                        inputPhone.setVisibility(View.GONE);
                        tilPhone.setVisibility(View.GONE);
                        break;
                }
            }
        });

        goToRegister.setOnClickListener(x->{
            Intent register = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(register);
            finish();
        });

        closeBtn.setOnClickListener(x->{
            onBackPressed();
        });

        final boolean[] usingPhone = {false};
        final String[] inputEmailStr = {""};
        final String[] inputPhoneStr = {""};
        final String[] inputPassStr = {""};

        loginBtn.setOnClickListener(x->{
            inputEmailStr[0] = inputEmail.getText().toString();
            inputPassStr[0] = inputPassword.getText().toString();
            inputPhoneStr[0] = inputPhone.getText().toString();

            usingPhone[0] = selectPhone.isChecked();
            if(usingPhone[0]){
                if(inputPhoneStr[0].isEmpty() || inputPassStr[0].isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                }else{
                    loginWithPhone(inputPhoneStr[0], inputPassStr[0]);
                }
            }else{
                if(inputEmailStr[0].isEmpty() || inputPassStr[0].isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                }else {
                    loginWithEmail(inputEmailStr[0], inputPassStr[0]);
                }
            }

        });
    }

    public void loginWithPhone(String phone, String password){
        if(db.validateCredentialsPhone(phone,password)){
            PreferenceData.setUserLoggedInStatus(getApplicationContext(), true);
            PreferenceData.setLoggedInUserUsername(getApplicationContext(), db.getUserDisplayName(db.getUserIDFromPhone(phone)));
            PreferenceData.setUserLoggedInEmail(getApplicationContext(), db.getUserEmail(db.getUserIDFromPhone(phone)));
            PreferenceData.setLoggedInUserId(getApplicationContext(), db.getUserIDFromPhone(phone));
            loadMain();
        }else{
            Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }

    public void loginWithEmail(String email, String password){
        if(db.validateCredentialsEmail(email,password)){
            PreferenceData.setUserLoggedInStatus(getApplicationContext(), true);
            PreferenceData.setLoggedInUserUsername(getApplicationContext(), db.getUserDisplayName(db.getUserIDFromEmail(email)));
            PreferenceData.setUserLoggedInEmail(getApplicationContext(), email);
            PreferenceData.setLoggedInUserId(getApplicationContext(), db.getUserIDFromEmail(email));
            loadMain();
        }else{
            Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadMain(){
        Intent main = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(main);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
package com.jxsr.festwithin;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jxsr.festwithin.models.User;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private final long YEAR_IN_SECOND = 31556952;

    EditText regPhone, regEmail, regName, regPassword, regConfPassword;
    RadioGroup selectGender;
    RadioButton selectMale, selectFemale, selectUns;
    Button changeDOB, registerBtn;
    TextView DOBLabel, goToLogin;
    ImageView regClose;
    DatePickerDialog DOBPicker;
    DBHelper db;

    SimpleDateFormat sdf;
    Date DOB;
    String dobLabelStr;

    int yearDOB = 0, monthDOB = 0, dayDOB = 0;
    long unixDOB = 0;
    int age = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regClose = findViewById(R.id.registerClose);
        regPhone = findViewById(R.id.editTextRegPhone);
        regEmail = findViewById(R.id.editTextRegEmail);
        regName = findViewById(R.id.editTextRegName);
        regPassword = findViewById(R.id.editTextRegPassword);
        regConfPassword = findViewById(R.id.editTextRegConfPassword);
        selectGender = findViewById(R.id.radioGroupRegGender);
        selectMale = findViewById(R.id.radioBtnMale);
        selectFemale = findViewById(R.id.radioBtnFemale);
        selectUns = findViewById(R.id.radioBtnUnspecified);
        changeDOB = findViewById(R.id.btnChangeDob);
        registerBtn = findViewById(R.id.cirRegisterButton);
        DOBLabel = findViewById(R.id.labelDob);
        goToLogin = findViewById(R.id.registerGoToLoginText);

        db = new DBHelper(getApplicationContext());

        DOBPicker = new DatePickerDialog(this);

        sdf = new SimpleDateFormat("dd MMMM yyyy");

        DOB = new GregorianCalendar(DOBPicker.getDatePicker().getYear(), DOBPicker.getDatePicker().getMonth(), DOBPicker.getDatePicker().getDayOfMonth()).getTime();
        DOBLabel.setText(sdf.format(DOB));

        DOBPicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                yearDOB = year;
                monthDOB = month;
                dayDOB = dayOfMonth;

                Instant temp = Instant.parse(""+String.format("%04d",year)+"-"+String.format("%02d",month)+"-"+String.format("%02d",dayOfMonth)+"T01:00:00Z");
                unixDOB = temp.getEpochSecond();

                DOB = new GregorianCalendar(DOBPicker.getDatePicker().getYear(), DOBPicker.getDatePicker().getMonth(), DOBPicker.getDatePicker().getDayOfMonth()).getTime();
                age = (int)((Instant.now().getEpochSecond() - unixDOB)/YEAR_IN_SECOND);
                dobLabelStr = sdf.format(DOB) + " (aged "+ age + ")";
                DOBLabel.setText(dobLabelStr);
            }
        });

        changeDOB.setOnClickListener(x->{
            DOBPicker.show();
        });

        registerBtn.setOnClickListener(x->{
            if(validateInputs()){
                User u = new User(
                        String.format("U%04d", db.getUsersCount()+1),
                        regName.getText().toString(),
                        regEmail.getText().toString(),
                        regPhone.getText().toString(),
                        selectMale.isChecked() ? "Male" : selectFemale.isChecked() ? "Female" : "Unspecified",
                        regPassword.getText().toString(),
                        unixDOB,
                        "https://www.kindpng.com/picc/m/451-4517876_default-profile-hd-png-download.png",
                        false,
                        regName.getText().toString()
                );
                if(db.insertUser(u)){
                    loadLogin();
                    Toast.makeText(getApplicationContext(), "Successfully registered, please sign in", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Registration failed, please try again later", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Please fill all fields correctly, tap (?) icon for more info", Toast.LENGTH_SHORT).show();
            }
        });

        regClose.setOnClickListener(x->{
            onBackPressed();
        });

        goToLogin.setOnClickListener(x->{
            loadLogin();
        });
    }

    public boolean isValidEmail(String email){
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return email.matches(regexPattern);
    }

    public boolean validateInputs(){
        if(!db.validateUnique(regEmail.getText().toString(), regPhone.getText().toString())){
            Toast.makeText(this, "Email or phone number is already in use!", Toast.LENGTH_SHORT).show();
        }
        if(age < 13){
            Toast.makeText(this, "You must be at least 13 years old", Toast.LENGTH_SHORT).show();
        }
        if(regName.getText().toString().length() < 4){
            Toast.makeText(this, "Name must be > 3 chars", Toast.LENGTH_SHORT).show();
        }
        if(regPassword.getText().toString().length() < 6){
            Toast.makeText(this, "Password must be > 5 chars", Toast.LENGTH_SHORT).show();
        }
        if(regPhone.getText().toString().length() < 7){
            Toast.makeText(this, "Phone must be > 6 chars", Toast.LENGTH_SHORT).show();
        }
        if(!isValidEmail(regEmail.getText().toString())){
            Toast.makeText(this, "Email must be valid!", Toast.LENGTH_SHORT).show();
        }

        return !(regPhone.getText().toString().length() < 7)
                && isValidEmail(regEmail.getText().toString())
                && db.validateUnique(regEmail.getText().toString(), regPhone.getText().toString())
                && !(regName.getText().toString().length() < 4)
                && !(regPassword.getText().toString().length() < 6)
                && regPassword.getText().toString().equals(regConfPassword.getText().toString())
                && selectGender.getCheckedRadioButtonId() != -1
                && age >= 13
                ;
    }

    public void loadLogin(){
        Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(login);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
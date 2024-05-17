package com.jxsr.festwithin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.jxsr.festwithin.models.Ticket;
import com.jxsr.festwithin.models.User;

public class TicketDetailActivity extends AppCompatActivity {

    ImageView qrCode;
    TextView eventTitle, className, identifier;
    Button invalidateBtn;
    DBHelper db;
    User user;
    Ticket ticket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);
        db = new DBHelper(getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(getApplicationContext()));
        ticket = db.getTicketFromID((String)getIntent().getSerializableExtra("ticketId"));

        qrCode = findViewById(R.id.td_qrCode);
        eventTitle = findViewById(R.id.td_eventtitle);
        className = findViewById(R.id.td_classname);
        identifier = findViewById(R.id.td_identifier);
        invalidateBtn = findViewById(R.id.invalidateBtn);

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(ticket.getTicketUniqueToken(), BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ((ImageView) findViewById(R.id.td_qrCode)).setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }

        eventTitle.setText(db.getEventFromID(ticket.getEventID()).getEventTitle());
        className.setText(ticket.getEventPricingClassName());
        identifier.setText(ticket.getTicketUniqueToken().substring(ticket.getTicketUniqueToken().length()-7));

        if(ticket.getTicketUniqueToken().equals("Invalid/Expired")){
            invalidateBtn.setText(ticket.getTicketUniqueToken());
            invalidateBtn.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.logo_red), PorterDuff.Mode.MULTIPLY);
            invalidateBtn.setTextColor(Color.WHITE);
            invalidateBtn.setClickable(false);
            invalidateBtn.setEnabled(false);
        }else{
            invalidateBtn.setOnClickListener(x->{
                EditText orgNameEditor = new EditText(this);
                orgNameEditor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                AlertDialog.Builder a = new AlertDialog.Builder(this);
                a.setTitle("Manual Ticket Invalidation");
                a.setMessage("Please enter organizer password to continue");
                if(orgNameEditor.getParent() != null){
                    ((ViewGroup)orgNameEditor.getParent()).removeView(orgNameEditor);
                }
                a.setView(orgNameEditor);
                a.setOnDismissListener(y->{
                    String entered = orgNameEditor.getText().toString();
                    if(entered.equals(db.getUserFromID(db.getEventFromID(ticket.getEventID()).getEventOrganizerUserID()).getUserPassword())){
                        ticket.invalidate(db);
                        Toast.makeText(this, "SUCCESS MTI: ID " + ticket.getTicketID() + " UID " + ticket.getTicketUniqueToken(), Toast.LENGTH_SHORT).show();
                        ticket = db.getTicketFromID((String)getIntent().getSerializableExtra("ticketId"));
                        if(ticket.getTicketUniqueToken().equals("Invalid/Expired")){
                            invalidateBtn.setText(ticket.getTicketUniqueToken());
                            invalidateBtn.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.logo_red), PorterDuff.Mode.MULTIPLY);
                            invalidateBtn.setTextColor(Color.WHITE);
                            invalidateBtn.setClickable(false);
                            invalidateBtn.setEnabled(false);
                            Toast.makeText(this, "Ticket is expired", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(this, "Invalid organizer password!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "If you are unfamiliar with MTI, do not attempt to do so", Toast.LENGTH_SHORT).show();
                    }
                });
                a.show();
            });
        }
    }
}
package com.jxsr.festwithin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jxsr.festwithin.models.User;
import com.jxsr.festwithin.models.Wallet;
import com.jxsr.festwithin.models.WalletTransaction;
import com.jxsr.festwithin.models.enums.TransactionStatus;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.TimeZone;

public class FestWalletActivity extends AppCompatActivity {
    TextView currency, balance, holderName, cardNumber, currencyFull;
    Button showFulLNumber, changeCurrency;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");

    TextView toggleInbound, toggleOutbound;

    DBHelper db;
    User user;
    Wallet userWallet;
    DecimalFormat df;

    String SHOW_TRANSACTIONS_MODE = "INBOUND";

    LinearLayout transactionsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fest_wallet);
        df = new DecimalFormat("#.##");
        db = new DBHelper(getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(getApplicationContext()));
        userWallet = db.getUserWallet(user.getUserID());

        transactionsHolder = findViewById(R.id.transactionListHolder);
        updateTransactions();

        currency = findViewById(R.id.currencyText);
        currencyFull = findViewById(R.id.currencyFullName);
        balance = findViewById(R.id.balanceText);
        holderName = findViewById(R.id.walletHolderName);
        cardNumber = findViewById(R.id.censoredWalletNumber);

        toggleInbound = findViewById(R.id.tvToggleInbound);
        toggleOutbound = findViewById(R.id.tvToggleOutbound);

        showFulLNumber = findViewById(R.id.buttonShowFullNumber);
        changeCurrency = findViewById(R.id.changeCurrencyBtn);

        currency.setText(userWallet.getWalletCurrency());
        currencyFull.setText(db.getCurrencyFullName(userWallet.getWalletCurrency()));

        sdf.setTimeZone(TimeZone.getTimeZone(PreferenceData.getUserTimezone(getApplicationContext())));

        cardNumber.setText("**** **** " + userWallet.getWalletPhone().substring(userWallet.getWalletPhone().length()-4));
        df.setRoundingMode(RoundingMode.FLOOR);
        balance.setText(""+df.format(userWallet.getBalance()));
        holderName.setText(db.getUserDisplayName(userWallet.getUserID()));

        int togglerColor = ((ColorDrawable)toggleInbound.getBackground()).getColor();
        toggleOutbound.setBackgroundColor(Color.LTGRAY);
        toggleInbound.setBackgroundColor(togglerColor);

        toggleInbound.setOnClickListener(x->{
            if(!SHOW_TRANSACTIONS_MODE.equals("INBOUND")){
                SHOW_TRANSACTIONS_MODE = "INBOUND";
                toggleOutbound.setBackgroundColor(Color.LTGRAY);
                toggleInbound.setBackgroundColor(togglerColor);
                updateTransactions();
            }
        });

        toggleOutbound.setOnClickListener(x->{
            if(!SHOW_TRANSACTIONS_MODE.equals("OUTBOUND")){
                SHOW_TRANSACTIONS_MODE = "OUTBOUND";
                toggleInbound.setBackgroundColor(Color.LTGRAY);
                toggleOutbound.setBackgroundColor(togglerColor);
                updateTransactions();
            }
        });

        showFulLNumber.setOnClickListener(x->{
            if(!showFulLNumber.getText().toString().equals("Hide")){
                cardNumber.setText(userWallet.getWalletPhone());
                showFulLNumber.setText(R.string.hide);
            }else{
                cardNumber.setText("**** **** " + userWallet.getWalletPhone().substring(userWallet.getWalletPhone().length()-4));
                showFulLNumber.setText(R.string.show);
            }
        });

        changeCurrency.setOnClickListener(x->{
            changeCurrency(user);
        });
    }

    public void changeCurrency(User user){
        final String[] selectedCurrency = {""};
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Change wallet currency");
        String[] items = db.getAvailableCurrenciesFullName();
        selectedCurrency[0] = items[0];
        b.setSingleChoiceItems(items, 0, (dialog, sel) -> {
            dialog.dismiss();
            selectedCurrency[0] = items[sel];
        });
        b.setOnDismissListener(x->{
            selectedCurrency[0] = selectedCurrency[0].substring(0,3);
            db.changeWalletCurrency(selectedCurrency[0], db.getUserWallet(user.getUserID()).getWalletID());
            userWallet = db.getUserWallet(user.getUserID());
            currency.setText(userWallet.getWalletCurrency());
            currencyFull.setText(db.getCurrencyFullName(userWallet.getWalletCurrency()));
            balance.setText(""+df.format(userWallet.getBalance()));
            updateTransactions();
        });
        b.show();
    }

    public void updateTransactions(){
        if(SHOW_TRANSACTIONS_MODE.equals("INBOUND")){
            addAllInboundTransactions();
        }else{
            addAllOutboundTransactions();
        }
    }

    public void addAllInboundTransactions(){
        transactionsHolder.removeAllViews();
        ArrayList<WalletTransaction> inboundTrans = db.getInboundTransactionsFromWalletID(userWallet.getWalletID());
        if(inboundTrans.size() == 0){
            TextView noTrans = new TextView(this);
            noTrans.setGravity(Gravity.CENTER);
            noTrans.setPadding(10,10,10,10);
            noTrans.setTextColor(Color.DKGRAY);
            noTrans.setText(R.string.no_transaction);
            noTrans.setTextSize(24);
            transactionsHolder.addView(noTrans);
        }else{
            for(WalletTransaction wt : inboundTrans){
                CardView transItemCard = new CardView(this);
                View.inflate(getApplicationContext(), R.layout.transaction_item, transItemCard);
                ((TextView)transItemCard.findViewById(R.id.trans_datetime)).setText(sdf.format(wt.getTransactionUnixSeconds()*1000L));
                ((TextView)transItemCard.findViewById(R.id.dest_holdername)).setText("from " + db.getUserDisplayName(db.getWalletFromID(wt.getTransactionOriginWalletID()).getUserID()));
                double transAmt = wt.getTransactionAmount();
                if(!wt.getTransactionOriginWalletID().equals("W0000")){
                    transAmt*=0.85;
                }
                ((TextView)transItemCard.findViewById(R.id.trans_amount)).setText(wt.getTransactionCurrency().equals(userWallet.getWalletCurrency()) ? ""+df.format(transAmt) : ""+df.format(db.convertCurrency(transAmt,wt.getTransactionCurrency(),userWallet.getWalletCurrency())));
                ((TextView)transItemCard.findViewById(R.id.trans_direction)).setText(wt.getTransactionStatus() == TransactionStatus.PAID ? "PAID" : wt.getTransactionStatus() == TransactionStatus.CANCELLED ? "CANCELLED" : "CANCEL");
                if(wt.getTransactionStatus() == TransactionStatus.UNPAID){
                    transItemCard.findViewById(R.id.trans_direction).setOnClickListener(x->{
                        AlertDialog.Builder cancel = new AlertDialog.Builder(this);
                        cancel.setTitle("Cancel Transaction");
                        cancel.setMessage("Do you want to cancel this unpaid transaction?");
                        cancel.setIcon(R.drawable.ic_baseline_payment_24);
                        cancel.setCancelable(true);
                        cancel.setPositiveButton("Yes", (arg0, arg1) -> {
                            wt.cancel(db);
                            Toast.makeText(FestWalletActivity.this, "Transaction cancelled", Toast.LENGTH_SHORT).show();
                            updateTransactions();
                        });
                        cancel.setNegativeButton("No", (arg0, arg1) -> Toast.makeText(FestWalletActivity.this, "Transaction cancellation aborted", Toast.LENGTH_SHORT).show());
                        cancel.show();
                    });
                }

                ((TextView)transItemCard.findViewById(R.id.trans_direction)).setTextColor(Color.WHITE);
                transItemCard.setBackgroundColor(wt.getTransactionStatus() == TransactionStatus.PAID ? Color.argb(255,147,184,138) : wt.getTransactionStatus() == TransactionStatus.CANCELLED ? Color.argb(255,207,100,100): Color.argb(255,151,151,151));
                transItemCard.setOnClickListener(x->{
                    Toast.makeText(this, "Transaction Status - "+wt.getTransactionStatus(), Toast.LENGTH_SHORT).show();
                });
                transactionsHolder.addView(transItemCard);

            }
        }

    }

    public void addAllOutboundTransactions(){
        transactionsHolder.removeAllViews();
        ArrayList<WalletTransaction> outboundTrans = db.getOutboundTransactionsFromWalletID(userWallet.getWalletID());
        if(outboundTrans.size() == 0){
            TextView noTrans = new TextView(this);
            noTrans.setGravity(Gravity.CENTER);
            noTrans.setPadding(10,10,10,10);
            noTrans.setTextColor(Color.DKGRAY);
            noTrans.setText(R.string.no_transaction);
            noTrans.setTextSize(24);
            transactionsHolder.addView(noTrans);
        }else{
            for(WalletTransaction wt : outboundTrans){
                CardView transItemCard = new CardView(this);
                View.inflate(getApplicationContext(), R.layout.transaction_item, transItemCard);
                ((TextView)transItemCard.findViewById(R.id.trans_datetime)).setText(sdf.format(wt.getTransactionUnixSeconds()*1000L));
                ((TextView)transItemCard.findViewById(R.id.dest_holdername)).setText("to " + db.getUserDisplayName(db.getWalletFromID(wt.getTransactionDestinationWalletID()).getUserID()));
                ((TextView)transItemCard.findViewById(R.id.trans_amount)).setText(wt.getTransactionCurrency().equals(userWallet.getWalletCurrency()) ? ""+df.format(wt.getTransactionAmount()) : ""+df.format(db.convertCurrency(wt.getTransactionAmount(),wt.getTransactionCurrency(),userWallet.getWalletCurrency())));
                ((TextView)transItemCard.findViewById(R.id.trans_direction)).setText(wt.getTransactionStatus() == TransactionStatus.PAID ? "PAID" : wt.getTransactionStatus() == TransactionStatus.CANCELLED ? "CANCELLED" : "CANCEL");
                if(wt.getTransactionStatus() == TransactionStatus.UNPAID){
                    transItemCard.findViewById(R.id.trans_direction).setOnClickListener(x->{
                        AlertDialog.Builder cancel = new AlertDialog.Builder(this);
                        cancel.setTitle("Cancel Transaction");
                        cancel.setMessage("Do you want to cancel this unpaid transaction?");
                        cancel.setIcon(R.drawable.ic_baseline_payment_24);
                        cancel.setCancelable(true);
                        cancel.setPositiveButton("Yes", (arg0, arg1) -> {
                            wt.cancel(db);
                            Toast.makeText(FestWalletActivity.this, "Transaction cancelled", Toast.LENGTH_SHORT).show();
                            updateTransactions();
                        });
                        cancel.setNegativeButton("No", (arg0, arg1) -> Toast.makeText(FestWalletActivity.this, "Transaction cancellation aborted", Toast.LENGTH_SHORT).show());
                        cancel.show();
                    });
                }

                ((TextView)transItemCard.findViewById(R.id.trans_direction)).setTextColor(Color.WHITE);
                transItemCard.setBackgroundColor(wt.getTransactionStatus() == TransactionStatus.PAID ? Color.argb(255,147,184,138) : wt.getTransactionStatus() == TransactionStatus.CANCELLED ? Color.argb(255,207,100,100): Color.argb(255,151,151,151));
                transItemCard.setOnClickListener(x->{
                    Toast.makeText(this, "Transaction Status - "+wt.getTransactionStatus(), Toast.LENGTH_SHORT).show();
                });
                transactionsHolder.addView(transItemCard);
            }
        }
    }
}
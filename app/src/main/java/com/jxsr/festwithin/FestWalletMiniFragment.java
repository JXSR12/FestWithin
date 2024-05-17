package com.jxsr.festwithin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jxsr.festwithin.models.User;
import com.jxsr.festwithin.models.Wallet;
import com.jxsr.festwithin.models.enums.TransactionStatus;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;

public class FestWalletMiniFragment extends Fragment {
    TextView currency, balance, upperLabel;
    Button goToWallet;

    DBHelper db;
    User user;
    Wallet userWallet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fest_wallet_mini, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DBHelper(requireActivity().getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(requireActivity().getApplicationContext()));
        userWallet = db.getUserWallet(user.getUserID());

        upperLabel = view.findViewById(R.id.miniUpperLabel);
        currency = view.findViewById(R.id.miniCurrency);
        balance = view.findViewById(R.id.miniBalance);
        goToWallet = view.findViewById(R.id.goToWalletBtn);

        updateViews();
    }

    public void updateViews(){
        if(userWallet == null){
            currency.setVisibility(View.GONE);
            balance.setGravity(Gravity.CENTER);
            balance.setText(R.string.pay_faster_festwallet);
            upperLabel.setText(R.string.no_wallet);
            goToWallet.setText(R.string.setup_wallet);

            goToWallet.setOnClickListener(x->setupWallet(user));
        }else{
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.FLOOR);
            upperLabel.setText("Your wallet balance");
            currency.setText(userWallet.getWalletCurrency());
            currency.setVisibility(View.VISIBLE);
            goToWallet.setText("Go to My Wallet");
            balance.setGravity(Gravity.LEFT);

            balance.setText("" + df.format(userWallet.getBalance()));
            goToWallet.setOnClickListener(x->{
                Intent wallet = new Intent(requireActivity().getApplicationContext(), FestWalletActivity.class);
                startActivity(wallet);
            });
        }
    }

    public void setupWallet(User user){
        final String[] selectedCurrency = {""};
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        b.setTitle("Select wallet currency");
        String[] items = db.getAvailableCurrenciesFullName();
        selectedCurrency[0] = items[0];
        b.setSingleChoiceItems(items, 0, (dialog, sel) -> {
            dialog.dismiss();
            selectedCurrency[0] = items[sel];
        });
        b.setOnDismissListener(x->{
            selectedCurrency[0] = selectedCurrency[0].substring(0,3);
            db.addWallet(String.format("W%04d", db.getWalletsCount()+1),
                    user.getUserPhone(),
                    user.getUserID(),
                    user.getUserPassword(),
                    true,
                    selectedCurrency[0],
                    db.convertCurrency(20,"USD",selectedCurrency[0]));
            ;
            String newTransID = String.format("T%04d", db.getTransactionsCount()+1);
            userWallet = db.getUserWallet(user.getUserID());
            db.addTransaction(newTransID,
                    "W0000",
                    userWallet.getWalletID(),
                    Instant.now().getEpochSecond(),
                    userWallet.getWalletCurrency(),
                    db.convertCurrency(20,"USD",userWallet.getWalletCurrency()),
                    TransactionStatus.PAID);

            //All users are given initial balance of 20 USD equivalent (So users can test the app)
            //The initial balance is given from the App Wallet (W0000) just to indicate a payment is made)

            if(userWallet == null){
                Toast.makeText(requireActivity().getApplicationContext(), "Wallet setup failed, try again later.", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(requireActivity().getApplicationContext(), "Wallet setup successful, received welcome bonus of USD20 equivalent", Toast.LENGTH_LONG).show();
            }
            updateViews();
        });
        b.show();
    }
}
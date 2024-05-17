package com.jxsr.festwithin.models;

import java.util.ArrayList;

public class Wallet {
    private String WalletID;
    private String WalletPhone;
    private String UserID;
    private String pin;
    private boolean active;
    private String WalletCurrency;
    private double balance;

    private ArrayList<WalletTransaction> walletTransactions;

    public Wallet(String walletID, String walletPhone, String userID, String pin, boolean active, String walletCurrency, double balance) {
        WalletID = walletID;
        WalletPhone = walletPhone;
        UserID = userID;
        this.pin = pin;
        this.active = active;
        WalletCurrency = walletCurrency;
        this.balance = balance;
        walletTransactions = new ArrayList<>();
    }

    public String getWalletID() {
        return WalletID;
    }

    public void setWalletID(String walletID) {
        WalletID = walletID;
    }

    public String getWalletPhone() {
        return WalletPhone;
    }

    public void setWalletPhone(String walletPhone) {
        WalletPhone = walletPhone;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getWalletCurrency() {
        return WalletCurrency;
    }

    public void setWalletCurrency(String walletCurrency) {
        WalletCurrency = walletCurrency;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public ArrayList<WalletTransaction> getWalletTransactions() {
        return walletTransactions;
    }

    public void setWalletTransactions(ArrayList<WalletTransaction> walletTransactions) {
        this.walletTransactions = walletTransactions;
    }
}

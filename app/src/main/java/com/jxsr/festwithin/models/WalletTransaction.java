package com.jxsr.festwithin.models;

import com.jxsr.festwithin.DBHelper;
import com.jxsr.festwithin.models.enums.TransactionStatus;

public class WalletTransaction {
    private String TransactionID;
    private String TransactionOriginWalletID;
    private String TransactionDestinationWalletID;
    private long TransactionUnixSeconds;
    private String TransactionCurrency;
    private double TransactionAmount;
    private TransactionStatus transactionStatus;

    public WalletTransaction(String transactionID, String transactionOriginWalletID, String transactionDestinationWalletID, long transactionUnixSeconds, String transactionCurrency, double transactionAmount, TransactionStatus transactionStatus) {
        TransactionID = transactionID;
        TransactionOriginWalletID = transactionOriginWalletID;
        TransactionDestinationWalletID = transactionDestinationWalletID;
        TransactionUnixSeconds = transactionUnixSeconds;
        TransactionCurrency = transactionCurrency;
        TransactionAmount = transactionAmount;
        this.transactionStatus = transactionStatus;
    }

    public int pay(DBHelper db){
        if(this.transactionStatus == TransactionStatus.UNPAID){
            if(db.getWalletBalance(TransactionOriginWalletID) >= db.convertCurrency(TransactionAmount,TransactionCurrency,db.getWalletFromID(TransactionOriginWalletID).getWalletCurrency())){
                db.updateWalletBalance(db.getWalletBalance(TransactionOriginWalletID)-TransactionAmount,TransactionOriginWalletID);
                db.updateWalletBalance(db.getWalletBalance(TransactionDestinationWalletID)+(TransactionAmount*0.85),TransactionDestinationWalletID);
                this.transactionStatus = TransactionStatus.PAID;
                db.updateTransactionStatus(TransactionStatus.PAID, this.TransactionID);
                return 0;
            }else{
                return -1;
            }
        }
        return -2;
    }

    public void cancel(DBHelper db){
        if(this.transactionStatus == TransactionStatus.UNPAID){
            this.transactionStatus = TransactionStatus.CANCELLED;
            db.updateTransactionStatus(TransactionStatus.CANCELLED, this.TransactionID);
        }
    }

    public String getTransactionID() {
        return TransactionID;
    }

    public void setTransactionID(String transactionID) {
        TransactionID = transactionID;
    }

    public String getTransactionOriginWalletID() {
        return TransactionOriginWalletID;
    }

    public void setTransactionOriginWalletID(String transactionOriginWalletID) {
        TransactionOriginWalletID = transactionOriginWalletID;
    }

    public String getTransactionDestinationWalletID() {
        return TransactionDestinationWalletID;
    }

    public void setTransactionDestinationWalletID(String transactionDestinationWalletID) {
        TransactionDestinationWalletID = transactionDestinationWalletID;
    }

    public long getTransactionUnixSeconds() {
        return TransactionUnixSeconds;
    }

    public void setTransactionUnixSeconds(long transactionUnixSeconds) {
        TransactionUnixSeconds = transactionUnixSeconds;
    }

    public String getTransactionCurrency() {
        return TransactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        TransactionCurrency = transactionCurrency;
    }

    public double getTransactionAmount() {
        return TransactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        TransactionAmount = transactionAmount;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}

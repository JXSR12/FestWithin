package com.jxsr.festwithin;
import static com.jxsr.festwithin.EventsFragment.latToKM;
import static com.jxsr.festwithin.EventsFragment.lngToKM;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.jxsr.festwithin.models.Event;
import com.jxsr.festwithin.models.EventPricing;
import com.jxsr.festwithin.models.Ticket;
import com.jxsr.festwithin.models.User;
import com.jxsr.festwithin.models.Wallet;
import com.jxsr.festwithin.models.WalletTransaction;
import com.jxsr.festwithin.models.enums.TransactionStatus;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class DBHelper extends SQLiteOpenHelper {
    public static final String USERS_DB = "FestWithin.db";
    public static final int DB_VERSION = 14;
    SQLiteDatabase db;

    private static final String DEFAULT_EVENT_BANNER = "https://i.ibb.co/VJF7vfs/EVENT.png";

    public DBHelper(@Nullable Context context ) {
        super(context, USERS_DB, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create users Table
        db.execSQL("CREATE TABLE IF NOT EXISTS users(" +
                "UserID TEXT PRIMARY KEY, " +
                "UserProfileImgSrc TEXT NOT NULL," +
                "UserEmail TEXT NOT NULL, " +
                "UserDisplayName TEXT NOT NULL, " +
                "UserPassword TEXT NOT NULL, " +
                "UserPhone TEXT NOT NULL, " +
                "UserGender TEXT NOT NULL, " +
                "UserDOB INTEGER NOT NULL, " +
                "UserIsOrganizer INTEGER NOT NULL," +
                "UserOrganizerName TEXT" +
                ")");

        //Create events Table
        db.execSQL("CREATE TABLE IF NOT EXISTS events(" +
                "EventID TEXT PRIMARY KEY, " +
                "EventOrganizerUserID TEXT NOT NULL, " +
                "EventTitle TEXT NOT NULL, " +
                "EventStartDate INTEGER NOT NULL, " +
                "EventDescription TEXT NOT NULL," +
                "EventLatitude REAL NOT NULL," +
                "EventLongitude REAL NOT NULL," +
                "EventBannerSrc TEXT," +
                "EventLocationDesc TEXT" +
                ")");

        //Create eventpricings Table
        db.execSQL("CREATE TABLE IF NOT EXISTS eventpricings(" +
                "EventID TEXT NOT NULL, " +
                "EPClassName TEXT NOT NULL," +
                "EPClassCurrency TEXT NOT NULL," +
                "EPClassPrice REAL NOT NULL," +
                "EPClassStock INTEGER NOT NULL," +
                "FOREIGN KEY(EventID) REFERENCES events(EventID) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")");

        //Create wallets Table
        db.execSQL("CREATE TABLE IF NOT EXISTS wallets(" +
                "WalletID TEXT PRIMARY KEY, " +
                "WalletPhone TEXT NOT NULL," +
                "WalletUserID TEXT NOT NULL," +
                "WalletPin TEXT NOT NULL," +
                "WalletIsActive INTEGER NOT NULL," +
                "WalletCurrency TEXT NOT NULL," +
                "WalletBalance REAL NOT NULL," +
                "FOREIGN KEY(WalletUserID) REFERENCES users(UserID) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")");

        //Create transactions Table
        db.execSQL("CREATE TABLE IF NOT EXISTS transactions(" +
                "TransactionID TEXT PRIMARY KEY, " +
                "TransactionOriginWalletID TEXT NOT NULL," +
                "TransactionDestinationWalletID TEXT NOT NULL," +
                "TransactionUnixSeconds INTEGER NOT NULL," +
                "TransactionCurrency TEXT NOT NULL," +
                "TransactionAmount REAL NOT NULL," +
                "TransactionStatus INTEGER NOT NULL," +
                "FOREIGN KEY(TransactionOriginWalletID) REFERENCES wallets(WalletID)," +
                "FOREIGN KEY(TransactionDestinationWalletID) REFERENCES wallets(WalletID)" +
                ")");

        //Create eventpricings Table
        db.execSQL("CREATE TABLE IF NOT EXISTS tickets(" +
                "TicketID TEXT PRIMARY KEY, " +
                "EventID TEXT NOT NULL," +
                "EventPricingClassName TEXT NOT NULL," +
                "TicketOwnerUserID TEXT NOT NULL," +
                "TicketTransactionID TEXT NOT NULL," +
                "TicketUniqueToken TEXT NOT NULL," +
                "FOREIGN KEY(EventID) REFERENCES events(EventID) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(TicketOwnerUserID) REFERENCES users(UserID)," +
                "FOREIGN KEY(TicketTransactionID) REFERENCES transactions(TransactionID)" +
                ")");

        //Create currencies Table
        db.execSQL("CREATE TABLE IF NOT EXISTS currencies(" +
                "CurrencyName TEXT PRIMARY KEY, " +
                "CurrencyUSDSingleValue REAL NOT NULL," +
                "CurrencyFullName TEXT NOT NULL" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS eventpricings");
        db.execSQL("DROP TABLE IF EXISTS wallets");
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS tickets");
        db.execSQL("DROP TABLE IF EXISTS currencies");
        onCreate(db);
    }

    public String getCurrencyFullName(String currencyShortname){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT CurrencyFullName FROM currencies WHERE (CurrencyName = ?)",
                new String[]{currencyShortname});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getString(0);
        }else{
            return "Currency not found";
        }
    }

    public double convertCurrency(double amount, String fromCurrencyName, String toCurrencyName){
        double fromCurrencyValueSU = getCurrencySingleUSDValue(fromCurrencyName);
        double toCurrencyValueSU = getCurrencySingleUSDValue(toCurrencyName);

        if(fromCurrencyValueSU != -1 && toCurrencyValueSU != -1){
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.FLOOR);
            double result = new Double(df.format((amount*fromCurrencyValueSU)/toCurrencyValueSU));
            return result;
        }else{
            return -1;
        }
    }

    public double getCurrencySingleUSDValue(String currencyName){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT CurrencyUSDSingleValue FROM currencies WHERE (CurrencyName = ?)",
                new String[]{currencyName});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getDouble(0);
        }else{
            return -1;
        }
    }

    public String[] getAvailableCurrenciesFullName(){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT CurrencyFullName, CurrencyName FROM currencies",
                new String[]{});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            ArrayList<String> arrayList = new ArrayList<>();
            int size = cursor.getCount();
            for(int i = 0; i < size; i++){
                arrayList.add(cursor.getString(1) + " - " + cursor.getString(0));
                cursor.moveToNext();
            }
            String[] results = new String[size];
            return arrayList.toArray(results);
        }else{
            return new String[]{};
        }
    }

    public Boolean addCurrency(String CurrencyName, double CurrencyUSDSingleValue, String CurrencyFullName) {
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CurrencyName", CurrencyName);
        contentValues.put("CurrencyUSDSingleValue", CurrencyUSDSingleValue);
        contentValues.put("CurrencyFullName", CurrencyFullName);

        long res = db.insert("currencies", null, contentValues);
        return res != -1;
    }

    public Boolean addWallet(String walletID, String walletPhone, String userID, String pin, boolean active, String walletCurrency, double balance) {
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("WalletID", walletID);
        contentValues.put("WalletPhone", walletPhone);
        contentValues.put("WalletUserID", userID);
        contentValues.put("WalletPin", pin);
        contentValues.put("WalletIsActive", active);
        contentValues.put("WalletCurrency", walletCurrency);
        contentValues.put("WalletBalance", balance);

        long res = db.insert("wallets", null, contentValues);
        return res != -1;
    }

    public Boolean addTransaction(String transactionID, String transactionOriginWalletID, String transactionDestinationWalletID, long transactionUnixSeconds, String transactionCurrency, double transactionAmount, TransactionStatus transactionStatus) {
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("TransactionID", transactionID);
        contentValues.put("TransactionOriginWalletID", transactionOriginWalletID);
        contentValues.put("TransactionDestinationWalletID", transactionDestinationWalletID);
        contentValues.put("TransactionUnixSeconds", transactionUnixSeconds);
        contentValues.put("TransactionCurrency", transactionCurrency);
        contentValues.put("TransactionAmount", transactionAmount);
        contentValues.put("TransactionStatus", transactionStatus.ordinal());

        long res = db.insert("transactions", null, contentValues);
        return res != -1;
    }

    public Boolean addTicket(String ticketID, String eventID, String eventPricingClassName, String ticketOwnerUserID, String ticketTransactionID, String ticketUniqueToken) {
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("TicketID", ticketID);
        contentValues.put("EventID", eventID);
        contentValues.put("EventPricingClassName", eventPricingClassName);
        contentValues.put("TicketOwnerUserID", ticketOwnerUserID);
        contentValues.put("TicketTransactionID", ticketTransactionID);
        contentValues.put("TicketUniqueToken", ticketUniqueToken);

        long res = db.insert("tickets", null, contentValues);
        return res != -1;
    }

    public Boolean addUser(String UserID, String UserProfileImgSrc, String UserEmail, String UserDisplayName, String UserPassword, String UserPhone, String UserGender, long UserDOB, int UserIsOrganizer){
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("UserID", UserID);
        contentValues.put("UserProfileImgSrc", UserProfileImgSrc);
        contentValues.put("UserEmail", UserEmail);
        contentValues.put("UserDisplayName", UserDisplayName);
        contentValues.put("UserPassword", UserPassword);
        contentValues.put("UserPhone", UserPhone);
        contentValues.put("UserGender", UserGender);
        contentValues.put("UserDOB", UserDOB);
        contentValues.put("UserIsOrganizer", 0);

        long res = db.insert("users", null, contentValues);
        return res != -1;
    }

    public Boolean addEvent(String EventID, String EventOrganizerUserID, String EventTitle, long EventStartDate, String EventDescription, double EventLatitude, double EventLongitude, String EventLocationDesc){
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("EventID", EventID);
        contentValues.put("EventOrganizerUserID", EventOrganizerUserID);
        contentValues.put("EventTitle", EventTitle);
        contentValues.put("EventStartDate", EventStartDate);
        contentValues.put("EventDescription", EventDescription);
        contentValues.put("EventLatitude", EventLatitude);
        contentValues.put("EventLongitude", EventLongitude);
        contentValues.put("EventLocationDesc", EventLocationDesc);

        long res = db.insert("events", null, contentValues);
        return res != -1;
    }

    public Boolean addEventPricing(String EventID, String EPClassName, String EPClassCurrency, double EPClassPrice, int EPClassStock){
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("EventID", EventID);
        contentValues.put("EPClassName", EPClassName);
        contentValues.put("EPClassCurrency", EPClassCurrency);
        contentValues.put("EPClassPrice", EPClassPrice);
        contentValues.put("EPClassStock", EPClassStock);

        long res = db.insert("eventpricings", null, contentValues);
        return res != -1;
    }

    public Boolean validateUnique(String UserEmail, String UserPhone){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE (UserEmail = ? OR UserPhone = ?)",
                new String[]{UserEmail, UserPhone});

        return !(cursor.getCount() > 0);
    }

    public Boolean validateCredentialsEmail(String UserEmail, String UserPassword){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE (UserEmail = ? AND UserPassword = ?)",
                new String[]{UserEmail, UserPassword});
        return cursor.getCount() > 0;
    }

    public Boolean validateCredentialsPhone(String UserPhone, String UserPassword){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE (UserPhone = ? AND UserPassword = ?)",
                new String[]{UserPhone, UserPassword});
        return cursor.getCount() > 0;
    }

    public String getUserIDFromPhone(String phone){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE (UserPhone = ?)",
                new String[]{phone});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getString(0);
        }else{
            return null;
        }
    }

    public String getUserIDFromEmail(String email){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE (UserEmail = ?)",
                new String[]{email});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getString(0);
        }else{
            return null;
        }
    }

    public String getUserDisplayName(String UserID){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT UserDisplayName FROM users WHERE (UserID = ?)",
                new String[]{UserID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getString(0);
        }else{
            return null;
        }
    }

    public String getUserEmail(String UserID){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT UserEmail FROM users WHERE (UserID = ?)",
                new String[]{UserID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getString(0);
        }else{
            return null;
        }
    }

    public String getUserPhone(String UserID){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT UserPhone FROM users WHERE (UserID = ?)",
                new String[]{UserID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getString(0);
        }else{
            return null;
        }
    }

    public void resetAvailableCurrencies(){
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM currencies");
        addCurrency("USD", 1.0, "United States Dollar");
        addCurrency("IDR", 0.000069, "Indonesian Rupiah");
        addCurrency("EUR", 1.08, "Euro");
        addCurrency("GBP", 1.28, "Pound Sterling");
        addCurrency("INR", 0.013, "Indian Rupee");
        addCurrency("CNY", 0.15, "Chinese Yuan");
        addCurrency("AUD", 0.72,"Australian Dollar");
        addCurrency("SGD", 0.73,"Singapore Dollar");
        addCurrency("CAD", 0.79,"Canadian Dollar");
        addCurrency("JPY", 0.0078,"Japanese Yen");
        addCurrency("HKD", 0.13,"Hong Kong Dollar");
        addCurrency("CHF", 1.04,"Swiss Franc");
    }

    public void seedFWUserAndWallet(){
        if(getUserFromID("U0000") == null){
            addUser("U0000",
                    "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_640.png",
                    "festwithin",
                    "FestWithin Team",
                    "fw1234",
                    "000000",
                    "Unspecified",
                    0,
                    0
            );
        }
        if(getWalletFromID("W0000") == null){
            addWallet("W0000",
                    "000000",
                    "U0000",
                    "fw1234",
                    true,
                    "USD",
                    99999999);
        }
        if(getWalletFromID("W0001") == null){
            addWallet("W0001",
                    "812815",
                    "U0001",
                    "johndoe1",
                    true,
                    "USD",
                    9999);
        }
    }

    public void seedExampleUser(){
        if(getUserFromID("U0001") == null){
            addUser("U0001",
                    "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_640.png",
                    "user@ex.com",
                    "John Doe",
                    "johndoe1",
                    "812815",
                    "Male",
                    1020740158,
                    0
                    );
            makeUserOrganizer("U0001", "John Doe Events");
        }
    }

    public void seedExampleEvents(){
        if(getEventFromID("E0001") == null){
            addEvent("E0001",
                    "U0001",
                    "Easter Celebration 2022",
                    1650903727,
                    "Celebrating easter together in a comfy place!",
                    -6.204531,
                    106.634683,
                    "Grand Villa"
            );
            addEventPricing("E0001","Regular", "IDR", 35000, 20);
            addEventPricing("E0001", "Gold", "IDR",50000, 10);
            addEventPricing("E0001", "Platinum", "IDR",100000, 5);
        }
        if(getEventFromID("E0002") == null){
            addEvent("E0002",
                    "U0001",
                    "Bike Race MRMAX 2022",
                    1650902039,
                    "Show your racing skills here",
                    -6.204369,
                    106.633897,
                    "Double Loop Circuit"
            );
            addEventPricing("E0002", "Participant Kit", "IDR",0, 100);
            addEventPricing("E0002", "Racer Kit", "IDR",150000, 40);
        }
        if(getEventFromID("E0003") == null){
            addEvent("E0003",
                    "U0001",
                    "Fishing Festival 2023",
                    1682439727,
                    "Harvesting all the wonderful sea creatures here",
                    -6.204475,
                    106.635658,
                    "Megamas Indah Pond"
            );
            addEventPricing("E0003", "Fisher", "EUR", 7.5, 200);
            addEventPricing("E0003", "Fisher+", "EUR", 9.5, 150);
            addEventPricing("E0003", "Fisher++", "EUR", 12.5, 100);
            addEventPricing("E0003", "FisherPro", "EUR", 17.5, 60);
            addEventPricing("E0003", "FisherPro+", "EUR", 20.0, 30);
        }
        if(getEventFromID("E0004") == null){
            addEvent("E0004",
                    "U0001",
                    "Gucci Exclusive VIP Invitational 2022",
                    1682439727,
                    "No description available",
                    -6.208475,
                    106.639658,
                    "L'etique Cafe"
            );
            addEventPricing("E0004", "VIP", "USD", 149.99, 0);
            addEventPricing("E0004", "VVIP", "USD", 299.99, 0);
            addEventPricing("E0004", "Premiere", "USD", 549.99, 0);
        }
    }

    public String debug_getFirstUserDetails(){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users",
                new String[]{});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getString(0);
        }else{
            return "null";
        }
    }

    public User getUserFromID(String UserID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE (UserID = ?)",
                new String[]{UserID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return new User(cursor.getString(0),
                    cursor.getString(3),
                    cursor.getString(2),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(4),
                    cursor.getLong(7),
                    cursor.getString(1),
                    cursor.getInt(8) != 0,
                    cursor.getString(9)
            );
        }else{
            return null;
        }
    }

    public Wallet getUserWallet(String UserID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM wallets WHERE (WalletUserID = ?)",
                new String[]{UserID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return new Wallet(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4) != 0,
                    cursor.getString(5),
                    cursor.getDouble(6)
            );
        }else{
            return null;
        }
    }

    public ArrayList<Ticket> getUserTickets(String UserID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tickets WHERE (TicketOwnerUserID = ?)",
                new String[]{UserID});
        cursor.moveToFirst();
        ArrayList<Ticket> arrayList = new ArrayList<>();
        if(cursor.getCount() > 0){
            int size = cursor.getCount();
            for(int i = 0; i < size; i++){
                arrayList.add(new Ticket(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5)
                ));
                cursor.moveToNext();
            }
        }
        return arrayList;
    }

    public Wallet getWalletFromID(String WalletID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM wallets WHERE (WalletID = ?)",
                new String[]{WalletID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return new Wallet(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4) != 0,
                    cursor.getString(5),
                    cursor.getDouble(6)
            );
        }else{
            return null;
        }
    }

    public ArrayList<WalletTransaction> getOutboundTransactionsFromWalletID(String WalletID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions WHERE (TransactionOriginWalletID = ?)",
                new String[]{WalletID});
        cursor.moveToFirst();
        ArrayList<WalletTransaction> arrayList = new ArrayList<>();
        if(cursor.getCount() > 0){
            int size = cursor.getCount();
            for(int i = 0; i < size; i++){
                arrayList.add(new WalletTransaction(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getString(4),
                        cursor.getDouble(5),
                        cursor.getInt(6) == 0 ? TransactionStatus.UNPAID : cursor.getInt(6) == 1 ? TransactionStatus.PAID : TransactionStatus.CANCELLED
                ));
                cursor.moveToNext();
            }
            Collections.reverse(arrayList);
        }
        return arrayList;
    }

    public ArrayList<WalletTransaction> getInboundTransactionsFromWalletID(String WalletID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions WHERE (TransactionDestinationWalletID = ?)",
                new String[]{WalletID});
        cursor.moveToFirst();
        ArrayList<WalletTransaction> arrayList = new ArrayList<>();
        if(cursor.getCount() > 0){
            int size = cursor.getCount();
            for(int i = 0; i < size; i++){
                arrayList.add(new WalletTransaction(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getString(4),
                        cursor.getDouble(5),
                        cursor.getInt(6) == 0 ? TransactionStatus.UNPAID : cursor.getInt(6) == 1 ? TransactionStatus.PAID : TransactionStatus.CANCELLED
                ));
                cursor.moveToNext();
            }
            Collections.reverse(arrayList);
        }
        return arrayList;
    }

    public WalletTransaction getTransactionFromID(String TransactionID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions WHERE (TransactionID = ?)",
                new String[]{TransactionID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return new WalletTransaction(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getLong(3),
                    cursor.getString(4),
                    cursor.getDouble(5),
                    cursor.getInt(6) == 0 ? TransactionStatus.UNPAID : cursor.getInt(6) == 1 ? TransactionStatus.PAID : TransactionStatus.CANCELLED
            );
        }else{
            return null;
        }
    }

    public Ticket getTicketFromID(String TicketID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tickets WHERE (TicketID = ?)",
                new String[]{TicketID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return new Ticket(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
            );
        }else{
            return null;
        }
    }

    public ArrayList<Event> getAllEvents(int rangeKM, double lat, double lng){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM events",
                new String[]{});
        cursor.moveToFirst();
        ArrayList<Event> arrayList = new ArrayList<>();
        if(cursor.getCount() > 0){
            int size = cursor.getCount();
            for(int i = 0; i < size; i++){
                Event e = getEventFromID(cursor.getString(0));
                if(getEventDistance(e, lat, lng) <= rangeKM){
                    arrayList.add(e);
                }
                cursor.moveToNext();
            }
        }
        return arrayList;
    }

    public double getEventDistance(Event e, double lat, double lng){
        //in KM
        double distanceLat = e.getEventLatitude() - lat;
        double distanceLong = e.getEventLongitude() - lng;
        return Math.sqrt(Math.pow(latToKM(distanceLat), 2)+ Math.pow(lngToKM(distanceLong), 2));
    }



    public ArrayList<Event> getAllEvents(){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM events",
                new String[]{});
        cursor.moveToFirst();
        ArrayList<Event> arrayList = new ArrayList<>();
        if(cursor.getCount() > 0){
            int size = cursor.getCount();
            for(int i = 0; i < size; i++){
                arrayList.add(getEventFromID(cursor.getString(0)));
                cursor.moveToNext();
            }
        }
        return arrayList;
    }

    public ArrayList<Event> getAllCreatedEvents(String UserID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM events WHERE EventOrganizerUserID = ?",
                new String[]{UserID});
        cursor.moveToFirst();
        ArrayList<Event> arrayList = new ArrayList<>();
        if(cursor.getCount() > 0){
            int size = cursor.getCount();
            for(int i = 0; i < size; i++){
                arrayList.add(getEventFromID(cursor.getString(0)));
                cursor.moveToNext();
            }
        }
        return arrayList;
    }

    public Event getEventFromID(String EventID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM events WHERE (EventID = ?)",
                new String[]{EventID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            Event e = new Event(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getLong(3),
                    cursor.getString(4),
                    cursor.getDouble(5),
                    cursor.getDouble(6),
                    cursor.getString(8)
            );
            if(cursor.getString(7) == null){
                e.setEventBannerSrc(DEFAULT_EVENT_BANNER);
            }else{
                e.setEventBannerSrc(cursor.getString(7));
            }
            e.getEventPricing().addAll(getEventPricingsFromID(EventID));
            return e;
        }else{
            return null;
        }
    }

    public ArrayList<EventPricing> getEventPricingsFromID(String EventID){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM eventpricings WHERE (EventID = ?)",
                new String[]{EventID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            ArrayList<EventPricing> arrayList = new ArrayList<>();
            int size = cursor.getCount();
            for(int i = 0; i < size; i++){
                arrayList.add(new EventPricing(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getInt(4)
                ));
                cursor.moveToNext();
            }
            return arrayList;
        }else{
            return null;
        }
    }

    public boolean insertUser(User user){
        return addUser(user.getUserID(), user.getUserProfileImgSrc(), user.getUserEmail(), user.getUserDisplayName(), user.getUserPassword(), user.getUserPhone(), user.getUserGender(), user.getUserDOB(), user.isUserIsOrganizer() ? 1 : 0);
    }

    public int getUsersCount(){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users",
                new String[]{});
        return cursor.getCount();
    }

    public int getTicketsCount(){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tickets",
                new String[]{});
        return cursor.getCount();
    }

    public int getTransactionsCount(){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions",
                new String[]{});
        return cursor.getCount();
    }

    public int getWalletsCount(){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM wallets",
                new String[]{});
        return cursor.getCount();
    }

    public int getEventsCount(){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM events",
                new String[]{});
        return cursor.getCount();
    }

    public int getEPCount(){
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM eventpricings",
                new String[]{});
        return cursor.getCount();
    }

    public void changeUserDisplayName(String UserID, String newDisplayName){
        db = getWritableDatabase();
        db.execSQL("UPDATE users SET UserDisplayName = ? WHERE UserID = ?", new String[]{newDisplayName, UserID});
    }

    public int getEPClassStock(String EventID, String EPClassName){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT EPClassStock FROM eventpricings WHERE (EventID = ? AND EPClassName = ?)",
                new String[]{EventID, EPClassName});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getInt(0);
        }else{
            return -1;
        }
    }

    public void updateEPStock(int newStock, String EventID, String EPClassName){
        db = getWritableDatabase();
        db.execSQL("UPDATE eventpricings SET EPClassStock = ? WHERE (EventID = ? AND EPClassName = ?)", new String[]{""+newStock, EventID, EPClassName});
    }

    public void makeUserOrganizer(String UserID, String OrganizerName){
        db = getWritableDatabase();
        db.execSQL("UPDATE users SET UserIsOrganizer = 1, UserOrganizerName = ? WHERE (UserID = ?)", new String[]{OrganizerName, UserID});
    }

    public void changeOrganizerName(String UserID, String OrganizerName){
        db = getWritableDatabase();
        db.execSQL("UPDATE users SET UserOrganizerName = ? WHERE (UserID = ?)", new String[]{OrganizerName, UserID});
    }

    public void updateWalletBalance(double newBalance, String WalletID){
        db = getWritableDatabase();
        db.execSQL("UPDATE wallets SET WalletBalance = ? WHERE (WalletID = ?)", new String[]{""+newBalance, WalletID});
    }

    public void updateUserPassword(String newPassword, String UserID){
        db = getWritableDatabase();
        db.execSQL("UPDATE users SET UserPassword = ? WHERE (UserID = ?)", new String[]{newPassword, UserID});
    }

    public void updateUserProfileSrc(String newImageUri, String UserID){
        db = getWritableDatabase();
        db.execSQL("UPDATE users SET UserProfileImgSrc = ? WHERE (UserID = ?)", new String[]{newImageUri, UserID});
    }

    public void invalidateTicket(String TicketID){
        db = getWritableDatabase();
        db.execSQL("UPDATE tickets SET TicketUniqueToken = ? WHERE (TicketID = ?)", new String[]{"Invalid/Expired", TicketID});
    }

    public void changeWalletCurrency(String newCurrency, String WalletID){
        db = getWritableDatabase();
        String oldCurrency = getWalletFromID(WalletID).getWalletCurrency();
        db.execSQL("UPDATE wallets SET WalletCurrency = ? WHERE (WalletID = ?)", new String[]{newCurrency, WalletID});
        updateWalletBalance(convertCurrency(getWalletBalance(WalletID), oldCurrency, newCurrency), WalletID);
    }



    public double getWalletBalance(String WalletID){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT WalletBalance FROM wallets WHERE (WalletID = ?)",
                new String[]{WalletID});
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            return cursor.getDouble(0);
        }else{
            return -1;
        }
    }

    public void updateTransactionStatus(TransactionStatus newStatus, String TransactionID){
        db = getWritableDatabase();
        db.execSQL("UPDATE transactions SET TransactionStatus = ? WHERE TransactionID = ?", new String[]{""+newStatus.ordinal(), TransactionID});
    }



}

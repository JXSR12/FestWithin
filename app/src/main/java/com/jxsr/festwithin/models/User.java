package com.jxsr.festwithin.models;

import java.util.Date;

public class User {
    private String UserID;
    private String UserDisplayName;
    private String UserEmail;
    private String UserPhone;
    private String UserGender;
    private String UserPassword;
    private long UserDOB;
    private String UserProfileImgSrc;
    private boolean UserIsOrganizer;
    private String UserOrganizerName;

    public User(String userID, String userDisplayName, String userEmail, String userPhone, String userGender, String userPassword, long userDOB, String userProfileImgSrc, boolean userIsOrganizer, String userOrganizerName) {
        UserID = userID;
        UserDisplayName = userDisplayName;
        UserEmail = userEmail;
        UserPhone = userPhone;
        UserGender = userGender;
        UserPassword = userPassword;
        UserDOB = userDOB;
        UserProfileImgSrc = userProfileImgSrc;
        UserIsOrganizer = userIsOrganizer;
        UserOrganizerName = userOrganizerName;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getUserDisplayName() {
        return UserDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        UserDisplayName = userDisplayName;
    }

    public String getUserEmail() {
        return UserEmail;
    }

    public void setUserEmail(String userEmail) {
        UserEmail = userEmail;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public void setUserPhone(String userPhone) {
        UserPhone = userPhone;
    }

    public String getUserGender() {
        return UserGender;
    }

    public void setUserGender(String userGender) {
        UserGender = userGender;
    }

    public String getUserPassword() {
        return UserPassword;
    }

    public void setUserPassword(String userPassword) {
        UserPassword = userPassword;
    }

    public long getUserDOB() {
        return UserDOB;
    }

    public void setUserDOB(long userDOB) {
        UserDOB = userDOB;
    }

    public String getUserProfileImgSrc() {
        return UserProfileImgSrc;
    }

    public void setUserProfileImgSrc(String userProfileImgSrc) {
        UserProfileImgSrc = userProfileImgSrc;
    }

    public boolean isUserIsOrganizer() {
        return UserIsOrganizer;
    }

    public void setUserIsOrganizer(boolean userIsOrganizer) {
        UserIsOrganizer = userIsOrganizer;
    }

    public String getUserOrganizerName() {
        return UserOrganizerName;
    }

    public void setUserOrganizerName(String userOrganizerName) {
        UserOrganizerName = userOrganizerName;
    }
}

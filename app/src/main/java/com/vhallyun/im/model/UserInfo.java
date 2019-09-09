package com.vhallyun.im.model;

/**
 * Created by zwp on 2019-08-28
 */
public class UserInfo {

    String userId;
    boolean isOnline = false;

    public UserInfo() {
    }

    public UserInfo(String userId, boolean isOnline) {
        this.userId = userId;
        this.isOnline = isOnline;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}

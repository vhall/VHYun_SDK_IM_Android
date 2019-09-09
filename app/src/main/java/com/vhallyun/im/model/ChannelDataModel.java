package com.vhallyun.im.model;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwp on 2019-08-28
 */
public class ChannelDataModel {


    /**
     * list : ["ONEPLUS A5010"]
     * context : {"ONEPLUS A5010":false}
     * disable_users : ["chat8749"]
     * channel_disable : false
     * total : 1
     * page_num : 1
     * page_all : 1
     */

    private boolean channel_disable;
    private int total;
    private int page_num;
    private int page_all;
    private JsonObject context;
    private List<String> list;
    private List<String> disable_users;

    public JsonObject getContext() {
        return context;
    }

    public void setContext(JsonObject context) {
        this.context = context;
    }

    public boolean isChannel_disable() {
        return channel_disable;
    }

    public void setChannel_disable(boolean channel_disable) {
        this.channel_disable = channel_disable;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage_num() {
        return page_num;
    }

    public void setPage_num(int page_num) {
        this.page_num = page_num;
    }

    public int getPage_all() {
        return page_all;
    }

    public void setPage_all(int page_all) {
        this.page_all = page_all;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public List<String> getDisable_users() {
        return disable_users;
    }

    public void setDisable_users(List<String> disable_users) {
        this.disable_users = disable_users;
    }

    public void resetList() {
        disableList.clear();
        onlineList.clear();
        for (String id : list) {
            if (disable_users.contains(id)) {
                UserInfo info = new UserInfo(id, true);
                disableList.add(info);
            } else {
                UserInfo info = new UserInfo(id, true);
                onlineList.add(info);
            }
        }
    }

    private List<UserInfo> onlineList = new ArrayList<>();
    private List<UserInfo> disableList = new ArrayList<>();

    public List<UserInfo> getOnlineList() {
        return onlineList;
    }

    public void setOnlineList(List<UserInfo> onlineList) {
        this.onlineList = onlineList;
    }

    public List<UserInfo> getDisableList() {
        return disableList;
    }

    public void setDisableList(List<UserInfo> disableList) {
        this.disableList = disableList;
    }

}

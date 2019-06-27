package com.example.frametest.UserMode;

public class User {
    private int id;
    private String user_name;
    private String user_phone;
    private String user_pasw;
    private int user_age;
    private boolean user_sex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getUser_pasw() {
        return user_pasw;
    }

    public void setUser_pasw(String user_pasw) {
        this.user_pasw = user_pasw;
    }

    public int getUser_age() {
        return user_age;
    }

    public void setUser_age(int user_age) {
        this.user_age = user_age;
    }

    public boolean isUser_sex() {
        return user_sex;
    }

    public void setUser_sex(boolean user_sex) {
        this.user_sex = user_sex;
    }
}

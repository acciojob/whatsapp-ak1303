package com.driver;

public class User {
    private String name;
    private String mobile;

    public void setName(String name) {
        this.name = name;
    }

    public User() {
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public User(String name, String mobile){
        this.name=name;
        this.mobile=mobile;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }
}

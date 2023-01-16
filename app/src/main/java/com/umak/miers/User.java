package com.umak.miers;

public class User {

    public String email, fullname, address, feedback;

    public User() {
    }

    public User(String email, String fullname, String address) {
        this.email = email;
        this.fullname = fullname;
        this.address = address;
    }

    public User(String email, String fullname) {
        this.email = email;
        this.fullname = fullname;
    }

    public User(String feedback) {
        this.feedback = feedback;
    }

}

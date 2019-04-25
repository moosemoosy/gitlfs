package com.airzac.inspire;

public class Users {
    private String email, username, fullname;

    public Users()
    {

    }

    public Users(String email, String username, String fullname) {
        this.email = email;
        this.username = username;
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}

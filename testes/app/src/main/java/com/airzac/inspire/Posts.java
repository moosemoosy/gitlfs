package com.airzac.inspire;

public class Posts {
    private String articlehtml, articletitle, authorname, date, personalityname, personalityid, time, uid, encrypted, decryptkey;
    private int published;
    public Posts()
    {

    }

    public Posts(String encrypted, String decryptkey) {
        this.encrypted = encrypted;
        this.decryptkey = decryptkey;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    public String getDecryptkey() {
        return decryptkey;
    }

    public void setDecryptkey(String decryptkey) {
        this.decryptkey = decryptkey;
    }

    public Posts(int published) {
        this.published = published;
    }

    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }

    public Posts(String articlehtml, String articletitle, String authorname, String date, String personalityname, String personalityid, String time, String uid) {
        this.articlehtml = articlehtml;
        this.articletitle = articletitle;
        this.authorname = authorname;
        this.date = date;
        this.personalityid = personalityid;
        this.personalityname = personalityname;
        this.time = time;
        this.uid = uid;
    }

    public String getArticlehtml() {
        return articlehtml;
    }

    public void setArticlehtml(String articlehtml) {
        this.articlehtml = articlehtml;
    }

    public String getArticleTitle() {
        return articletitle;
    }

    public void setArticleTitle(String articletitle) {
        this.articletitle = articletitle;
    }

    public String getAuthorname() {
        return authorname;
    }

    public void setAuthorname(String authorname) {
        this.authorname = authorname;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPersonalityid() {
        return personalityid;
    }

    public void setPersonalityid(String personalityid) {
        this.personalityid = personalityid;
    }

    public String getPersonalityname() {
        return personalityname;
    }

    public void setPersonalityname(String personalityname) {
        this.personalityname = personalityname;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

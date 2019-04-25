package com.airzac.inspire;

public class Personality {

    private String personalityname, description, id;

    public Personality() {

    }

    public Personality(String personalityname, String description, String id) {
        this.personalityname = personalityname;
        this.description = description;
        this.id = id;
    }

    public String getPersonalityname() {
        return personalityname;
    }

    public void setPersonalityname(String personalityname) {
        this.personalityname = personalityname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

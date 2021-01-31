package com.example.cmp309spacegame;

//this stores the data to be sent to the firebase database
public class Users {
    private String name;
    private Integer score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //this is used, do not delete
    public Integer getScore() {
        return score;
    }

    void setScore(Integer score) {
        this.score = score;
    }
}

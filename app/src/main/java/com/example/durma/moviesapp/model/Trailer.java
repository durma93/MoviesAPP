package com.example.durma.moviesapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by durma on 24.1.18..
 */

public class Trailer implements Serializable {
    @SerializedName("key")
    private String key;

    @SerializedName("name")
    private String name;

    public Trailer(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

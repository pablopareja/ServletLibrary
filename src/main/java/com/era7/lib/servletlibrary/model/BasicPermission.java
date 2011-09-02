package com.era7.lib.servletlibrary.model;

public class BasicPermission {

    protected String id;
    protected String description;

    public BasicPermission() {
        id = "";
        description = "";
        
    }

    public BasicPermission(String id, String description) {
        this.id = id;
        this.description = description;
    }

    //----------------------GETTERS----------------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    //------------------------------------------------

    //----------------------SETTERS----------------
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    //------------------------------------------------
}

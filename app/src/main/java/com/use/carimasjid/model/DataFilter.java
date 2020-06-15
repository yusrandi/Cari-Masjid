package com.use.carimasjid.model;

public class DataFilter {
    private String id, parentId, name;

    public DataFilter(String id, String parentId, String name) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

    public DataFilter(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }
}

package com.shareplaylearn.models;

/**
 * Created by stu on 11/7/16.
 */
public class GoogleUserEntity {
    private String id;
    private String displayName;
    private String url;

    public String getId() {
        return id;
    }

    public GoogleUserEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public GoogleUserEntity setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public GoogleUserEntity setUrl(String url) {
        this.url = url;
        return this;
    }
}

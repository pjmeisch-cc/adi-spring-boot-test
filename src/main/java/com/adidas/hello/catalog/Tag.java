package com.adidas.hello.catalog;


public class Tag extends BaseEntity {

    private String url;

    private String name;

    public Tag() {}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Tag{" +
            "url='" + url + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}

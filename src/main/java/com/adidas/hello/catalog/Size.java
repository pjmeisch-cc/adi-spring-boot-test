package com.adidas.hello.catalog;

public class Size extends BaseEntity {

    private String size;

    private String url;

    private int stock;

    public Size() {}

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "Size{" +
            "size='" + size + '\'' +
            ", url='" + url + '\'' +
            ", stock=" + stock +
            '}';
    }
}

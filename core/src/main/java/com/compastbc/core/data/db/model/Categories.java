package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity(nameInDb = "Categories")
public class Categories {

    @Id
    private Long id;
    private String categoryId;
    private String categoryName;
    private String productId;

    @Generated(hash = 1332401159)
    public Categories(Long id, String categoryId, String categoryName,
                      String productId) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productId = productId;
    }

    @Generated(hash = 267348489)
    public Categories() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}

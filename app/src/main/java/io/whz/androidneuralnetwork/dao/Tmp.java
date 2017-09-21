package io.whz.androidneuralnetwork.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Tmp {
    @Id
    private long id;

    private String name;

    @Generated(hash = 1734466971)
    public Tmp(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Generated(hash = 1095487782)
    public Tmp() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.zhc.hefengweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by zhc on 2018/9/19.
 * 创建省级实体类并继承于LitePal的DataSupport类，
 * DataSupport将实体类连接到SQLite数据库
 */

public class Province extends DataSupport {

    private int id;

    private String provinceName;

    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}

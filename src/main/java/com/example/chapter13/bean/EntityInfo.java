package com.example.chapter13.bean;

public class EntityInfo {
    public String name; // 实体名称
    public String relation; // 实体关系 就是好友这个string
    public EntityInfo(String name, String relation) {
        this.name = name;
        this.relation = relation;
    }
}

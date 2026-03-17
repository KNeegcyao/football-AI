package com.soccer.forum.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    USER("USER", "普通用户"),
    ADMIN("ADMIN", "管理员");

    @EnumValue
    private final String code;
    
    @JsonValue
    private final String desc;

    UserRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

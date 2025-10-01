package io.level114.domain;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ServerStatus {
    Active,
    Revoked,
    @JsonEnumDefaultValue
    Disabled;
}



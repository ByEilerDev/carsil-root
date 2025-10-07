package com.carsil.userapi.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Brand {
    TENNIS("TENNIS SAS"),
    ELEDE("LINEA DIRECTA"),
    BLANK("");

    private final String label;

    Brand(String label) { this.label = label; }

    @JsonValue
    public String getLabel() { return label; }

    public static Brand fromLabel(String label) { return fromJson(label); }

    @JsonCreator
    public static Brand fromJson(String value) {
        if (value == null) return null;
        String norm = value.trim();
        return Arrays.stream(values())
                .filter(r -> r.label.equalsIgnoreCase(norm))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid  Brand: " + value));
    }
}


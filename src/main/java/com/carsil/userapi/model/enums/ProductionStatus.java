package com.carsil.userapi.model.enums;

public enum ProductionStatus {
    PROCESO("PROCESO"),
    ASIGNADO("ASIGNADO"),
    CONFECCION("CONFECCIÓN");

    private final String label;
    ProductionStatus(String label) { this.label = label; }

    public String getLabel() { return label; }

}

package com.carsil.userapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "carsil_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Size(max = 255, message = "The description cannot exceed 255 characters.")
    private String description;

    @NotNull(message = "Module name cannot be null.")
    @Size(min = 2, max = 100, message = "The name must have between 2 and 100 characters.")
    @Column(nullable = false)
    private String name;

    @Column
    private Integer numPersons;

    private java.math.BigDecimal loadDays;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"module"})
    private java.util.List<Product> products = new java.util.ArrayList<>();

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("totaLoadDays")
    public java.math.BigDecimal getTotaLoadDays() {
        if (products == null || products.isEmpty()) return java.math.BigDecimal.ZERO;

        return products.stream()
                .map(Product::getLoadDays)
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
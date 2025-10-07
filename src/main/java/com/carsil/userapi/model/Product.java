package com.carsil.userapi.model;

import com.carsil.userapi.model.enums.Brand;
import com.carsil.userapi.model.enums.ProductionStatus;
import com.carsil.userapi.model.enums.StoppageReason;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@Entity
@Table(
        name = "carsil_product",
        uniqueConstraints = @UniqueConstraint(name = "uk_carsil_product_op", columnNames = "op")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal price;

    @NotNull
    @Column(nullable = false)
    private Integer quantity = 0;

    @NotNull
    @Column(nullable = false)
    private LocalDate assignedDate;

    @Column()
    private LocalDate plantEntryDate;

    @NotNull
    @Pattern(regexp = "^[0-9]*$", message = "The reference must contain only numbers.")
    @Column(nullable = false)
    private String reference;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Brand brand = Brand.BLANK;

    @NotNull
    @Pattern(regexp = "^[0-9]*$", message = "The OP field must contain only numbers.")
    @Column(nullable = false)
    private String op;

    @NotNull
    @Pattern(regexp = "^[0-9]*$", message = "The campaign must contain only numbers.")
    @Column(nullable = false)
    private String campaign;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "The type must contain letters and numbers.")
    @Column(nullable = false)
    private String type;

    @Column
    private String description;

    @ElementCollection
    @CollectionTable(name = "product_size_quantities", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "size")
    @Column(name = "units")
    private Map<String, Integer> sizeQuantities = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"products"})
    private Module module;

    @PrePersist
    private void validateOnCreate() {
        // Normalización inline
        if (sizeQuantities == null) sizeQuantities = new java.util.HashMap<>();
        sizeQuantities.replaceAll((k, v) -> v == null ? 0 : Math.max(0, v));

        int sum = sizeQuantities.values().stream().mapToInt(Integer::intValue).sum();
        boolean hasSizes = sum > 0;

        if (quantity == null || quantity == 0) {
            if (!hasSizes) throw new IllegalArgumentException("quantity or sizeQuantities are required on create");
            quantity = sum;
        } else {
            if (!hasSizes) throw new IllegalArgumentException("sizeQuantities are required when quantity is provided on create");
            if (!quantity.equals(sum)) {
                throw new IllegalArgumentException("The sum of sizes (" + sum + ") does not match the total (quantity=" + quantity + ")");
            }
        }

        if (quantityMade == null) quantityMade = 0;
        if (quantityMade < 0 || quantityMade > quantity) {
            throw new IllegalArgumentException("quantityMade must be between 0 and quantity");
        }
    }

    @jakarta.persistence.PreUpdate
    private void validateOnUpdate() {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be set before updating");
        }
        if (quantityMade == null) quantityMade = 0;
        if (quantityMade < 0 || quantityMade > quantity) {
            throw new IllegalArgumentException("quantityMade must be between 0 and quantity");
        }
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ProductionStatus status = ProductionStatus.PROCESO;

    @Enumerated(EnumType.STRING)
    @Column(name = "descripcion_paro", length = 50)
    private StoppageReason stoppageReason;

    @Column(nullable = false)
    private Integer quantityMade = 0;

    @Column
    private String actualDeliveryDate;    // FECHA ENTREGA REAL

    @Column
    private Integer missing;                  // FALTA

    @Column
    private Double sam;                   // SAM

    @Column
    private Integer samTotal;                 // SAM TOTAL

    @Transient
    public BigDecimal getTotalPrice() {
        return (price == null || quantity == null)
                ? BigDecimal.ZERO
                : price.multiply(BigDecimal.valueOf(quantity));
    }


    @Transient
    public Integer getCycleCalculated() {
        if (assignedDate == null) return null;
        LocalDate end = (plantEntryDate != null) ? plantEntryDate : LocalDate.now();
        long diff = DAYS.between(assignedDate, end);
        return (diff < 0) ? 0 : Math.toIntExact(diff);
    }

    @Transient
    public Integer getQuantityPending() {
        if (quantity == null || quantityMade == null) return null;
        return Math.max(0, quantity - quantityMade);
    }

    public void addMade(int delta) {
        if (delta == 0) return;

        int base = (quantityMade == null ? 0 : quantityMade);
        if (quantity == null)
            throw new IllegalStateException("quantity must be set before updating quantityMade");

        int newMade = base + delta;
        if (newMade < 0) throw new IllegalArgumentException("quantityMade cannot be negative");
        if (newMade > quantity) throw new IllegalArgumentException("quantityMade cannot exceed total quantity");

        this.quantityMade = newMade;

        if (this.quantity != null) this.missing = Math.max(0, this.quantity - newMade);
        if (this.sam != null && this.missing != null) {
            this.samTotal = (int) Math.round(this.sam * this.missing);
        }
    }

    @Transient
    public Double getDeliveryPercentage() {
        if (quantity == null || quantity == 0 || quantityMade == null) {
            return 0.0;
        }
        return (quantityMade.doubleValue() / quantity.doubleValue()) * 100.0;
    }

    @Transient
    public BigDecimal getLoadDays() {
        Integer samTotalMin = this.samTotal;
        Integer people = (module != null ? module.getNumPersons() : null);

        if (samTotalMin == null || samTotalMin <= 0) return BigDecimal.ZERO;
        if (people == null || people <= 0) return BigDecimal.ZERO;

        return BigDecimal.valueOf(samTotalMin)
                .divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(9), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(people), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(1.35))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
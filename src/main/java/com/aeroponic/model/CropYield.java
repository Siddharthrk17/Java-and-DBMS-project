package com.aeroponic.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "crop_yield")
public class CropYield {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yield_id")
    private Integer yieldId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tower_id", nullable = false)
    private AeroponicTower tower;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "harvest_date", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "quantity_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantityKg;

    public CropYield() {}

    public CropYield(AeroponicTower tower, Product product, LocalDate harvestDate, BigDecimal quantityKg) {
        this.tower = tower;
        this.product = product;
        this.harvestDate = harvestDate;
        this.quantityKg = quantityKg;
    }

    public Integer getYieldId() { return yieldId; }
    public void setYieldId(Integer yieldId) { this.yieldId = yieldId; }

    public AeroponicTower getTower() { return tower; }
    public void setTower(AeroponicTower tower) { this.tower = tower; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }

    public BigDecimal getQuantityKg() { return quantityKg; }
    public void setQuantityKg(BigDecimal quantityKg) { this.quantityKg = quantityKg; }
}

package com.aeroponic.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "so_lines")
public class SOLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Integer lineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "so_id", nullable = false)
    @JsonBackReference
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_kg", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityKg;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", insertable = false, updatable = false)
    private BigDecimal lineTotal;

    public SOLine() {}

    public SOLine(SalesOrder salesOrder, Product product, BigDecimal quantityKg, BigDecimal unitPrice) {
        this.salesOrder = salesOrder;
        this.product = product;
        this.quantityKg = quantityKg;
        this.unitPrice = unitPrice;
    }

    public Integer getLineId() { return lineId; }
    public void setLineId(Integer lineId) { this.lineId = lineId; }

    public SalesOrder getSalesOrder() { return salesOrder; }
    public void setSalesOrder(SalesOrder salesOrder) { this.salesOrder = salesOrder; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getQuantityKg() { return quantityKg; }
    public void setQuantityKg(BigDecimal quantityKg) { this.quantityKg = quantityKg; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() {
        if (lineTotal != null) return lineTotal;
        if (quantityKg != null && unitPrice != null) return quantityKg.multiply(unitPrice);
        return BigDecimal.ZERO;
    }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}

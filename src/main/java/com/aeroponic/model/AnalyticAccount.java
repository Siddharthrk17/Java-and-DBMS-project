package com.aeroponic.model;

import jakarta.persistence.*;

@Entity
@Table(name = "analytic_accounts")
public class AnalyticAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analytic_id")
    private Integer analyticId;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tower_id")
    private AeroponicTower tower;

    public AnalyticAccount() {}

    public AnalyticAccount(String code, String name, AeroponicTower tower) {
        this.code = code;
        this.name = name;
        this.tower = tower;
    }

    public Integer getAnalyticId() { return analyticId; }
    public void setAnalyticId(Integer analyticId) { this.analyticId = analyticId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AeroponicTower getTower() { return tower; }
    public void setTower(AeroponicTower tower) { this.tower = tower; }
}

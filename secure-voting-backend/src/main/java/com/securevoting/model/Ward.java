package com.securevoting.model;

import javax.persistence.*;

@Entity
@Table(name = "wards")
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ward_id")
    private Integer wardId;

    @Column(name = "ward_name", nullable = false, unique = true)
    private String wardName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Constructors
    public Ward() {}

    public Ward(String wardName, String description) {
        this.wardName = wardName;
        this.description = description;
    }

    // Getters and Setters
    public Integer getWardId() {
        return wardId;
    }

    public void setWardId(Integer wardId) {
        this.wardId = wardId;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}









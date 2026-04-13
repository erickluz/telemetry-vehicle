package com.example.vehicletelemetrydashboard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class VehicleInfoEmbeddable {

    @Column(name = "plate", nullable = false, unique = true)
    private String plate;

    @Column(name = "vin")
    private String vin;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "model_name")
    private String model;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(name = "color")
    private String color;

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

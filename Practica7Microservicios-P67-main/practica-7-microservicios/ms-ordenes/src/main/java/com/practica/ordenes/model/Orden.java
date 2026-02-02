package com.practica.ordenes.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @SequenceGenerator(
            name = "orden_seq",
            sequenceName = "orden_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orden_seq")
    private Long id;

    private String destino;

    @Column(length = 2000)
    private String productosJson;

    private Double costoEnvio;

    private Instant creadaEn;

    public Orden() {}

    public Orden(String destino, String productosJson, Double costoEnvio) {
        this.destino = destino;
        this.productosJson = productosJson;
        this.costoEnvio = costoEnvio;
        this.creadaEn = Instant.now();
    }

    public Long getId() { return id; }
    public String getDestino() { return destino; }
    public String getProductosJson() { return productosJson; }
    public Double getCostoEnvio() { return costoEnvio; }
    public Instant getCreadaEn() { return creadaEn; }

    public void setId(Long id) { this.id = id; }
    public void setDestino(String destino) { this.destino = destino; }
    public void setProductosJson(String productosJson) { this.productosJson = productosJson; }
    public void setCostoEnvio(Double costoEnvio) { this.costoEnvio = costoEnvio; }
    public void setCreadaEn(Instant creadaEn) { this.creadaEn = creadaEn; }
}

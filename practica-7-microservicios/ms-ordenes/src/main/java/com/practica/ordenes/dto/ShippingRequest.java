package com.practica.ordenes.dto;

import java.util.List;

public class ShippingRequest {
    public List<Integer> productos;
    public String destino;

    public ShippingRequest() {}

    public ShippingRequest(List<Integer> productos, String destino) {
        this.productos = productos;
        this.destino = destino;
    }
}

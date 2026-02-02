package com.practica.productos.controller;

import com.practica.productos.model.Producto;
import com.practica.productos.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Object health() {
        return new Object() {
            public final String estado = "ok";
            public final String servicio = "ms-productos";
        };
    }

    @GetMapping("/products")
    public List<Producto> listar() {
        return service.listar();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        Producto p = service.obtener(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    // PUT /products/{id}/stock?cantidad=2
    @PutMapping("/products/{id}/stock")
    public ResponseEntity<?> descontarStock(@PathVariable Long id, @RequestParam int cantidad) {
        Producto actualizado = service.descontarStock(id, cantidad);
        if (actualizado == null) {
            return ResponseEntity.badRequest().body("No existe el producto o no hay stock suficiente.");
        }
        return ResponseEntity.ok(actualizado);
    }
}
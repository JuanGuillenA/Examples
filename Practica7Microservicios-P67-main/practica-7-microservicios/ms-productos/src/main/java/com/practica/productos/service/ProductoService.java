package com.practica.productos.service;

import com.practica.productos.model.Producto;
import com.practica.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repo;

    public ProductoService(ProductoRepository repo) {
        this.repo = repo;
    }

    public List<Producto> listar() {
        return repo.findAll();
    }

    public Producto obtener(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Producto descontarStock(Long id, int cantidad) {
        Producto p = repo.findById(id).orElse(null);
        if (p == null) return null;
        if (p.getStock() == null || p.getStock() < cantidad) return null;

        p.setStock(p.getStock() - cantidad);
        return repo.save(p);
    }
}

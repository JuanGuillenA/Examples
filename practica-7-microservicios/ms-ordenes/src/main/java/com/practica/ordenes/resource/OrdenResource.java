package com.practica.ordenes.resource;

import com.practica.ordenes.dto.OrdenRequest;
import com.practica.ordenes.dto.ShippingRequest;
import com.practica.ordenes.dto.ShippingResponse;
import com.practica.ordenes.model.Orden;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdenResource {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("OrdenesPU");

    private static final Jsonb jsonb = JsonbBuilder.create();

    private static final String MS_ENVIOS = "http://ms-envios:8082";
    private static final String MS_PRODUCTOS = "http://ms-productos:8081";

    @GET
    @Path("/health")
    public Map<String, String> health() {
        Map<String, String> r = new HashMap<>();
        r.put("estado", "ok");
        r.put("servicio", "ms-ordenes");
        return r;
    }

    @POST
    @Path("/orders")
    public Response crearOrden(OrdenRequest req) {

        // Validación
        if (req == null || req.productos == null || req.productos.isEmpty()
                || req.destino == null || req.destino.isBlank()) {
            return Response.status(400).entity(Map.of(
                    "error", "Debes enviar 'productos' (lista) y 'destino'.",
                    "ejemplo", Map.of("destino", "Cuenca", "productos", new int[]{1,2,3})
            )).build();
        }

        Client client = ClientBuilder.newClient();
        EntityManager em = emf.createEntityManager();

        try {
            // 1) Llamar a ms-envios para calcular costo
            ShippingResponse envioResp;
            try {
                envioResp = client
                        .target(MS_ENVIOS + "/shipping/calculate")
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(new ShippingRequest(req.productos, req.destino),
                                MediaType.APPLICATION_JSON), ShippingResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(502).entity(Map.of(
                        "error", "No se pudo calcular el envío (ms-envios).",
                        "detalle", String.valueOf(e.getMessage())
                )).build();
            }

            // 2) Descontar stock en ms-productos (1 unidad por ID)
            try {
                for (Integer id : req.productos) {
                    Response stockResp = client
                            .target(MS_PRODUCTOS + "/products/" + id + "/stock")
                            .queryParam("cantidad", 1)
                            .request(MediaType.APPLICATION_JSON)
                            .put(Entity.text(""));

                    if (stockResp.getStatus() >= 400) {
                        String body = "";
                        try { body = stockResp.readEntity(String.class); } catch (Exception ignore) {}
                        return Response.status(400).entity(Map.of(
                                "error", "Stock insuficiente o producto no existe",
                                "productoId", id,
                                "status", stockResp.getStatus(),
                                "respuesta", body
                        )).build();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(502).entity(Map.of(
                        "error", "No se pudo descontar stock (ms-productos).",
                        "detalle", String.valueOf(e.getMessage())
                )).build();
            }

            // 3) Guardar orden en Postgres
            try {
                em.getTransaction().begin();

                String productosJson = jsonb.toJson(req.productos);
                Orden orden = new Orden(req.destino, productosJson, envioResp.costo_envio);

                em.persist(orden);
                em.getTransaction().commit();

                // 4) Respuesta final (201)
                Map<String, Object> resp = new HashMap<>();
                resp.put("id", orden.getId());
                resp.put("destino", orden.getDestino());
                resp.put("productos", req.productos);
                resp.put("costo_envio", orden.getCostoEnvio());
                resp.put("creada_en", orden.getCreadaEn().toString());

                return Response.status(201).entity(resp).build();

            } catch (Exception e) {
                e.printStackTrace();
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                return Response.status(500).entity(Map.of(
                        "error", "No se pudo guardar la orden.",
                        "detalle", String.valueOf(e.getMessage())
                )).build();
            }

        } finally {
            try { em.close(); } catch (Exception ignore) {}
            try { client.close(); } catch (Exception ignore) {}
        }
    }
}

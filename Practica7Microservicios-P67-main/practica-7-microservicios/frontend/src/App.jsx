import { useEffect, useMemo, useState } from "react";
import {
  getHealthGateway,
  getHealthProductos,
  getHealthEnvios,
  getHealthOrdenes,
  getProductos,
  crearOrden
} from "./api";

const CIUDADES = ["Cuenca", "Quito", "Guayaquil"];

function App() {
  const [output, setOutput] = useState("");
  const [productosDB, setProductosDB] = useState([]);
  const [seleccionados, setSeleccionados] = useState(new Set());

  // formulario
  const [nombre, setNombre] = useState("");
  const [ciudadIndex, setCiudadIndex] = useState(0);

  const destino = CIUDADES[ciudadIndex];

  const show = (data) => setOutput(JSON.stringify(data, null, 2));

  const run = async (fn) => {
    try {
      const data = await fn();
      show(data);
      return data;
    } catch (e) {
      // intenta parsear error JSON si viene como string
      try {
        show(JSON.parse(e.message));
      } catch {
        show({ error: String(e.message || e) });
      }
      return null;
    }
  };

  const idsSeleccionados = useMemo(
    () => Array.from(seleccionados),
    [seleccionados]
  );

  const toggleProducto = (id) => {
    setSeleccionados((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const cargarProductos = async () => {
    const data = await run(() => getProductos());
    if (Array.isArray(data)) setProductosDB(data);
  };

  useEffect(() => {
    cargarProductos();
  }, []);

  const comprar = async () => {
    if (!nombre.trim()) {
      show({ error: "Escribe el nombre de la persona." });
      return;
    }
    if (idsSeleccionados.length === 0) {
      show({ error: "Selecciona al menos un producto para comprar." });
      return;
    }

    const resp = await run(() => crearOrden(destino, idsSeleccionados, nombre.trim()));
    // Si fue éxito (201), resp debe traer costo_envio
    // Lo mostramos en output y además una forma visible
    if (resp && typeof resp.costo_envio !== "undefined") {
      show({
        mensaje: "Orden creada exitosamente",
        costo_envio: resp.costo_envio,
        respuesta: resp
      });
    }
  };

  return (
    <div style={{ padding: 20, maxWidth: 900 }}>
      <h1>Práctica 7 - Microservicios</h1>
      <p>Gateway: {import.meta.env.VITE_API_URL}</p>

      {/* HEALTH */}
      <h2>Health</h2>
      <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
        <button onClick={() => run(() => getHealthGateway())}>Gateway</button>
        <button onClick={() => run(() => getHealthProductos())}>Productos</button>
        <button onClick={() => run(() => getHealthEnvios())}>Envíos</button>
        <button onClick={() => run(() => getHealthOrdenes())}>Órdenes</button>
      </div>

      {/* LISTADO PRODUCTOS */}
      <h2 style={{ marginTop: 24 }}>Productos (desde la BD)</h2>
      <button onClick={cargarProductos}>Actualizar lista</button>

      <div style={{ marginTop: 10 }}>
        {productosDB.length === 0 ? (
          <p>No hay productos cargados.</p>
        ) : (
          <div style={{ display: "grid", gap: 10 }}>
            {productosDB.map((p) => (
              <div
                key={p.id}
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  padding: 12,
                  border: "1px solid #444",
                  borderRadius: 10
                }}
              >
                <label style={{ display: "flex", gap: 10, alignItems: "center" }}>
                  <input
                    type="checkbox"
                    checked={seleccionados.has(p.id)}
                    onChange={() => toggleProducto(p.id)}
                  />
                  <div>
                    <div style={{ fontWeight: 700 }}>
                      {p.nombre} <span style={{ opacity: 0.7 }}>(ID: {p.id})</span>
                    </div>
                    <div style={{ opacity: 0.8 }}>
                      Precio: ${p.precio} — Stock: {p.stock}
                    </div>
                  </div>
                </label>

                <div style={{ fontSize: 12, opacity: 0.8 }}>
                  {seleccionados.has(p.id) ? "✅ Seleccionado" : ""}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* FORMULARIO DE COMPRA */}
      <h2 style={{ marginTop: 28 }}>Comprar (Crear orden)</h2>

      <div
        style={{
          marginTop: 10,
          padding: 14,
          border: "1px solid #444",
          borderRadius: 12,
          display: "grid",
          gap: 12,
          maxWidth: 600
        }}
      >
        {/* Nombre */}
        <div style={{ display: "grid", gap: 6 }}>
          <label style={{ fontWeight: 700 }}>Nombre de la persona</label>
          <input
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="Ej: Ariel Solano"
          />
        </div>

        {/*  ciudades */}
        <div style={{ display: "grid", gap: 6 }}>
          <label style={{ fontWeight: 700 }}>Ciudad de envío</label>

          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <input
              type="range"
              min="0"
              max={CIUDADES.length - 1}
              value={ciudadIndex}
              onChange={(e) => setCiudadIndex(parseInt(e.target.value))}
            />
            <div style={{ fontWeight: 700 }}>{destino}</div>
          </div>

          <div style={{ fontSize: 12, opacity: 0.8 }}>
            Ciudades: {CIUDADES.join(" • ")}
          </div>
        </div>

        {/* Resumen selección */}
        <div style={{ fontSize: 13, opacity: 0.9 }}>
          <strong>Productos seleccionados:</strong>{" "}
          {idsSeleccionados.length > 0 ? idsSeleccionados.join(", ") : "ninguno"}
        </div>

        <button onClick={comprar} style={{ padding: "10px 12px", fontWeight: 800 }}>
          Comprar
        </button>
      </div>

      {/* OUTPUT */}
      <pre style={{ marginTop: 20, whiteSpace: "pre-wrap" }}>{output}</pre>
    </div>
  );
}

export default App;

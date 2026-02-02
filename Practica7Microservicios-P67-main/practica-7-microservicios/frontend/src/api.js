const API_BASE = import.meta.env.VITE_API_URL;

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || "Error en la petición");
  }
  const ct = res.headers.get("content-type") || "";
  if (ct.includes("application/json")) return res.json();
  const text = await res.text();
  return { message: text };
}
// Health
export const getHealthGateway = async () => {
  const res = await fetch(`${API_BASE}/health`);
  if (!res.ok) throw new Error(await res.text());
  const ct = res.headers.get("content-type") || "";
  if (ct.includes("application/json")) return res.json();
  const text = await res.text();
  return { estado: "ok", servicio: "gateway", message: text };
};
export const getHealthProductos = () => request("/api/productos/health");
export const getHealthEnvios = () => request("/api/envios/health");
export const getHealthOrdenes = () => request("/api/ordenes/health");
// Productos
export const getProductos = () => request("/api/productos/products");
// Órdenes (flujo principal)
export const crearOrden = (destino, productos, nombre) =>
  request("/api/ordenes/orders", {
    method: "POST",
    body: JSON.stringify({ destino, productos, nombre })
  });

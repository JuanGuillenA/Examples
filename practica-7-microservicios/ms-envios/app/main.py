from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import List

app = FastAPI(
    title="Microservicio de Envíos",
    description="Calcula el costo de envío de una orden de forma simple (stateless).",
    version="1.0.0"
)
# Modelos
class SolicitudEnvio(BaseModel):
    productos: List[int] = Field(..., description="Lista de IDs de productos")
    destino: str = Field(..., description="Ciudad o destino de entrega")

class RespuestaEnvio(BaseModel):
    destino: str
    cantidad_productos: int
    costo_envio: float

# Endpoints de salud
@app.get("/health")
def health():
    return {"estado": "ok", "servicio": "ms-envios"}

# Endpoint principal
@app.post("/shipping/calculate", response_model=RespuestaEnvio)
def calcular_envio(payload: SolicitudEnvio):
    """
    Algoritmo simple:
    - Costo base: 2.50
    - +1.25 por cada producto
    - Ajuste por destino (solo como ejemplo)
    """
    costo_base = 2.50
    por_producto = 1.25 * len(payload.productos)

    destino_lower = payload.destino.strip().lower()
    ajuste_destino = 0.0

    if destino_lower in ["cuenca", "quito", "guayaquil"]:
        ajuste_destino = 1.00
    else:
        ajuste_destino = 2.00

    total = round(costo_base + por_producto + ajuste_destino, 2)

    return RespuestaEnvio(
        destino=payload.destino,
        cantidad_productos=len(payload.productos),
        costo_envio=total
    )

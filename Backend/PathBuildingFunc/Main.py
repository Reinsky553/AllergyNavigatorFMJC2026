import osmnx as ox
import networkx as nx
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
from shapely.geometry import shape, Point
import uvicorn
from fastapi.staticfiles import StaticFiles

# Импортируем наши (не)придуманные данные
from data import TREES_GEOJSON, POLLEN_ZONES
from landmarks import get_all_landmark_coords

app = FastAPI()
app.mount("/static", StaticFiles(directory="static"), name="static")

# Загружаем граф Кемерово (пешеходные дорожки)
# При первом запуске скачается из интернета, затем будет в кэше
try:
    G = ox.load_graphml("kemerovo.graphml")
except:
    G = ox.graph_from_place("Kemerovo, Russia", network_type="walk")
    ox.save_graphml(G, "kemerovo.graphml")


class RouteRequest(BaseModel):
    start_coords: List[float]  # [lat, lon]
    end_coords: List[float]
    sensitivity: float  # 0.0 (ты не аллергик (зачем?)) -> 10.0 (зачем ты выходишь на улицу?)


@app.post("/get_route")
async def get_route(req: RouteRequest):
    try:
        # Находим ближайшие узлы графа к точкам старта и финиша
        orig_node = ox.nearest_nodes(G, req.start_coords[1], req.start_coords[0])
        dest_node = ox.nearest_nodes(G, req.end_coords[1], req.end_coords[0])

        def custom_weight(u, v, data):
            # 1. Базовая длина дороги в метрах
            length = data.get('length', 1)

            # Находим координаты центра отрезка дороги
            node_u, node_v = G.nodes[u], G.nodes[v]
            mid_lat = (node_u['y'] + node_v['y']) / 2
            mid_lon = (node_u['x'] + node_v['x']) / 2
            mid_point = Point(mid_lon, mid_lat)

            # 2. Штраф за деревья
            tree_penalty = 0
            for feature in TREES_GEOJSON['features']:
                tree_pos = shape(feature['geometry'])
                if mid_point.distance(tree_pos) < 0.0005:  # ~50 метров
                    tree_penalty += 2.0  # Нашли дерево — добавили тяжести пути

            # 3. Штраф за пыльцу
            pollen_penalty = 0
            for zone in POLLEN_ZONES:
                lat_min, lon_min, lat_max, lon_max = zone['bbox']
                if lat_min <= mid_lat <= lat_max and lon_min <= mid_lon <= lon_max:
                    pollen_penalty = zone['level']
                    break

            # Итоговая формула: Длина * (1 + (Деревья + Пыльца) * Чувствительность)
            return length * (1 + (tree_penalty + pollen_penalty) * req.sensitivity)

        # Алгоритм Дейкстры
        route_nodes = nx.shortest_path(G, orig_node, dest_node, weight=custom_weight)

        # Собираем координаты
        route_coords = [[G.nodes[n]['y'], G.nodes[n]['x']] for n in route_nodes]

        return {
            "status": "success",
            "route": route_coords,
            "total_nodes": len(route_nodes)
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/get_tour_route")
async def get_tour_route(req: RouteRequest):
    try:
        landmarks = get_all_landmark_coords()
        if not landmarks:
            raise HTTPException(status_code=404, detail="No landmarks found")

        full_route_coords = []
        current_start = [req.start_coords[1], req.start_coords[0]]

        points_to_visit = landmarks.copy()

        last_node = None

        for destination in points_to_visit:
            orig_node = ox.nearest_nodes(G, current_start[0], current_start[1])
            dest_node = ox.nearest_nodes(G, destination[1], destination[0])

            route_segment = nx.shortest_path(G, orig_node, dest_node, weight='length')

            segment_coords = [[G.nodes[n]['y'], G.nodes[n]['x']] for n in route_segment]

            if full_route_coords:
                full_route_coords.extend(segment_coords[1:])
            else:
                full_route_coords.extend(segment_coords)

            current_start = [destination[1], destination[0]]

        from landmarks import NOTABLE_PLACES

        return {
            "status": "success",
            "route": full_route_coords,
            "landmarks": [
                {
                    "name": p["name"],
                    "lat": p["lat"],
                    "lon": p["lon"],
                    "image_url": f"http://127.0.0.1:8000/static/{p['name'].replace(' ', '_').lower()}.jpg"
                } for p in NOTABLE_PLACES
            ]
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
# Статические данные: Деревья
TREES_GEOJSON = {
    "type": "FeatureCollection",
    "features": [
        {"type": "Feature", "properties": {"тип": "Береза"}, "geometry": {"type": "Point", "coordinates": [86.0872, 55.3551]}},
        {"type": "Feature", "properties": {"тип": "Береза"}, "geometry": {"type": "Point", "coordinates": [86.0880, 55.3560]}},
        {"type": "Feature", "properties": {"тип": "Дуб"}, "geometry": {"type": "Point", "coordinates": [86.0910, 55.3580]}}
    ]
}

# Динамические данные: Уровни пыльцы (Сетка/Районы)
# Ключ: (lat_min, lon_min, lat_max, lon_max), Значение: уровень пыльцы 0.0 - 5.0
POLLEN_ZONES = [
    {"bbox": (55.354, 86.085, 55.357, 86.090), "level": 4.5}, # "Красная зона" пыльцы
    {"bbox": (55.340, 86.100, 55.350, 86.110), "level": 1.2}  # Чистая зона
]
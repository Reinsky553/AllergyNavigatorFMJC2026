from typing import List, Dict

#Kemerovo
NOTABLE_PLACES = [
    {"name": "Театр Драмы", "lat": 55.3551, "lon": 86.0796, "image_filename": "drama_theatre.jpg"},
    {"name": "Монумент Шахтерам", "lat": 55.3444, "lon": 86.0883, "image_filename": "monument_miners.jpg"},
    {"name": "Парк победы", "lat": 55.3595, "lon": 86.1132, "image_filename": "victory_park.jpg"},
    {"name": "Театр музыки", "lat": 55.3540, "lon": 86.0850}
]

def get_all_landmark_coords() -> List[List[float]]:
    """Returns a list of [lat, lon] for all manual landmarks."""
    return [[p["lat"], p["lon"]] for p in NOTABLE_PLACES]
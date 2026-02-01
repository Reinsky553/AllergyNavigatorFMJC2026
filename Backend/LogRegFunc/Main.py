import sqlite3
from flask import Flask, request, jsonify

app = Flask(__name__)


def init_db():
    conn = sqlite3.connect('users.db')
    cursor = conn.cursor()
    # Таблица пользователей
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL
        )
    ''')
    # ТАБЛИЦА: Аллергии (связана с пользователем по username)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS user_allergies (
            username TEXT NOT NULL,
            allergy_name TEXT NOT NULL,
            PRIMARY KEY (username, allergy_name)
        )
    ''')
    conn.commit()
    conn.close()


# --- Регистрация ---
@app.route('/api/auth/register', methods=['POST'])
def register():
    data = request.get_json()
    username = data.get('username')
    password = data.get('pass')  # Используем 'pass' как в Kotlin

    conn = sqlite3.connect('users.db')
    cursor = conn.cursor()
    try:
        cursor.execute("INSERT INTO users (username, password) VALUES (?, ?)", (username, password))
        conn.commit()
        return jsonify({"message": "Success"}), 201
    except sqlite3.IntegrityError:
        return jsonify({"message": "User already exists"}), 400
    finally:
        conn.close()


# --- Логин ---
@app.route('/api/auth/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('pass')  # Типо хешируется

    conn = sqlite3.connect('users.db')
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM users WHERE username = ? AND password = ?", (username, password))
    user = cursor.fetchone()
    conn.close()

    if user:
        # В реальном проекте здесь генерируется JWT Token
        return jsonify({"token": "fake-jwt-token", "username": username}), 200
    else:
        return jsonify({"message": "Invalid credentials"}), 401


# --- СИНХРОНИЗАЦИЯ АЛЛЕРГИЙ ---
@app.route('/api/user/allergies', methods=['POST'])
def sync_allergies():
    # В реальности мы бы проверяли токен
    token = request.headers.get('Authorization')
    data = request.get_json()
    allergies = data.get('allergies', [])

    username = "test_user"

    conn = sqlite3.connect('users.db')
    cursor = conn.cursor()
    try:
        # 1. Удаляем старые аллергии пользователя
        cursor.execute("DELETE FROM user_allergies WHERE username = ?", (username,))
        # 2. Вставляем новый список
        for allergy in allergies:
            cursor.execute("INSERT INTO user_allergies (username, allergy_name) VALUES (?, ?)",
                           (username, allergy))
        conn.commit()
        return jsonify({"message": "Synced"}), 200
    except Exception as e:
        return jsonify({"message": str(e)}), 500
    finally:
        conn.close()


# --- ПОЛУЧЕНИЕ АЛЛЕРГИЙ ---
@app.route('/api/user/allergies', methods=['GET'])
def get_allergies():
    username = "test_user"
    conn = sqlite3.connect('users.db')
    cursor = conn.cursor()
    cursor.execute("SELECT allergy_name FROM user_allergies WHERE username = ?", (username,))
    allergies = [row[0] for row in cursor.fetchall()]
    conn.close()
    return jsonify({"allergies": allergies}), 200


if __name__ == '__main__':
    init_db()
    app.run(host='0.0.0.0', port=5000, debug=True)
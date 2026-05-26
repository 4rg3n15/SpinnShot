"""SpinnShot backend integration tests.

Covers: health, categories, questions (+filter), games CRUD, leaderboard aggregation.
Hits the public REACT_APP_BACKEND_URL like a real client.
"""
from __future__ import annotations

import os
from pathlib import Path

import pytest
import requests
from dotenv import load_dotenv

load_dotenv(Path(__file__).resolve().parents[2] / "frontend" / ".env")

BASE_URL = os.environ["REACT_APP_BACKEND_URL"].rstrip("/")
API = f"{BASE_URL}/api"

# Expected categories per /app/backend/questions.csv (9 total)
EXPECTED_CATEGORIES = {
    "Biología", "Cine", "Cultura Colombiana", "Cómics", "Deportes",
    "Geografía", "Historia", "Series", "Videojuegos",
}


# ---------- Fixtures ----------
@pytest.fixture(scope="session")
def client() -> requests.Session:
    s = requests.Session()
    s.headers.update({"Content-Type": "application/json"})
    return s


@pytest.fixture
def sample_game_a() -> dict:
    return {
        "mode": "shot_o_reto",
        "rounds": 3,
        "categories": ["Cine", "Música"],
        "players": [
            {"name": "TEST_Ana", "points": 7, "shots": 1},
            {"name": "TEST_Luis", "points": 4, "shots": 2},
            {"name": "TEST_Sofi", "points": 5, "shots": 0},
        ],
        "winner": "TEST_Ana",
    }


@pytest.fixture
def sample_game_b() -> dict:
    return {
        "mode": "verdad_o_reto",
        "rounds": 4,
        "categories": ["Historia"],
        "players": [
            {"name": "TEST_Ana", "points": 3, "shots": 0},
            {"name": "TEST_Luis", "points": 8, "shots": 0},
            {"name": "TEST_Sofi", "points": 2, "shots": 0},
        ],
        "winner": "TEST_Luis",
    }


# ---------- /api/health ----------
class TestHealth:
    def test_health_ok(self, client):
        r = client.get(f"{API}/health", timeout=15)
        assert r.status_code == 200
        body = r.json()
        assert body == {"status": "ok", "service": "spinnshot"}


# ---------- /api/categories ----------
class TestCategories:
    def test_categories_sorted_and_expected(self, client):
        r = client.get(f"{API}/categories", timeout=15)
        assert r.status_code == 200
        cats = r.json()
        assert isinstance(cats, list)
        assert len(cats) == 9, f"Expected 9 categories, got {len(cats)}: {cats}"
        assert set(cats) == EXPECTED_CATEGORIES, f"Mismatch: {set(cats) ^ EXPECTED_CATEGORIES}"
        assert cats == sorted(cats), "Categories must be sorted"


# ---------- /api/questions ----------
class TestQuestions:
    def test_questions_total_at_least_50(self, client):
        r = client.get(f"{API}/questions", timeout=15)
        assert r.status_code == 200
        qs = r.json()
        assert isinstance(qs, list)
        assert len(qs) >= 50, f"Expected >=50 questions, got {len(qs)}"
        # validate shape
        for q in qs[:5]:
            assert {"categoria", "pregunta", "respuesta"} <= q.keys()

    def test_questions_filter_by_categoria_cine(self, client):
        r = client.get(f"{API}/questions", params={"categoria": "Cine"}, timeout=15)
        assert r.status_code == 200
        qs = r.json()
        assert len(qs) > 0, "Expected at least 1 Cine question"
        assert all(q["categoria"] == "Cine" for q in qs)

    def test_questions_filter_todas_returns_all(self, client):
        r_all = client.get(f"{API}/questions", timeout=15).json()
        r_todas = client.get(f"{API}/questions", params={"categoria": "todas"}, timeout=15).json()
        assert len(r_todas) == len(r_all)

    def test_questions_cache_is_consistent_across_calls(self, client):
        """Two consecutive requests must return identical lists when CSV is unchanged."""
        first = client.get(f"{API}/questions", timeout=15).json()
        second = client.get(f"{API}/questions", timeout=15).json()
        assert first == second

    def test_questions_reload_endpoint(self, client):
        r = client.post(f"{API}/questions/reload", timeout=15)
        assert r.status_code == 200, r.text
        body = r.json()
        assert body["status"] == "ok"
        assert body["count"] >= 50
        # Cached count must equal what /api/questions returns
        qs = client.get(f"{API}/questions", timeout=15).json()
        assert body["count"] == len(qs)


# ---------- /api/games + /api/leaderboard ----------
class TestGamesAndLeaderboard:
    created_ids: list[str] = []

    def test_create_game_returns_id_and_uid(self, client, sample_game_a):
        r = client.post(f"{API}/games", json=sample_game_a, timeout=15)
        assert r.status_code == 200, r.text
        body = r.json()
        assert "id" in body and "uid" in body
        assert isinstance(body["id"], str) and len(body["id"]) == 24
        assert isinstance(body["uid"], str) and len(body["uid"]) >= 32
        TestGamesAndLeaderboard.created_ids.append(body["id"])

    def test_create_second_game_and_history_persists(self, client, sample_game_b):
        r = client.post(f"{API}/games", json=sample_game_b, timeout=15)
        assert r.status_code == 200
        TestGamesAndLeaderboard.created_ids.append(r.json()["id"])

        # GET /api/games should now include both, with iso played_at strings
        r2 = client.get(f"{API}/games", timeout=15)
        assert r2.status_code == 200
        games = r2.json()
        ids = {g["_id"] for g in games}
        for gid in TestGamesAndLeaderboard.created_ids:
            assert gid in ids, f"Created game {gid} missing from /api/games"
        # iso format check
        for g in games:
            if g["_id"] in TestGamesAndLeaderboard.created_ids:
                assert isinstance(g["played_at"], str)
                # iso8601 has 'T'
                assert "T" in g["played_at"]

    # ---- Leaderboard aggregation, split per player to keep each test simple ----
    # Expected totals after the two games inserted in test_create_two_games:
    #   TEST_Ana : 2 games, points 7+3=10, shots 1+0=1, wins 1
    #   TEST_Luis: 2 games, points 4+8=12, shots 2+0=2, wins 1
    #   TEST_Sofi: 2 games, 0 wins
    EXPECTED_LEADERBOARD = {
        "TEST_Ana": {"games": 2, "points": 10, "shots": 1, "wins": 1},
        "TEST_Luis": {"games": 2, "points": 12, "shots": 2, "wins": 1},
        "TEST_Sofi": {"games": 2, "wins": 0},
    }

    def _get_leaderboard_rows(self, client):
        r = client.get(f"{API}/leaderboard", timeout=15)
        assert r.status_code == 200
        return {row["name"]: row for row in r.json()}

    def _assert_player_totals(self, rows, name, expected):
        assert name in rows, f"{name} not in leaderboard: {list(rows)}"
        actual = rows[name]
        for key, value in expected.items():
            assert actual[key] == value, (
                f"{name}.{key} = {actual[key]}, expected {value}"
            )

    def test_leaderboard_contains_ana(self, client):
        rows = self._get_leaderboard_rows(client)
        self._assert_player_totals(rows, "TEST_Ana", self.EXPECTED_LEADERBOARD["TEST_Ana"])

    def test_leaderboard_contains_luis(self, client):
        rows = self._get_leaderboard_rows(client)
        self._assert_player_totals(rows, "TEST_Luis", self.EXPECTED_LEADERBOARD["TEST_Luis"])

    def test_leaderboard_contains_sofi(self, client):
        rows = self._get_leaderboard_rows(client)
        self._assert_player_totals(rows, "TEST_Sofi", self.EXPECTED_LEADERBOARD["TEST_Sofi"])

    def test_delete_game_invalid_id_returns_400(self, client):
        r = client.delete(f"{API}/games/not-a-real-id", timeout=15)
        assert r.status_code == 400

    def test_delete_game_unknown_returns_404(self, client):
        # valid-looking ObjectId that doesn't exist
        r = client.delete(f"{API}/games/507f1f77bcf86cd799439011", timeout=15)
        assert r.status_code == 404

    def test_zz_cleanup_delete_created_games(self, client):
        """Cleanup: delete all created TEST_ games."""
        assert len(TestGamesAndLeaderboard.created_ids) >= 2
        for gid in TestGamesAndLeaderboard.created_ids:
            r = client.delete(f"{API}/games/{gid}", timeout=15)
            assert r.status_code == 200
            assert r.json() == {"deleted": True}
        # verify gone
        games = client.get(f"{API}/games", timeout=15).json()
        ids = {g["_id"] for g in games}
        for gid in TestGamesAndLeaderboard.created_ids:
            assert gid not in ids

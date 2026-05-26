"""SpinnShot backend - FastAPI + MongoDB.

Provides endpoints for:
 - serving the canonical questions list (so the Android app can refresh remotely)
 - persisting finished games + retrieving history / leaderboard
"""
from __future__ import annotations

import csv
import os
import uuid
from datetime import datetime, timezone
from pathlib import Path
from typing import Annotated, Any

from bson import ObjectId
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from motor.motor_asyncio import AsyncIOMotorClient
from pydantic import BaseModel, BeforeValidator, ConfigDict, Field

ROOT = Path(__file__).resolve().parent
load_dotenv(ROOT / ".env")

MONGO_URL = os.environ["MONGO_URL"]
DB_NAME = os.environ["DB_NAME"]

client = AsyncIOMotorClient(MONGO_URL)
db = client[DB_NAME]

app = FastAPI(title="SpinnShot API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ---------- Mongo helpers ----------
def _coerce_id(v: Any) -> str:
    if isinstance(v, ObjectId):
        return str(v)
    return str(v)


PyObjectId = Annotated[str, BeforeValidator(_coerce_id)]


# ---------- Models ----------
class Question(BaseModel):
    categoria: str
    pregunta: str
    respuesta: str


class PlayerScore(BaseModel):
    name: str
    points: int
    shots: int


class GameRecordIn(BaseModel):
    mode: str
    rounds: int
    categories: list[str]
    players: list[PlayerScore]
    winner: str
    played_at: datetime | None = None


class GameRecord(BaseModel):
    model_config = ConfigDict(populate_by_name=True)
    id: PyObjectId = Field(alias="_id")
    mode: str
    rounds: int
    categories: list[str]
    players: list[PlayerScore]
    winner: str
    played_at: datetime


# ---------- CSV loader ----------
QUESTIONS_CSV = ROOT / "questions.csv"


def _load_csv_questions() -> list[Question]:
    if not QUESTIONS_CSV.exists():
        return []
    out: list[Question] = []
    with QUESTIONS_CSV.open(encoding="utf-8") as fh:
        reader = csv.DictReader(fh)
        for row in reader:
            try:
                out.append(
                    Question(
                        categoria=row["categoria"].strip(),
                        pregunta=row["pregunta"].strip(),
                        respuesta=row["respuesta"].strip(),
                    )
                )
            except KeyError:
                continue
    return out


# ---------- Routes ----------
@app.get("/api/health")
async def health() -> dict[str, str]:
    return {"status": "ok", "service": "spinnshot"}


@app.get("/api/questions", response_model=list[Question])
async def list_questions(
    categoria: str | None = Query(default=None, description="Filtra por categoría"),
) -> list[Question]:
    questions = _load_csv_questions()
    if categoria and categoria.lower() != "todas":
        questions = [q for q in questions if q.categoria.lower() == categoria.lower()]
    return questions


@app.get("/api/categories", response_model=list[str])
async def list_categories() -> list[str]:
    return sorted({q.categoria for q in _load_csv_questions()})


@app.post("/api/games", response_model=dict)
async def save_game(record: GameRecordIn) -> dict[str, str]:
    doc = record.model_dump()
    doc["played_at"] = doc.get("played_at") or datetime.now(timezone.utc)
    doc["uid"] = str(uuid.uuid4())
    result = await db.games.insert_one(doc)
    return {"id": str(result.inserted_id), "uid": doc["uid"]}


@app.get("/api/games", response_model=list[dict])
async def list_games(limit: int = 50) -> list[dict]:
    cursor = db.games.find().sort("played_at", -1).limit(limit)
    out: list[dict] = []
    async for doc in cursor:
        doc["_id"] = str(doc["_id"])
        if isinstance(doc.get("played_at"), datetime):
            doc["played_at"] = doc["played_at"].isoformat()
        out.append(doc)
    return out


@app.get("/api/leaderboard", response_model=list[dict])
async def leaderboard(limit: int = 10) -> list[dict]:
    pipeline = [
        {"$unwind": "$players"},
        {
            "$group": {
                "_id": "$players.name",
                "games": {"$sum": 1},
                "points": {"$sum": "$players.points"},
                "shots": {"$sum": "$players.shots"},
                "wins": {
                    "$sum": {"$cond": [{"$eq": ["$players.name", "$winner"]}, 1, 0]}
                },
            }
        },
        {"$sort": {"wins": -1, "points": -1}},
        {"$limit": limit},
        {
            "$project": {
                "name": "$_id",
                "_id": 0,
                "games": 1,
                "points": 1,
                "shots": 1,
                "wins": 1,
            }
        },
    ]
    return [doc async for doc in db.games.aggregate(pipeline)]


@app.delete("/api/games/{game_id}")
async def delete_game(game_id: str) -> dict[str, bool]:
    try:
        oid = ObjectId(game_id)
    except Exception as exc:
        raise HTTPException(status_code=400, detail="invalid id") from exc
    res = await db.games.delete_one({"_id": oid})
    if res.deleted_count == 0:
        raise HTTPException(status_code=404, detail="not found")
    return {"deleted": True}

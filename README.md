# SpinnShot 🎲🍸

App social para fiestas, escrita en **Kotlin + Jetpack Compose**. Ruleta de
jugadores, preguntas de cultura general por categorías, tres modos de juego
con puntos y shots, ranking final y un menú para editar la partida sobre la
marcha.

> Este repo incluye además un **backend FastAPI + MongoDB** y un
> **companion web (React)** que muestra ranking e historial.

## Estructura

```
app/                 Módulo Android (Kotlin + Jetpack Compose)
 └─ src/main/java/com/example/spinnshot/
    ├─ data/        Modelos (Player, Question, GameMode, GameState) + repo CSV + Retrofit
    ├─ logic/       GameEngine, ScoreCalculator, TurnManager, WinnerResolver
    └─ ui/          onboarding, agevalidation, setup, game, question, result, theme, components
 └─ src/main/assets/questions.csv   Banco con 50+ preguntas
 └─ src/test/...    Pruebas JVM de la lógica
backend/             FastAPI + MongoDB (historial + leaderboard)
frontend/            React landing/companion web
```

## Cómo correr la app Android

1. Requiere **Android Studio Hedgehog/Iguana+**, JDK 17, Android SDK 34 y un
   dispositivo o emulador con **Android 8.0 (API 26)+**.
2. Abre `/app` como proyecto Gradle. El módulo principal es `:app`.
3. (Opcional) Para apuntar a un backend propio configura
   `./gradlew :app:assembleDebug -PapiBaseUrl="https://my.api/"`.
4. Ejecuta `./gradlew :app:assembleDebug` o pulsa **Run** desde Android
   Studio. El `MainActivity` levanta el grafo de navegación de Compose.
5. Pruebas unitarias: `./gradlew :app:test`.

## Backend (FastAPI)

```bash
cd backend
pip install -r requirements.txt
uvicorn server:app --reload
```

Endpoints principales:

- `GET  /api/health` – healthcheck
- `GET  /api/categories` – categorías derivadas del CSV
- `GET  /api/questions?categoria=X` – banco de preguntas filtrado
- `POST /api/games` – guarda el resultado de una partida
- `GET  /api/games` – historial reciente
- `GET  /api/leaderboard` – ranking acumulado

## Companion web

```bash
cd frontend
yarn
yarn start
```

Muestra ranking, historial y guía de build/uso. Tema nocturno morado/neón.

## Modos de juego

| Modo            | Acierto              | Fallo / Shot               | Fallo / Otro                            |
| --------------- | -------------------- | -------------------------- | --------------------------------------- |
| Shot o Reto     | +1 pt, −1 shot       | −1 pt, +1 shot             | Reto: cumple −1/−1, no cumple −2/+2     |
| Verdad o Reto   | +1 pt                | Verdad cumple −2 / no −2   | Reto cumple −1 / no cumple −3           |
| Verdad o Shot   | +1 pt, −1 shot       | −1 pt, +1 shot             | Verdad cumple −1/−1, no cumple −2/+2    |

Empate:
- En *Shot o Reto* y *Verdad o Shot*: gana quien tenga **menos shots**.
- En *Verdad o Reto*: se dispara una **ronda de desempate** manual.

## Validación de edad

La pantalla `AgeValidationScreen` calcula la edad real (no la guarda) y, si
el usuario es menor de 18, bloquea los modos con alcohol tanto en la
configuración inicial como dentro del menú de edición.

## Licencia

Uso académico/demo.

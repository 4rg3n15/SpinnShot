# SpinnShot — PRD

## Original problem statement
App social para fiestas en Kotlin / Jetpack Compose: ruleta de jugadores,
preguntas de cultura general por categorías, 3 modos (Shot o Reto, Verdad o
Reto, Verdad o Shot) con puntos y shots, ranking final con desempates por
modo, edición de partida en vivo y validación de edad bloqueando los modos
con alcohol para menores. Mínimo 50 preguntas en CSV.

## User choices (Jan 2026)
- Implementación: **Kotlin / Jetpack Compose** (código fuente, no ejecutable
  en el entorno preview).
- Persistencia: **Backend FastAPI + MongoDB** (historial + leaderboard).
- Entrega: **end-to-end** de una sola vez (no commits separados aquí).
- Tipografía: **Helvetica** (Compose + companion web).
- Edad: **bloqueo** de los modos con alcohol para menores de 18.

## User personas
- Anfitrión de fiesta con grupo de amigos.
- Jugador casual conocedor o no de cultura general.
- Posible menor de edad → flujo restringido sin alcohol.

## Architecture
- **Android**: módulo `:app` con Compose, navigation, Retrofit + kotlinx
  serialization. Estado central en `GameViewModel`. Lógica pura en
  `logic/`. Banco de preguntas en `assets/questions.csv` (55 entradas).
- **Backend**: FastAPI + Motor (MongoDB).
  Endpoints: `/api/health`, `/api/categories`, `/api/questions`,
  `/api/games` (POST/GET/DELETE), `/api/leaderboard`.
- **Companion web**: React con tema nocturno morado/neón Helvetica, muestra
  ranking y partidas recientes.

## What's been implemented (2026-01)
- 28 archivos Kotlin que cubren onboarding, validación de edad, setup
  (categorías, modo, jugadores, rondas), pantalla principal con ruleta
  animada en Canvas, menú modal de edición de juego, pantalla de pregunta
  con flashcard 3D y máquina de estados completa para los 3 modos, pantalla
  de resultado con desempate manual para Verdad o Reto.
- `GameEngine`/`ScoreCalculator`/`TurnManager`/`WinnerResolver` con tests
  JVM unitarios (`/app/app/src/test/...`).
- FastAPI con MongoDB validado por testing agent (100% backend).
- Companion web React validado por testing agent (100% frontend).
- `questions.csv` con 55 preguntas, 9 categorías, compartido entre asset
  Android y backend.

## Prioritized backlog
- **P1**: Permitir que el companion web inicie una partida (modo "remote
  controller") para facilitar pruebas sin Android Studio.
- **P1**: Cachear `/api/questions` en startup en lugar de releer CSV.
- **P2**: Banner de error global en frontend cuando el API falla.
- **P2**: Idempotencia por `uid` en `POST /api/games` (devolver 409).
- **P2**: Tie-break sort key adicional por shots en `/api/leaderboard`.
- **P3**: Animación dedicada del ganador (confeti) en `ResultScreen`.
- **P3**: i18n: extraer strings a `strings.xml`.

## Known limitations
- Android NO se compila en este entorno (sólo código fuente).
- El timer entre el spin animado y la navegación a la pregunta es fijo (3s).

## Next tasks
- Si el usuario lo pide, agregar autenticación opcional o sincronización de
  partidas en tiempo real (websockets) para múltiples teléfonos.

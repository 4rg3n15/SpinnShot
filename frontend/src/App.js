import { useState } from "react";
import useLandingData from "./hooks/useLandingData";
import "./App.css";

const GAME_MODES = [
  { key: "shot_o_reto", name: "Shot o Reto", desc: "Respondes mal: shot o reto. Con alcohol." },
  { key: "verdad_o_reto", name: "Verdad o Reto", desc: "Sin alcohol. Cumple o paga puntos." },
  { key: "verdad_o_shot", name: "Verdad o Shot", desc: "Verdad o trago al fallar." },
];

const PHONE_PLAYERS = ["Ana", "Luis", "Sofi", "Caro", "Juan", "Dani"];
const FULL_ROTATION_DEGREES = 360;

function Header() {
  return (
    <header className="header" data-testid="site-header">
      <div className="brand">
        <span className="logo-dot" />
        <span className="logo-text">SpinnShot</span>
        <span className="tag">companion · web</span>
      </div>
      <nav className="nav">
        <a href="#about" data-testid="nav-about">Concepto</a>
        <a href="#modes" data-testid="nav-modes">Modos</a>
        <a href="#leaderboard" data-testid="nav-leaderboard">Ranking</a>
        <a href="#history" data-testid="nav-history">Historial</a>
        <a href="#build" data-testid="nav-build">Build</a>
      </nav>
    </header>
  );
}

function HeroCopy() {
  return (
    <>
      <p className="kicker">KOTLIN · JETPACK COMPOSE · ESTÉTICA NOCTURNA</p>
      <h1>
        Gira la ruleta.<br/>
        Reta a tus amigos.<br/>
        <span className="hl">Sirve los shots.</span>
      </h1>
      <p className="lead">
        SpinnShot es una app móvil Android para fiestas: ruleta de jugadores,
        preguntas de cultura general por categorías, tres modos de juego
        con puntos y shots, ranking final y un menú para editar la partida
        sobre la marcha.
      </p>
      <div className="cta-row">
        <a className="btn-primary" href="#build" data-testid="cta-build">Cómo correrlo</a>
        <a className="btn-ghost" href="#modes" data-testid="cta-modes">Ver modos</a>
      </div>
    </>
  );
}

function StatStrip({ stats }) {
  return (
    <div className="stat-strip" data-testid="stat-strip">
      <div><strong>{stats.players ?? "—"}</strong><span>jugadores en historial</span></div>
      <div><strong>{stats.games ?? "—"}</strong><span>partidas guardadas</span></div>
      <div><strong>{stats.categories ?? "9"}</strong><span>categorías</span></div>
      <div><strong>{stats.questions ?? "50+"}</strong><span>preguntas</span></div>
    </div>
  );
}

function PhoneMock() {
  const step = FULL_ROTATION_DEGREES / PHONE_PLAYERS.length;
  return (
    <div className="phone-frame" data-testid="phone-mock">
      <div className="phone-screen">
        <div className="phone-top">SpinnShot</div>
        <div className="roulette">
          <div className="roulette-disk">
            {PHONE_PLAYERS.map((name, i) => (
              <span key={name} style={{ transform: `rotate(${i * step}deg) translateY(-78px)` }}>{name}</span>
            ))}
          </div>
          <div className="roulette-pin" />
        </div>
        <button className="phone-btn">GIRAR</button>
        <p className="phone-meta">Ronda 3 · Turno: Ana · Cine</p>
        <div className="phone-score">
          <span>Ana <em>+4</em></span>
          <span>Luis <em>+2</em></span>
          <span>Sofi <em>+1</em></span>
        </div>
      </div>
    </div>
  );
}

function Hero({ stats }) {
  return (
    <section className="hero" data-testid="hero">
      <div className="hero-grid">
        <div className="hero-left">
          <HeroCopy />
          <StatStrip stats={stats} />
        </div>
        <div className="hero-right">
          <PhoneMock />
        </div>
      </div>
    </section>
  );
}

function About() {
  return (
    <section className="block" id="about" data-testid="about">
      <h2>El concepto</h2>
      <div className="grid-3">
        <div className="feat" data-testid="feat-roulette">
          <h3>Ruleta de jugadores</h3>
          <p>Orden inicial aleatorio. Cada jugador toma su turno de girar y la ruleta elige a quién le toca responder, evitando seleccionarlo a sí mismo.</p>
        </div>
        <div className="feat" data-testid="feat-questions">
          <h3>Preguntas por categoría</h3>
          <p>Banco local en <code>assets/questions.csv</code> con 9 categorías. Sin repetidos hasta agotar el filtro. Sincroniza con el backend si hay internet.</p>
        </div>
        <div className="feat" data-testid="feat-rules">
          <h3>Reglas por modo</h3>
          <p>Tres modos: Shot o Reto, Verdad o Reto y Verdad o Shot. Cada uno con su propia tabla de puntos, shots y desempate.</p>
        </div>
        <div className="feat" data-testid="feat-edit">
          <h3>Edita en caliente</h3>
          <p>Menú hamburguesa para añadir jugadores, cambiar rondas, categorías o modo sin reiniciar la partida.</p>
        </div>
        <div className="feat" data-testid="feat-age">
          <h3>Validación de edad</h3>
          <p>Si la persona es menor de edad, los modos con alcohol quedan bloqueados; sólo se permite Verdad o Reto.</p>
        </div>
        <div className="feat" data-testid="feat-history">
          <h3>Historial en la nube</h3>
          <p>Al cerrar la partida la app envía el resultado al backend FastAPI + MongoDB para ranking acumulado.</p>
        </div>
      </div>
    </section>
  );
}

function Modes() {
  return (
    <section className="block" id="modes" data-testid="modes">
      <h2>Modos de juego</h2>
      <div className="grid-3">
        {GAME_MODES.map((m) => (
          <div className="mode-card" key={m.key} data-testid={`mode-${m.key}`}>
            <span className="mode-tag">{m.key.replace(/_/g, " ")}</span>
            <h3>{m.name}</h3>
            <p>{m.desc}</p>
            <ul>
              {m.key === "shot_o_reto" && (<>
                <li>Bien: +1 punto, −1 shot</li>
                <li>Mal → Shot: −1 punto, +1 shot</li>
                <li>Mal → Reto cumple: −1 punto, −1 shot</li>
                <li>Mal → Reto no cumple: −2 puntos, +2 shots</li>
                <li>Desempate por menos shots</li>
              </>)}
              {m.key === "verdad_o_reto" && (<>
                <li>Bien: +1 punto</li>
                <li>Mal → Verdad cumple: −2 puntos</li>
                <li>Mal → Reto cumple: −1 punto</li>
                <li>Reto no cumple: −3 puntos</li>
                <li>Empate → ronda de desempate</li>
              </>)}
              {m.key === "verdad_o_shot" && (<>
                <li>Bien: +1 punto, −1 shot</li>
                <li>Mal → Shot: −1 punto, +1 shot</li>
                <li>Mal → Verdad cumple: −1 punto, −1 shot</li>
                <li>Mal → Verdad no cumple: −2 puntos, +2 shots</li>
                <li>Desempate por menos shots</li>
              </>)}
            </ul>
          </div>
        ))}
      </div>
    </section>
  );
}

function Leaderboard({ rows }) {
  return (
    <section className="block" id="leaderboard" data-testid="leaderboard">
      <h2>Ranking global <span className="muted">/ vía API</span></h2>
      {rows.length === 0 ? (
        <p className="muted" data-testid="empty-leaderboard">
          Aún no hay partidas guardadas. Cuando la app Android termine una partida
          aparecerán aquí los jugadores con más victorias.
        </p>
      ) : (
        <table className="lb-table" data-testid="lb-table">
          <thead>
            <tr><th>#</th><th>Jugador</th><th>Victorias</th><th>Partidas</th><th>Puntos</th><th>Shots</th></tr>
          </thead>
          <tbody>
            {rows.map((r, i) => (
              <tr key={r.name} data-testid={`lb-row-${i}`}>
                <td>{i + 1}</td>
                <td><strong>{r.name}</strong></td>
                <td>{r.wins}</td>
                <td>{r.games}</td>
                <td>{r.points}</td>
                <td>{r.shots}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

function History({ rows }) {
  return (
    <section className="block" id="history" data-testid="history">
      <h2>Historial reciente</h2>
      {rows.length === 0 ? (
        <p className="muted" data-testid="empty-history">Todavía no hay partidas registradas.</p>
      ) : (
        <div className="hist-grid">
          {rows.map((g) => (
            <article className="hist-card" key={g._id} data-testid={`hist-${g._id}`}>
              <header>
                <span className="hist-mode">{g.mode}</span>
                <span className="hist-date">{new Date(g.played_at).toLocaleString()}</span>
              </header>
              <p className="hist-winner">🏆 {g.winner}</p>
              <ul>
                {g.players.map((p) => (
                  <li key={p.name}><span>{p.name}</span><em>{p.points} pts · {p.shots} shots</em></li>
                ))}
              </ul>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function BuildInstructions() {
  return (
    <section className="block" id="build" data-testid="build">
      <h2>Cómo correr la app Android</h2>
      <ol className="steps">
        <li><strong>Requisitos:</strong> Android Studio Hedgehog+, JDK 17, Android SDK 34, dispositivo o emulador con Android 13 (API 33).</li>
        <li>Abre <code>/app</code> como proyecto Gradle en Android Studio. El módulo principal es <code>:app</code>.</li>
        <li>(Opcional) Si tu dispositivo tiene acceso al backend, configura <code>BuildConfig.API_BASE_URL</code> con la URL pública del FastAPI. Por defecto la app usa el CSV local (<code>app/src/main/assets/questions.csv</code>).</li>
        <li>Ejecuta <code>./gradlew :app:assembleDebug</code> o presiona <em>Run</em>. La <code>MainActivity</code> arranca el grafo de navegación de Compose.</li>
        <li>Las pruebas de lógica viven en <code>app/src/test/java/com/example/spinnshot/logic/</code> — corre <code>./gradlew :app:test</code>.</li>
      </ol>
      <p className="muted">
        Esta web es un companion: muestra ranking + historial alimentados por la
        app móvil. La lógica del juego corre 100% en Kotlin/Compose.
      </p>
    </section>
  );
}

function ErrorBanner({ message, onRetry }) {
  return (
    <div className="error-banner" role="alert" data-testid="error-banner">
      <div>
        <strong>Error de conexión</strong>
        <p>{message}</p>
      </div>
      <button type="button" onClick={onRetry} data-testid="error-retry">
        Reintentar
      </button>
    </div>
  );
}

export default function App() {
  const [reloadTick, setReloadTick] = useState(0);
  const { leaders, history, stats, error } = useLandingData(reloadTick);
  const retry = () => setReloadTick((n) => n + 1);

  return (
    <div className="app" data-testid="app-root">
      <Header />
      <main>
        {error && <ErrorBanner message={error} onRetry={retry} />}
        <Hero stats={stats} />
        <About />
        <Modes />
        <Leaderboard rows={leaders} />
        <History rows={history} />
        <BuildInstructions />
      </main>
      <footer className="footer" data-testid="footer">
        SpinnShot · Hecho con Kotlin + Jetpack Compose · Backend FastAPI + MongoDB
      </footer>
    </div>
  );
}

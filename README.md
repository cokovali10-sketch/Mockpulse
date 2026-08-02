# MockPulse

A desktop inspector and chaos-control client for the **MockPulse** mock HTTP server, built with **Compose Multiplatform** and **Kotlin**.

The app connects to a running MockPulse backend over a WebSocket channel and lets you inspect live HTTP traffic, resolve requests paused at breakpoints, and inject synthetic latency, errors and drops — from a sleek aurora/glass-style desktop UI.

---

## Features

- **Live traffic inspector** — a virtualized table of every mocked HTTP exchange streamed over WebSocket.
- **Request detail drawer** — expand any single request to see method, status, latency, headers and bodies with JSON syntax highlighting.
- **Filters** — instant traffic filtering by method (`GET`/`POST`), status class (`4xx`/`5xx`) and free-text path search; one click clears all.
- **Pause / Resume** — freeze the traffic list while the mock server keeps serving.
- **Breakpoint interceptor** — when the backend pauses a request at a breakpoint, a dialog appears to **pass**, **drop** or **modify** (rewrite status + JSON body) before the server resolves it.
- **Chaos dashboard** — live drag controls for latency (fixed/range), error rate and drop rate, pushed to the backend `ChaosConfig` on the fly.
- **Route catalog** — browse every registered route, its status codes and deprecation state.
- **State & recordings** — review recorded mock exchanges and clear them.
- **Reconnect** — automatic WebSocket reconnection with backoff, plus a **Connect** dialog to point at any host/port.

---

## Screens

| Screen    | Purpose |
|-----------|-------------|
| `Traffic` | Live event table + filters (WebSocket) |
| `Chaos`   | Latency / error / drop sliders + reset |
| `Routes`  | Registered mock route catalog |
| `State`   | Recorded exchanges + clear |

---

## Requirements

- **JDK 17+** (Temurin/Eclipse Adoptium recommended; JDK 21 used in CI)
- A running **MockPulse** backend exposing `ws://<host>:<port>/__mockpulse/ws` and the `/__mockpulse/*` control-plane REST endpoints.

---

## Getting started

```bash
./gradlew build          # compile and unit-test
./gradlew run            # launch the desktop app
```

The app connects to `localhost:8080` by default; use the **Connect** button in the header to target a different MockPulse instance.

Platform packages:

```bash
./gradlew createDistributable   # -> build/compose/binaries/main/app
./gradlew packageReleaseMsi     # Windows installer (needs WiX)
./gradlew packageReleaseDmg     # macOS disk image
```

---

## Backend contract

The backend must expose:

```text
GET    /__mockpulse/health       HealthStatus
GET    /__mockpulse/routes       RouteList
GET    /__mockpulse/config       ChaosConfig
PATCH  /__mockpulse/config       partial ChaosConfig update
GET    /__mockpulse/recordings   Recordings
DELETE /__mockpulse/recordings   clear recordings
GET    /__mockpulse/ws           WebSocket protocol
```

Server → client WS envelope:

```json
{"type":"ready","data":{...}}
{"type":"traffic","data":{...}}
{"type":"breakpoint","data":{...}}
{"type":"resolved","data":{...}}
{"type":"ping","data":{}}
```

Client → server breakpoint resolution:

```json
{"type":"resolve","data":{"id":"...","decision":"modify","response":{"status":201,"headers":{},"body":{...}}}}
```

---

## Tech stack

- **Kotlin 2.2** + **Compose Multiplatform Desktop** (`org.jetbrains.compose`)
- **Ktor Client (CIO)** — REST, WebSockets, content negotiation
- **kotlinx.serialization** — typed DTOs with `@SerialName` mapping to the Go wire structures
- **Coroutines / StateFlow / SharedFlow** — reactive UI
- Clean Architecture: `ui` → `viewmodel` → `network` → `core`

### Source layout

```
src/main/kotlin/com/mockpulse/studio/
├── core/
│   ├── model/       # DTOs mirroring the backend wire protocol
│   └── util/        # formatting helpers (timestamp, bytes, latency, JSON)
├── network/         # Ktor client, WebSocket connection, event parser
├── viewmodel/       # AppViewModel, ChaosViewModel, RoutesViewModel, StateViewModel
└── ui/
    ├── theme/       # aurora palette, glass panels, typography
    ├── components/  # badges, metric cards, JSON tree viewer, dialogs
    └── screens/     # Traffic, Chaos, Routes, State
```

---

## Versions

See `build.gradle.kts`:

- Kotlin `2.2.20`
- Compose Multiplatform `1.9.0`
- Ktor `3.2.3`
- kotlinx-serialization `1.9.0`
- kotlinx-coroutines `1.10.2`

---

## License

MIT — free to use, modify and distribute. See `LICENSE`.
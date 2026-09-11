<div align="center">

# MockPulse Studio

**Real-time desktop control plane for the [MockPulse](https://github.com/cokovali10-sketch/Mockpulse) mock API server**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose_Multiplatform-1.9-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-MIT-2ea44f)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-21+-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net)

</div>

---

A sleek, aurora glassmorphism-styled desktop app that connects to a running MockPulse backend over WebSocket and lets you **inspect live HTTP traffic**, **resolve breakpoint-intercepted requests**, and **inject chaos** — latency, errors, and drops — all in real time.

> **MockPulse Studio is the GUI companion.** It requires a running [MockPulse Go server](https://github.com/cokovali10-sketch/Mockpulse) backend.

---

## Features

| Capability | Description |
|---|---|
| **Live Traffic Inspector** | Virtualized table of every mocked HTTP exchange streamed over WebSocket |
| **Request Detail Drawer** | Expand any request to see method, status, latency, headers, and body with JSON syntax highlighting |
| **Instant Filters** | Filter by HTTP method, status class (`4xx`/`5xx`), or free-text path search |
| **Pause / Resume** | Freeze the traffic list while the mock server keeps serving |
| **Breakpoint Interceptor** | When the backend pauses a request, a dialog appears to **pass**, **drop**, or **modify** the response |
| **Chaos Dashboard** | Drag controls for latency (fixed/range), error rate, and drop rate — pushed live to the backend |
| **Route Catalog** | Browse every registered mock route, its status codes, and deprecation state |
| **State & Recordings** | Review recorded mock exchanges and clear them |
| **Auto-Reconnect** | Exponential backoff WebSocket reconnection with a **Connect** dialog for any host/port |

---

## Screens

| Screen | Purpose |
|---|---|
| `Traffic` | Live event table + filters (WebSocket) |
| `Chaos` | Latency / error / drop sliders + reset |
| `Routes` | Registered mock route catalog |
| `State` | Recorded exchanges + clear |

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    MockPulse Studio                       │
│                                                          │
│  ┌──────────┐   ┌───────────┐   ┌────────────────────┐  │
│  │ UI Layer │──▶│ ViewModel │──▶│    Network Layer    │  │
│  │ Screens  │   │ (State-   │   │ Ktor Client (CIO)  │  │
│  │ Compose  │   │  Flows)   │   │ REST + WebSocket   │  │
│  └──────────┘   └───────────┘   └─────────┬──────────┘  │
│                                           │              │
└───────────────────────────────────────────┼──────────────┘
                                            │
                              ┌─────────────▼─────────────┐
                              │    MockPulse Go Backend    │
                              │  Fiber + fasthttp server   │
                              │  OpenAPI spec → mock routes│
                              │  Lua scripting + chaos     │
                              └───────────────────────────┘
```

**Data flow:** UI screens observe `StateFlow` properties from ViewModels. ViewModels subscribe to WebSocket events via `MockPulseConnection`, which handles reconnection with exponential backoff. REST calls go through `MockPulseApiClient` for health checks, route listing, chaos config, and breakpoint resolution.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2 |
| UI | Compose Multiplatform Desktop 1.9 |
| HTTP / WebSocket | Ktor Client (CIO) 3.2.3 |
| Serialization | kotlinx.serialization 1.9.0 |
| Concurrency | Coroutines + StateFlow / SharedFlow |
| Architecture | Clean Architecture: `ui` → `viewmodel` → `network` → `core` |

---

## Project Structure

```
src/main/kotlin/com/mockpulse/studio/
├── core/
│   ├── model/          # DTOs mirroring the backend wire protocol
│   └── util/           # Formatting helpers (timestamp, bytes, latency, JSON)
├── network/
│   ├── MockPulseConnection.kt   # WebSocket lifecycle + reconnection
│   ├── MockPulseApiClient.kt    # REST + WS client
│   └── WsEventParser.kt         # Typed WS event decoder
├── viewmodel/
│   ├── AppViewModel.kt          # Traffic + breakpoint orchestration
│   ├── ChaosViewModel.kt        # Chaos config controls
│   ├── RoutesViewModel.kt       # Route catalog
│   └── StateViewModel.kt        # Recordings management
└── ui/
    ├── MockPulseApp.kt          # Root composable
    ├── theme/                   # Aurora palette, glass panels, typography
    ├── components/              # StatusBadge, JsonTreeViewer, dialogs
    └── screens/                 # Traffic, Chaos, Routes, State
```

---

## Requirements

- **JDK 17+** (Temurin/Eclipse Adoptium recommended; JDK 21 used in CI)
- A running **MockPulse** backend exposing `ws://<host>:<port>/__mockpulse/ws` and the `/__mockpulse/*` control-plane REST endpoints

---

## Getting Started

```bash
./gradlew build          # compile and unit-test
./gradlew run            # launch the desktop app
```

The app connects to `localhost:8080` by default. Use the **Connect** button in the header to target a different MockPulse instance.

### Platform Packages

```bash
./gradlew createDistributable            # -> build/compose/binaries/main/app
./gradlew packageReleaseMsi              # Windows installer (needs WiX)
./gradlew packageReleaseDmg              # macOS disk image
```

---

## Backend Contract

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

### WebSocket Protocol

**Server → client** envelope:

```json
{"type":"ready","data":{"version":"0.2.0","intercept":false,"client_id":"abc"}}
{"type":"traffic","data":{"id":"abc123","method":"GET","path":"/accounts/1","status_code":200,...}}
{"type":"breakpoint","data":{"id":"bp-1","method":"POST","path":"/accounts",...}}
{"type":"resolved","data":{"id":"bp-1","decision":"pass","timed_out":false}}
{"type":"ping","data":{}}
```

**Client → server** breakpoint resolution:

```json
{
  "type": "resolve",
  "data": {
    "id": "bp-1",
    "decision": "modify",
    "response": {
      "status": 201,
      "headers": {"Content-Type": "application/json"},
      "body": {"created": true}
    }
  }
}
```

---

## Examples

### Inspect Live Traffic

1. Start MockPulse server: `./mockpulse --spec ./petstore.json --port 8080`
2. Launch Studio: `./gradlew run`
3. Click **Connect** (defaults to `localhost:8080`)
4. Send requests to the mock server — they appear instantly in the **Traffic** tab

### Intercept a Request

1. Enable breakpoints on the backend: `./mockpulse --spec ./petstore.json --intercept`
2. Send a matching request — a **Breakpoint Interceptor** dialog appears in Studio
3. Choose **Pass**, **Drop**, or **Modify** (rewrite status + JSON body)
4. The backend resolves the request with your decision

### Inject Chaos

1. Open the **Chaos** tab
2. Drag sliders for latency, error rate, or drop rate
3. Config is pushed live via `PATCH /__mockpulse/config` — no restart needed

---

## Testing

```bash
./gradlew test    # run all unit tests (parser, models, formatters)
```

Tests cover:
- `WsEventParserTest` — WebSocket event decoding (ready, traffic, breakpoint, resolved, ping, unknown)
- `ModelsTest` — DTO serialization round-trips
- `FormattersTest` — Timestamp, byte, and latency formatting

---

## Versions

| Dependency | Version |
|---|---|
| Kotlin | 2.2.20 |
| Compose Multiplatform | 1.9.0 |
| Ktor | 3.2.3 |
| kotlinx-serialization | 1.9.0 |
| kotlinx-coroutines | 1.10.2 |

---

## License

[MIT](LICENSE) — free to use, modify, and distribute.

# 🚀 MockPulse v1.0.0 — First Official Release

**MockPulse** is a battery-included **mock HTTP API server** with a **real-time inspect & intercept control plane**, built on a high-performance Go/Fiber core — plus a sleek **desktop control-room** (Kotlin · Compose Multiplatform) styled with a cyberpunk **glassmorphism** theme.

Spin up a fully-featured mock of any OpenAPI service in seconds: stateful responses, Lua scripting, live traffic inspection, dynamic breakpoints, chaos injection and proxy-with-auto-recording — all from one tool.

---

## 🚀 What's New / Highlights

### ⚡ Core Go Engine
- **Fiber + fasthttp** HTTP stack with **sub-millisecond overhead** per request.
- Auto-bootstraps every route & schema directly from **OpenAPI 3.0 / 3.1** specs.
- Built-in **Chaos Engineering**: configurable latency (fixed/range), error rates + error codes, and request drops — adjustable **live** without restarts.

### 🧠 Stateful Mocks
- **Embedded Lua scripting** (`gopher-lua`) — per-request hooks can read the request, transform responses and drive the mock dynamically.
- **In-Memory KV store with TTL** — persist counters and shared state between requests (e.g. increment, session, caches).

### 📡 Real-time Inspector & Interceptor
- Bidirectional **WebSocket protocol** (`/__mockpulse/ws`) streaming `ready · traffic · breakpoint · resolved · ping` events in real time.
- **Dynamic breakpoints** — pause any matching request, inspect the payload, then **pass**, **drop** or **modify** the response from the client before resolving.

### 🖥️ MockPulse Studio GUI
- **Kotlin · Compose Multiplatform** desktop app (Windows/macOS).
- Live traffic table + filters, request detail drawer with **JSON syntax highlighting**.
- Interactive **breakpoint interceptor dialog**, **Chaos dashboard** sliders and routes/state management.
- **Aurora glassmorphism** theme — frosted panels, neon accents over a blurred gradient backdrop.

### 🌐 Proxy & Auto-Recording
- **Proxy mode** — fall back to a real upstream when no mock matches, with transparent **auto-recording** of every proxied exchange for later replay.

---

## 📦 Download & Quick Start

### Binary (preferred)
Grab the platform binary from the **Assets** section below
(`mockpulse_1.0.0_windows_amd64.tar.gz`, `…_linux_amd64`, `…_darwin_arm64`, etc.).

```bash
# Start the server from an existing OpenAPI spec
./mockpulse --spec ./petstore.json --port 8080

# Enable interactive breakpoints + proxy fallback
./mockpulse --spec ./petstore.json --intercept --proxy http://api.example.com
```

### Docker
```bash
docker pull ghcr.io/cokovali10-sketch/mockpulse:1.0.0

docker run -p 8080:8080 \
  -v "$PWD/spec.json:/spec.json" \
  ghcr.io/cokovali10-sketch/mockpulse:1.0.0 \
  --spec /spec.json --port 8080
```

### MockPulse Studio (Desktop GUI)
Download `MockPulseStudio-1.0.0.msi` (Windows) from the **Assets**, then press **Connect** in the header to target any running MockPulse instance.

---

## 🧪 Quality & Coverage

- ✅ **100% unit-test pass** on the Go backend (`go test ./...`) across all 10 packages — traffic hub, chaos engine, Lua hooks, realtime interceptor, proxy recorder and server control plane.
- ✅ **100% unit-test pass** on the Studio client (`./gradlew test`) — parser, models and formatting suites.
- ✅ **Live end-to-end verification** — WS breakpoint flow, SSE streaming, Lua stateful counters and proxy auto-recording all validated against the real server.

---

## ✍️ Full Changelog

For everything that landed since the last pre-release, see [CHANGELOG.md](./CHANGELOG.md).

---

**Give it a star** ⭐ if MockPulse makes your API mocking painless, and report issues at
[https://github.com/cokovali10-sketch/Mockpulse/issues](https://github.com/cokovali10-sketch/Mockpulse/issues).
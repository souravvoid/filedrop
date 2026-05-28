# PROJECT_ANALYSIS.md — PeerLink Engineering Analysis
**Analyst:** Antigravity (Principal Engineer)  
**Date:** 2026-05-28  
**Repo:** souravvoid/filedrop  
**Version:** 1.0.0  

---

## 1. Architecture Summary

PeerLink is a **serverless peer-to-peer file transfer desktop application** built on JavaFX 21. Two machines connect directly over LAN TCP sockets using ECDH key exchange and AES-256-GCM stream encryption. Zero cloud, zero relay, zero third-party servers.

```
┌──────────────────────────────────────────────┐
│                  UI Layer                    │
│   Main.java · MainController.java · FXML     │
│   peerlink.css (Aurora theme)                │
└──────────────────┬───────────────────────────┘
                   │
┌──────────────────▼───────────────────────────┐
│               Logic Layer                    │
│   FileSender · FileReceiver · NetworkUtils   │
│   PortUtils  · InviteCode   · TransferStats  │
└──────────────────┬───────────────────────────┘
                   │
┌──────────────────▼───────────────────────────┐
│             Security Layer                   │
│   CryptoUtils (ECDH, AES-GCM)               │
│   HandshakeManager (key exchange protocol)   │
└──────────────────────────────────────────────┘
```

---

## 2. Module Breakdown

| Package | Files | Responsibility |
|---------|-------|----------------|
| `com.peerlink.ui` | Main.java, Launcher.java, MainController.java, MainView.fxml, peerlink.css | JavaFX UI, navigation, user interaction |
| `com.peerlink.logic` | FileSender, FileReceiver, InviteCode, NetworkUtils, PortUtils, TransferStats | Transfer engine, network, encoding |
| `com.peerlink.security` | CryptoUtils, HandshakeManager | ECDH + AES-256-GCM crypto |

Total: **11 Java classes**, **1 FXML**, **1 CSS**, **1 logback config**

---

## 3. Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| javafx-controls | 21.0.3 | UI controls |
| javafx-fxml | 21.0.3 | FXML loader |
| slf4j-api | 2.0.13 | Logging facade |
| logback-classic | 1.5.6 | Logging implementation |

**No third-party crypto libraries** — uses JDK built-in `javax.crypto` and `java.security`. This is correct and appropriate.

---

## 4. Security Observations

| # | Finding | Severity | Status |
|---|---------|----------|--------|
| 1 | ECDH secp256r1 correctly implemented | ✅ Good | — |
| 2 | AES-256-GCM with 128-bit auth tag | ✅ Good | — |
| 3 | IV incremented per chunk (not random) — avoids IV collision on large files | ✅ Good | — |
| 4 | GCM authentication tag validates integrity — tampered data detected | ✅ Good | — |
| 5 | Metadata length bounds checked (0–8192 bytes) — prevents memory bombs | ✅ Good | — |
| 6 | Chunk length bounded to 4MB+28 — prevents OOM | ✅ Good | — |
| 7 | Sender approval dialog before every transfer | ✅ Good | — |
| 8 | Filename: no path traversal prevention in `receiveFileData` — `new File(saveDir, fileName)` trusts sender-supplied name | ⚠️ Medium | See Recommendations |
| 9 | No TLS/certificate pinning on peer public keys — ECDH is unauthenticated (MITM possible on untrusted LAN) | ⚠️ Medium | By design, invite-code is out-of-band auth |
| 10 | No rate limiting on connection attempts | 🟡 Low | Acceptable for LAN tool |

---

## 5. Build Observations

| # | Observation | Impact |
|---|-------------|--------|
| 1 | Java version mismatch: pom.xml targets Java 21, system has Java 25 | **Build works**, compiler compat mode works |
| 2 | `linux-rpm` profile auto-activates on Fedora (detects `/etc/redhat-release`) and **conflicts** with `jar-only` profile | **Breaks default `mvn package`** — requires `-DskipRpm=true` |
| 3 | jpackage app-image fails on Fedora 44 Java 25 due to modified `java.security` | **jpackage unusable on this host** |
| 4 | No `.gitignore` present — `target/`, `.idea/`, `.vscode/` tracked in git | **Repository pollution** — Fixed |
| 5 | `dependency-reduced-pom.xml` committed to repo | **Clutter** — Fixed |
| 6 | `styles.css` in resources is unused (app loads `peerlink.css` only) | **Dead asset** |
| 7 | `--add-opens` compiler arg has no effect at compile time (Maven warning) | **Cosmetic warning** |

---

## 6. Packaging Observations

| # | Observation |
|---|-------------|
| 1 | AppImage strategy: `jpackage --type app-image` fails; manual AppDir + AppRun created as workaround |
| 2 | AppRun uses **system Java** with **bundled JavaFX modules** — portable across distros with Java 21+ |
| 3 | `appimagetool` not installed on host — final `.AppImage` binary cannot be generated locally; see APPIMAGE_BUILD.md |
| 4 | CI/CD pipeline covers: DEB (Ubuntu), RPM (Fedora container), Windows, macOS — well-structured |
| 5 | macOS build references `icon.icns` which does not exist in repo — macOS CI will fail |

---

## 7. Missing Components

| Component | Impact |
|-----------|--------|
| `.gitignore` | Critical — IDE/build artifacts polluting repo |
| `icon.icns` | macOS DMG build will fail |
| `README.md` | No root README — GitHub shows blank project page |
| Unit test sources (`src/test/`) | Zero test infrastructure |
| `appimagetool` binary | AppImage assembly incomplete locally |
| Path traversal sanitization | Receiver accepts any filename from sender |

---

## 8. Production Risks

| Risk | Probability | Severity |
|------|-------------|----------|
| MITM on untrusted WiFi (no peer auth) | Medium | High |
| Sender sends `../evil.sh` as filename | Low (local LAN) | Medium |
| Large file (>2GB) sent — `totalSent` int math could overflow | Low | High |
| Java 21 requirement — users with Java 8/11 cannot run | High | Medium |
| jpackage fails on modified-security JDKs (Fedora 44+) | Medium | Medium |

---

## 9. Code Quality Issues

- `TransferStats.etaSeconds` is typed as `String` but named `etaSeconds` — misleading field name
- `FileSender.sendFileData()` overhead calculation: `encrypted.length - 28` assumes exactly 28 bytes overhead (12 IV + 16 tag) — correct but undocumented
- `FileReceiver`: `totalReceived[0]` uses single-element array as lambda closure workaround — not ideal; use `AtomicLong`
- `Launcher.java` is a one-liner delegating to `Main.java` — sole purpose is to be the shaded JAR entry class (valid pattern)

---

## 10. Dead Code Findings

| File | Issue |
|------|-------|
| `styles.css` | Never loaded by application — `Main.java` loads `peerlink.css` only |
| `packaging/com.peerlink.PeerLink.desktop` | Duplicate of AppDir desktop entry |
| `dist/mac/README.txt`, `dist/windows/README.txt` | Placeholder READMEs with no real content |

---

## 11. Performance Bottlenecks

| Area | Observation |
|------|-------------|
| Buffer size | 4MB send/receive socket buffers + 4MB file read chunks — well-tuned |
| Queue depth | 8-slot blocking queue — good balance between memory and throughput |
| Virtual threads | Used correctly for I/O-bound sender/receiver — optimal |
| TCP_NODELAY | Enabled — reduces latency for small packets |
| Encryption | Per-chunk AES-GCM in producer thread — off UI thread ✅ |
| ETA calculation | Updates every chunk — could cause UI spam on fast LAN |

---

## 12. Recommendations

1. **Fix path traversal**: Sanitize received filenames — strip directory separators
2. **Fix profile conflict**: Make `linux-rpm` profile explicitly activated only (not OS file detection)  
3. **Add `icon.icns`**: Required for macOS DMG builds to succeed
4. **Add README.md**: Essential for GitHub discoverability
5. **Change `etaSeconds` to `eta`**: `TransferStats` field naming
6. **Replace `totalReceived[0]` with `AtomicLong`**: Cleaner threading pattern
7. **Add `src/test/java`**: Minimal JUnit 5 test sources for CI integration
8. **Consider peer authentication**: Publish fingerprint of ephemeral public key in invite code

---

*Generated by Antigravity Engineering Audit*

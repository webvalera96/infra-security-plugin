# AGENTS.md — Infra-Security Plugin

## What This Plugin Does

An IntelliJ IDEA plugin that integrates an external LSP server ("ips") to provide security analysis for Docker/Container files. When you open a `Dockerfile` or `Containerfile`, the plugin launches the `ips` LSP server (via `--stdio`) and connects it to the IDE's LSP framework.

## Build & Run Commands (via Gradle Wrapper)

| Action | Command | Notes |
|--------|---------|-------|
| **Run IDE with plugin** | `./gradlew runIde` | Launches a sandboxed IntelliJ instance |
| **Run tests** | `./gradlew check` | Uses `IntelliJ Platform Test Framework` |
| **Verify plugin** | `./gradlew verifyPlugin` | Checks compatibility against the target IDE version |
| **Publish to Marketplace** | `./gradlew publishPlugin` | Needs `JETBRAINS_TOKEN` / marketplace credentials |

All commands use the Gradle Wrapper (`./gradlew`). The project uses Gradle Configuration Cache and Build Cache (enabled in `gradle.properties`).

## Project Structure

```
├── build.gradle.kts                     # IntelliJ Platform Gradle Plugin config
├── settings.gradle.kts                  # Plugin management, repository config
├── gradle.properties                    # Group: ru.webvalera96, version: 1.0.0-SNAPSHOT
├── gradle/libs.versions.toml            # Version catalog (currently only JUnit 4.13.2)
├── src/main/kotlin/
│   └── InfraProductsSecurityLspServerSupportProvider.kt  # Main source file
├── src/main/resources/META-INF/
│   ├── plugin.xml                       # Plugin manifest
│   └── pluginIcon.svg                   # Plugin icon
└── .run/
    ├── runIde.run.xml                   # Predefined Run/Debug config for runIde
    ├── runTests.run.xml                 # Predefined Run/Debug config for check
    └── runVerifications.run.xml         # Predefined Run/Debug config for verifyPlugin
```

## Architecture & Data Flow

**Entry point**: `plugin.xml` registers `InfraProductsSecurityLspServerSupportProvider` as an `platform.lsp.serverSupportProvider` extension.

**Trigger flow**:
1. IntelliJ opens any file in the project
2. `InfraProductsSecurityLspServerSupportProvider.fileOpened()` is called
3. Only if the file is a `Dockerfile` or `Containerfile` (case-insensitive), the LSP server is started
4. The `InfraProductsSecurityLspServerDescriptor` (a `ProjectWideLspServerDescriptor`) creates a command: `<ips_binary> --stdio`
5. `isSupportedFile()` confirms only Docker/Container files get routed to this server

**LSP server binary resolution** (in priority order):
1. `$IPS_PATH` environment variable — if set and the file exists, use that path
2. Fallback: bare `ips` command (must be on `$PATH`)

**ProjectWideLspServerDescriptor** means a single LSP server instance serves the whole project, not per-file.

## Key Conventions & Gotchas

- **The plugin ID is `ru.webvalera96.infra-security-plugin`** — used in plugin.xml and for Marketplace identity.
- **Kotlin stdlib is NOT bundled** (`kotlin.stdlib.default.dependency = false` in `gradle.properties`). The IDE provides its own Kotlin runtime.
- **The only dependency is `com.intellij.modules.lsp`** (declared in plugin.xml). This pulls in the LSP API (`com.intellij.platform.lsp.api`).
- **No Kotlin version is explicitly declared** in `build.gradle.kts`; `org.jetbrains.kotlin.jvm` version `2.3.20` is set in `settings.gradle.kts` under `pluginManagement`.
- **Target IDE version**: IntelliJ IDEA `2026.1.4` (set in `build.gradle.kts` via `intellijIdea("2026.1.4")`).
- **The source file uses `internal` visibility** for both the provider and utility class — these are not part of the plugin's public API.
- **The `ips` binary must exist** at runtime — the plugin does not bundle it. It's expected to be installed separately by the user or set via `IPS_PATH`.
- **No test files exist yet** — `src/test` directory is absent. Test framework dependency (JUnit 4.13.2) is declared but unused.
- **Logs** from run/debug live in `$PROJECT_DIR$/.intellijPlatform/sandbox/.../log/idea.log` (and `.../log-test/idea.log` for tests).

## Common Gotchas When Modifying

- If you add new LSP features (e.g., code actions, diagnostics handling), ensure they go in `InfraProductsSecurityLspServerDescriptor` or a new subclass. The descriptor controls command line, file support, and LSP capabilities.
- If you change the provider to support additional file types, update both `fileOpened()` and `isSupportedFile()` — they must stay in sync.
- The `findBinary()` logic is the only place where the LSP server path is resolved. If you add more fallback strategies, keep them in priority order.
- The `--stdio` flag is hardcoded in `createCommandLine()`. The `ips` LSP server must support the stdio transport.

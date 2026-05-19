# springboot-test-project — SpecShield test fixture

A minimal Spring Boot / Maven project used to validate that SpecShield's `Dashboard → Set up CI` flow works correctly for Java projects. Pair this with [`../test_scenarios.md`](../test_scenarios.md) when running a test pass.

The OpenAPI specs in this directory are intentionally **identical** to the ones in `../nodejs-test-project/` so test scenario **P9.1** (multi-language consistency) can verify that SpecShield's diff output is byte-identical across project types — proving the tool is language-agnostic.

## What's here new

| Path | Purpose |
|---|---|
| `pom.xml` | Detection target for `specshield init` (provides the service name via `<artifactId>`). |
| `openapi.yaml` | Baseline (v1) OpenAPI spec — identical to the Node fixture. |
| `openapi.v2.yaml` | v2 spec with deliberate breaking, modification, and additive changes. |
| `src/main/java/com/specshield/test/TestApplication.java` | Spring Boot entry point. |
| `src/main/java/com/specshield/test/UserController.java` | In-memory implementation of the v1 spec. Optional — `specshield` doesn't need the app running. |
| `.gitignore` | Excludes `target/`, IDE files, diff outputs, etc. |

## What `specshield init` should detect

When you run `specshield init` here, the wizard should report:

```
✔  Detected git repo: <wherever you pushed it>
✔  Found OpenAPI spec: openapi.yaml
✔  Detected service name from pom.xml: springboot-test-project
```

Specifically: the service-name detection comes from a naive `<artifactId>` regex match in `projectDetect.js:104-108` — so the value of `<artifactId>` in `pom.xml` is what gets picked up.

## Spec change summary (v1 → v2)

Identical to the Node fixture:

| Severity | Change |
|---|---|
| 🚨 Breaking | `User.legacy_id` response field removed |
| 🚨 Breaking | `POST /users` request body `email` is now required (was optional) |
| 🚨 Breaking | `DELETE /users/{id}` endpoint removed |
| ✏️ Modification | `GET /users` `limit` max raised from 100 → 250 |
| ✨ Addition | New endpoint: `GET /users/{id}/audit-log` |
| ✨ Addition | New optional `User.last_login_at` response field |

## How to use this fixture

```bash
# 1. (Optional) Run the app to confirm the spec matches the implementation.
mvn spring-boot:run
# Then visit: http://localhost:8080/v3/api-docs (springdoc auto-generated spec)

# 2. Detection sanity check — confirms specshield init can find the project.
specshield init --print

# 3. Local compare — should produce 3 breaking + 1 modification + 2 additions.
specshield compare openapi.yaml openapi.v2.yaml

# 4. JSON output — should be byte-identical to nodejs-test-project's output.
specshield compare openapi.yaml openapi.v2.yaml --json --output diff.json

# 5. Cross-project consistency check (test scenario P9.1):
diff <(jq -S . diff.json) <(jq -S . ../nodejs-test-project/diff.json)
# Expected: no output. Both projects produce identical diff JSON.
```

## To set up the GitHub Actions test (Phase 8 of test_scenarios.md)

```bash
# From inside this directory:
git init
git add -A
git commit -m "Initial test fixture"
gh repo create specshield-test-java --private --source=. --remote=origin --push

# Add the API key as a repo secret
gh secret set SPECSHIELD_API_KEY --body "$YOUR_SPECSHIELD_API_KEY"

# Then create a PR that bumps openapi.yaml to openapi.v2.yaml content
git checkout -b breaking-changes
cp openapi.v2.yaml openapi.yaml
git commit -am "Test: introduce breaking changes"
git push -u origin breaking-changes
gh pr create --fill
```

A sticky PR comment should appear within ~2 minutes showing the diff report. The check should be ❌ failing because of the 3 breaking changes. **The PR comment should look identical to the Node project's PR comment** (per test scenario P9.4).

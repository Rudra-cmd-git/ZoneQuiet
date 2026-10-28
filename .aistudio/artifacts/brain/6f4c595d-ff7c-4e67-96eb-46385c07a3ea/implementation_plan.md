# Guarantee APK Attachment on GitHub Releases

A targeted update to ensure that every push to the `main` branch automatically creates and updates a GitHub Release with the compiled APK file directly attached and downloadable under the Assets section.

### User Review & Critical Decisions

> [!IMPORTANT]
> - **Identified Cause of Empty Release**: 
>   1. When running on the `main` branch or manual trigger, `github.ref_name` was evaluating to `"main"` (a branch name, not a valid semantic release tag).
>   2. The file upload was using a wildcard path (`release-assets/ZoneQuiet-*.apk`) that silently succeeded without failing if files weren't matched.
> - **Confirmed Solutions**:
>   1. **Trigger on Every Push to `main`**: Enable `on: push: branches: [ "main" ]` so whenever changes are pushed from AI Studio, the Release workflow runs automatically.
>   2. **Explicit Tagging**: Force a valid release tag (e.g. `v1.0.0`) when triggered from `main`.
>   3. **Direct File Reference & Validation**: Point `files` directly to the compiled APK and set `fail_on_unmatched_files: true` so the release cannot be created without the APK attached.

---

### 1. Overview & Core Concept

- **What It Delivers**: 
  1. Guaranteed attachment of `ZoneQuiet.apk` directly to the GitHub Release on every push.
  2. Immediate download availability under **GitHub Releases -> Assets**.
  3. Continuous artifact upload under **GitHub Actions -> Summary -> Artifacts**.
- **Key Value**: One-click download directly from the GitHub repository homepage and Releases page without empty assets.

---

### 2. User Experience & Releases Walkthrough

1. **Pushing Code**:
   - Push code to GitHub via AI Studio's **Settings / GitHub** menu.
2. **Automated Release**:
   - Workflow runs on `main`.
   - Packages `app-debug.apk` into `ZoneQuiet-v1.0.0.apk`.
   - Publishes or updates release **ZoneQuiet v1.0.0**.
3. **Downloading the APK**:
   - Open your GitHub repository.
   - Click on **Releases** on the right side.
   - Under **Assets**, click **`ZoneQuiet-v1.0.0.apk`** to download.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Trigger on Branch `main` with Dedicated Tag**
  - *Chosen Approach*: `on: push: branches: [ main ]` with tag `v1.0.0` or dynamic tag generation.
  - *Why*: AI Studio pushes directly to the `main` branch. This eliminates the need for manual tag pushes or CLI commands.

- **Decision 2: Direct File Path & Unmatched File Guard**
  - *Chosen Approach*: Explicitly copy `app-debug.apk` to `release-assets/ZoneQuiet-v1.0.0.apk` and specify `fail_on_unmatched_files: true`.
  - *Why*: Prevents empty releases by enforcing that the APK must exist and be uploaded.

---

### 4. Technical Architecture & Workflow Specification

```
┌────────────────────────────────────────────────────────────────────────┐
│                     Updated Release Workflow Pipeline                  │
│                                                                        │
│   Trigger: `on: push: branches: [ "main" ]` + `tags: [ "v*" ]`         │
│                        │                                               │
│                        ▼                                               │
│   1. Restore Debug Keystore                                            │
│   2. Build APK (`gradle assembleDebug`)                                │
│   3. Copy binary: `app-debug.apk` ──> `ZoneQuiet-v1.0.0.apk`           │
│   4. Upload to GitHub Actions Artifacts (`ZoneQuiet-apk`)              │
│   5. `softprops/action-gh-release@v2`:                                 │
│      ├── tag_name: `v1.0.0`                                            │
│      ├── name: "ZoneQuiet v1.0.0"                                      │
│      ├── files: `release-assets/ZoneQuiet-v1.0.0.apk`                  │
│      ├── fail_on_unmatched_files: true                                 │
│      └── generate_release_notes: true                                  │
└────────────────────────────────────────────────────────────────────────┘
```

#### Execution Steps Upon Approval

1. Update `.github/workflows/release-apk.yml` with the branch trigger, explicit tag handling, and unmatched file validation.
2. Commit changes to the `main` branch.
3. Provide instructions for pushing and downloading from Releases.

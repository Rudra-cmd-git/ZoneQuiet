# Release ZoneQuiet v1.0.1 Package & GitHub Deployment

A comprehensive plan to increment the application version to `v1.0.1` (versionCode 2), build the updated APK with density-aware map alignment and native geocoding search, and configure the GitHub release workflow to publish the `v1.0.1` release.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following parameters were confirmed during clarification:
> - **Version Number**: `v1.0.1` (versionCode `2`, versionName `"1.0.1"`).
> - **Release Tag Target**: `v1.0.1` on GitHub Releases with `ZoneQuiet-v1.0.1.apk` attached directly.

---

### 1. Overview & Core Concept

- **What It Delivers**: 
  1. Updated Android application package with official version `1.0.1` reflecting all map rendering and search fixes.
  2. Local build verification and binary assembly producing the new APK.
  3. GitHub CI/CD workflow configured for `ZoneQuiet v1.0.1` with the git tag `v1.0.1` ready to push.
- **Key Value**: Clean version increment ensuring Android devices recognize the update over the previous v1.0 build, and GitHub users can easily download the latest `ZoneQuiet-v1.0.1.apk`.

---

### 2. User Experience & Release Walkthrough

1. **In-App & Package Identity**:
   - Package version code bumped to `2`, version name to `"1.0.1"`.
   - Android OS allows users with v1.0 installed to upgrade seamlessly without uninstalling.
2. **GitHub Releases Page**:
   - New release published: **ZoneQuiet v1.0.1**.
   - Under **Assets**, the direct download link is **`ZoneQuiet-v1.0.1.apk`**.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision: Version Code Increment (1 -> 2, "1.0" -> "1.0.1")**
  - *Chosen Approach*: Update `defaultConfig` in `app/build.gradle.kts`.
  - *Why*: Android Package Manager requires `versionCode` to strictly increase for package updates to succeed on physical devices.

---

### 4. Technical Architecture & Deployment Sequence

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Version Bump Pipeline                           │
│                                                                        │
│   `app/build.gradle.kts`                                               │
│   ├── versionCode: 1 ──> 2                                             │
│   └── versionName: "1.0" ──> "1.0.1"                                   │
│                        │                                               │
│                        ▼                                               │
│   Local Build: `gradle assembleDebug`                                  │
│   └── Outputs: `app/build/outputs/apk/debug/app-debug.apk` (v1.0.1)    │
│                        │                                               │
│                        ▼                                               │
│   GitHub Actions: `.github/workflows/release-apk.yml`                  │
│   ├── tag_name: `v1.0.1`                                               │
│   ├── name: "ZoneQuiet v1.0.1"                                         │
│   └── Asset: `ZoneQuiet-v1.0.1.apk`                                    │
│                        │                                               │
│                        ▼                                               │
│   Git: Commit changes & create tag `v1.0.1`                            │
└────────────────────────────────────────────────────────────────────────┘
```

#### Execution Steps Upon Approval

1. Update `versionCode = 2` and `versionName = "1.0.1"` in `app/build.gradle.kts`.
2. Update `.github/workflows/release-apk.yml` to target `ZoneQuiet v1.0.1` and `ZoneQuiet-v1.0.1.apk`.
3. Assemble the updated APK using `gradle assembleDebug`.
4. Commit the changes and tag `v1.0.1`.
5. Guide the user on pushing to GitHub.

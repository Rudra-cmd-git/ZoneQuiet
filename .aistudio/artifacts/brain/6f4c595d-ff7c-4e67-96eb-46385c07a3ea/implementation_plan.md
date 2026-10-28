# Fix GitHub Actions Keystore & Workflow Build Failure

A surgical fix to resolve the GitHub Actions CI failure (`Keystore file '/home/runner/work/ZoneQuiet/ZoneQuiet/debug.keystore' not found for signing config 'debugConfig'`) across both the build and release workflows.

### User Review & Critical Decisions

> [!IMPORTANT]
> - **Identified Root Cause**: The file `debug.keystore` is excluded by `.gitignore` to prevent committing binary keystores. The repository includes `debug.keystore.base64`, but the CI workflow runner was attempting to build without first reconstructing `debug.keystore`.
> - **Resolution**: Add an automated decoding step in `.github/workflows/build-apk.yml` and `.github/workflows/release-apk.yml` that restores `debug.keystore` from `debug.keystore.base64` (with a resilient `keytool` fallback generator if the base64 file is ever absent).

---

### 1. Overview & Core Concept

- **What It Fixes**: 
  1. Prevents the `:app:validateSigningDebug` task failure on GitHub Actions runners.
  2. Ensures both `.github/workflows/build-apk.yml` and `.github/workflows/release-apk.yml` build the APK reliably and attach it to GitHub Releases.
- **Key Value**: 100% automated, zero-configuration CI/CD pipeline on GitHub without needing manual secrets or broken local files.

---

### 2. User Experience & Workflow After Fix

1. **Pushing Changes**:
   - Push your code or tag (`v1.0.0`) to GitHub.
2. **GitHub Actions Run**:
   - Step 1: Checks out the repository.
   - Step 2: Restores `debug.keystore` from `debug.keystore.base64`.
   - Step 3: Compiles `app-debug.apk` without signing errors.
   - Step 4: Packages and attaches `ZoneQuiet-v1.0.0.apk` to the GitHub Release.
3. **Download**:
   - The green checkmark appears on GitHub Actions, and the APK is immediately ready for download under **Releases**!

---

### 3. Key Product Decisions & Trade-Offs

- **Decision: Automated Keystore Restoration in CI vs. Committing Binary Keystores**
  - *Chosen Approach*: Decode `debug.keystore.base64` at workflow runtime with a `keytool` generator fallback.
  - *Why*: Adheres to Android and Git best practices by keeping `.gitignore` clean while guaranteeing the GitHub Actions runner always has a valid debug keystore.

---

### 4. Technical Architecture & Build Fix Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        GitHub Actions Runner                           │
│                                                                        │
│   git checkout ──> Has source code & `debug.keystore.base64`           │
│                          │                                             │
│                          ▼                                             │
│   [NEW STEP] "Restore Debug Keystore"                                  │
│   ├── Checks for `debug.keystore.base64`                               │
│   ├── Decodes to `/home/runner/work/ZoneQuiet/ZoneQuiet/debug.keystore`│
│   └── Fallback: generates keystore via `keytool` if missing            │
│                          │                                             │
│                          ▼                                             │
│   Task `:app:validateSigningDebug`                                     │
│   ├── Validates `debug.keystore` (FOUND & VALID)                       │
│   └── Compiles, packages, and signs `app-debug.apk`                    │
│                          │                                             │
│                          ▼                                             │
│   Task: `softprops/action-gh-release@v2`                               │
│   └── Publishes Release `v1.0.0` with `ZoneQuiet-v1.0.0.apk`           │
└────────────────────────────────────────────────────────────────────────┘
```

#### Steps to Execute Upon Approval

1. Update `.github/workflows/build-apk.yml` with the keystore restoration step.
2. Update `.github/workflows/release-apk.yml` with the keystore restoration step.
3. Commit and push the updated workflows to `main` and update the `v1.0.0` tag.

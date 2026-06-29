# Handoff

## Goal
Android warehouse management app (RGMCInventory v2) — shipping a polished v2.0 APK with three theme modes (Minimalist B&W default, Light, Dark) and a streamlined scanner form. The end state is a signed release APK uploaded to the Google Drive folder linked in the Update App button: `https://drive.google.com/drive/folders/1uJxDnvHUz_s9qd6l0vs1tmmkoTMp8sFy`.

## Current State
**Everything is working. The release APK is built and signed.**

- `app/build/outputs/apk/release/app-release.apk` — 32 MB, signed with `rgmc_release.jks`, ready to upload to Google Drive
- `versionCode = 2`, `versionName = "2.0"` (in `app/build.gradle.kts`)
- Three themes fully wired: Minimalist (default, B&W), Light, Dark — toggled via MIN|LGT|DRK buttons in the main screen header
- Scanner setup form: encoder removed, rack + location moved to inventory screen
- Inventory screen: compact RACK + LOCATION bar sits between stats bar and search field; location auto-selects "Stock Room" on first load

The only remaining action is uploading the APK to Google Drive (cannot be done by Claude).

## Files Actively Being Edited
All edits are complete and stable. These are the files changed across this session:

**Theme system (from prior session, now stable):**
- `app/src/main/res/values/attrs.xml` — NEW: declares 24 custom color attrs (`colorBackground`, `colorBorder`, `colorTextPrimary`, `colorModuleScanner`, `colorVariancePositive`, etc.)
- `app/src/main/res/values/colors.xml` — REWRITTEN: all colors prefixed `dark_*`, `light_*`, `min_*`; added `<color name="colorPrimary">#1A56DB</color>` constant for adaptive launcher icon
- `app/src/main/res/values/themes.xml` — REWRITTEN: `Theme.RGMCInventory` (Minimalist/default), `Theme.RGMCInventory.Light`, `Theme.RGMCInventory.Dark`, plus 3 splash variants; dark theme parent is `Theme.MaterialComponents` (NOT `Theme.MaterialComponents.DarkActionBar` — that doesn't exist)
- `app/src/main/java/com/rgmc/inventory/util/ThemeManager.kt` — NEW: manages mode via SharedPreferences; `applyTheme()` called before `super.onCreate()`
- `app/src/main/java/com/rgmc/inventory/ui/MainActivity.kt` — added `ThemeManager.applyTheme(this)` before `super.onCreate()`
- `app/src/main/java/com/rgmc/inventory/ui/SplashActivity.kt` — added `ThemeManager.applySplashTheme(this)` before `super.onCreate()`
- `app/src/main/res/layout/fragment_main.xml` — REWRITTEN: added MIN|LGT|DRK toggle in header; all `@color/` → `?attr/`
- `app/src/main/java/com/rgmc/inventory/ui/main/MainFragment.kt` — REWRITTEN: added `setupThemeToggle()`, `switchTheme()`, `updateToggleUI()`; uses `import com.google.android.material.R as MatR` for `MatR.attr.colorPrimary`
- `app/src/main/res/drawable/bg_theme_toggle_track.xml` — NEW: rounded rect with `?attr/colorBorder` stroke
- All 24 layout files — `@color/colorX` → `?attr/colorX`; `?attr/colorBackground` used throughout (declared in attrs.xml)
- 8 drawable files (`bg_card.xml`, `bg_input_field.xml`, etc.) — updated to use `?attr/` references
- `app/src/main/java/com/rgmc/inventory/ui/adapter/InventoryAdapter.kt` — uses `resolveAttrColor()` for variance colors
- `app/src/main/java/com/rgmc/inventory/ui/adapter/ScanHistoryAdapter.kt` — uses `resolveAttrColor()` for status dot colors
- `app/src/main/java/com/rgmc/inventory/ui/scanner/BarcodeScannerFragment.kt` — uses `resolveAttrColor()` instead of hardcoded hex colors
- `app/src/main/java/com/rgmc/inventory/ui/scanner/ActiveCutOffsFragment.kt` — uses `resolveAttrColor(R.attr.colorPrimaryLight)`

**Scanner form refactor (this session):**
- `app/src/main/res/layout/fragment_scanner_setup.xml` — removed LOCATION field from SCHEDULE card; removed entire PERSONNEL section (rack + encoder); SCHEDULE card bottom margin bumped to 28dp (now last card before button)
- `app/src/main/res/layout/fragment_scanner_inventory.xml` — added compact RACK + LOCATION row between stats bar and search; `etRack` (52dp, default "1") + `spinnerLocation` (auto-selects "Stock Room")
- `app/src/main/java/com/rgmc/inventory/ui/scanner/ScannerSetupFragment.kt` — removed `currentLocations`, `setupLocationSpinner()`, encoder/rack focus listeners, `vm.onEncoderChanged()`/`vm.onRackChanged()` from `btnEnter`; explicit entity imports instead of wildcard
- `app/src/main/java/com/rgmc/inventory/ui/scanner/ScannerInventoryFragment.kt` — added `currentLocations`, `setupLocationSpinner()` with stock-room auto-select logic, rack pre-fill from ViewModel, `etRack` focus listener

## Failed Attempts
- **What was tried**: Changing dark theme parent to `Theme.MaterialComponents.DarkActionBar` — **Why it failed**: That style name does not exist in Material Components 1.12.0. Correct parent is `Theme.MaterialComponents`.
- **What was tried**: Replacing `?attr/colorBackground` in all 13 layout files with `?android:attr/colorBackground` — **Why it failed**: `?android:attr/colorBackground` reads the Android system attribute value which is controlled by the Material parent theme (white for light), not our custom theme values. Reverting to `?attr/colorBackground` (with the attr declared in our attrs.xml) is the correct approach.
- **What was tried**: Keeping `colorBackground` out of `attrs.xml` and using `?attr/colorBackground` in layouts — **Why it failed**: aapt2 treats undeclared attribute names as app-namespace attributes and fails to link with "style attribute 'attr/colorBackground (aka com.rgmc.inventory:attr/colorBackground)' not found."

## Next Step
**Upload the APK to Google Drive.** The file is at:
```
C:\RGMC\Source\git\RGMCInventory-v2\app\build\outputs\apk\release\app-release.apk
```
Upload it to: `https://drive.google.com/drive/folders/1uJxDnvHUz_s9qd6l0vs1tmmkoTMp8sFy`

This is the only remaining task. No code changes are pending.

## Context & Gotchas
- **Signing config is hardcoded in `app/build.gradle.kts`**: `storeFile = file("../rgmc_release.jks")`, `storePassword = "RGMCInv2024!"`, `keyAlias = "rgmc_key"`, `keyPassword = "RGMCInv2024!"`. The keystore is at `C:\RGMC\Source\git\rgmc_release.jks` (one level above the project root).
- **SDK is read-only**: `C:\Program Files (x86)\Android\android-sdk` triggers marshalling warnings on every build ("Probably the SDK is read-only"). These are harmless warnings, not errors — builds succeed fine.
- **`colorBackground` is declared in our `attrs.xml`**, not just set in themes. This is intentional — without the declaration aapt2 can't resolve `?attr/colorBackground` in layout XML files even though Material Components sets the value.
- **Dark theme parent must be `Theme.MaterialComponents`** (not `...DarkActionBar` — that variant doesn't exist). The dark theme also needs `android:windowLightStatusBar = false` set explicitly (already done).
- **`resolveAttrColor()` extension is in `ThemeManager.kt`**: `fun Context.resolveAttrColor(@AttrRes attr: Int): Int` — imported by all fragments that need programmatic color resolution.
- **`MatR.attr.colorPrimary`**: `MainFragment.kt` imports `com.google.android.material.R as MatR` and uses `MatR.attr.colorPrimary` for the toggle button highlight. Using `R.attr.colorPrimary` would fail because `colorPrimary` is not in the app's own `attrs.xml`.
- **Encoder field**: Removed from UI but `encoder: String = ""` still exists in `ScannerSetupState` and is written to DB as empty string. No schema migration needed.
- **Location auto-select logic**: `setupLocationSpinner()` in `ScannerInventoryFragment` searches for the first location whose `locationName` contains "stock" (case-insensitive). If none found, no default is set. This handles the "Stock Room" default requirement.
- **Rack pre-fill on resume**: `binding.etRack.setText(vm.setupState.value.rack.toString())` runs at `onViewCreated` time, so resumed sessions (via `resumeSession()`) correctly show the session's saved rack number.
- **`v2.0` badge in UI** is hardcoded text in `fragment_main.xml` line 62 (`android:text="v2.0"`). If version is bumped, update it there too.
- **Kapt warning** ("Kapt currently doesn't support language version 2.0+. Falling back to 1.9.") appears on every build. Harmless, not actionable without upgrading dependencies.

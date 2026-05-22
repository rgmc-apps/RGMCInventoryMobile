# RGMC Inventory v2

> **A fast, offline-first Android inventory management system built for the warehouse floor.**  
> Scan barcodes with your camera, track stock against NAV quantities in real time, collect personnel signatures, and push everything to the cloud — all from your phone.

---

## Table of Contents

- [Overview](#overview)
- [What's New in v2](#whats-new-in-v2)
- [Features](#features)
- [Screen Tour](#screen-tour)
  - [Home Menu](#home-menu)
  - [Scanner Setup](#scanner-setup)
  - [Inventory Dashboard](#inventory-dashboard)
  - [Barcode Scanner](#barcode-scanner)
  - [Products](#products)
  - [Notes](#notes)
  - [Signatures](#signatures)
  - [Export](#export)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Building the APK](#building-the-apk)
- [Installation](#installation)

---

## Overview

RGMC Inventory is the mobile companion app for the RGMC stock management system. Field auditors use it to:

1. **Pull down NAV quantities** from the server for a selected store and cut-off period
2. **Walk the floor and scan EAN-13 barcodes** — the app records every scan locally, shows live variance against NAV
3. **Collect signatures** from store personnel
4. **Push the completed inventory** back to the API and close the cut-off

The app is fully **offline-capable** — scans are written to a local SQLite database instantly. Internet is only required for the initial data pull and the final export.

---

## What's New in v2

v2 is a **full native Kotlin rewrite** of the original Xamarin.Forms app. Same business logic, same API — dramatically better hardware integration and a fraction of the APK size.

| | v1 (Xamarin) | v2 (Kotlin Native) |
|---|---|---|
| Language | C# | **Kotlin** |
| UI Framework | Xamarin.Forms | **Native Android Views** |
| Barcode Scanner | ZXing (Xamarin binding) | **Google ML Kit + CameraX** |
| Local Database | sqlite-net-pcl (3 separate .db3 files) | **Room** (single unified DB) |
| Networking | RestSharp | **Retrofit + OkHttp** |
| Signature Capture | SkiaSharp canvas | **Custom Android Canvas View** |
| APK Size | ~125 MB | **~30 MB** |
| Min Android | Android 6 | Android 7 (API 24) |
| Architecture | Xamarin MVVM | MVVM + ViewModel + StateFlow + Navigation Component |

---

## Features

### Barcode Scanning
- Real-time EAN-13 detection using **Google ML Kit** — no button press needed
- Live preview via **CameraX** with hardware-accelerated decode
- **Torch control** directly from the scan screen
- Configurable **quantity per scan** — scan once, record N units
- Instant audio feedback (beep) on successful capture
- Shows NAV qty, scanned qty, and variance for every scan

### Inventory Management
- Download NAV stock quantities per store/cut-off from the server
- Track scans by **location** and **rack** for granular placement records
- **Variance dashboard** — colour-coded red/green/grey so shortages jump out immediately
- Full-text search across barcodes and item descriptions
- Import existing scan records from the server mid-session

### Product Browser
- Browse products by **Brand → Item Group → Category** cascade
- Full-resolution product images pulled from the API
- Search by stock number or description

### Signatures
- Add any number of store personnel per inventory session
- Freehand **signature capture** on a smooth canvas
- Signatures stored as PNG bytes, exported to the server alongside inventory records

### Notes
- Lightweight notepad built into the app — useful for floor observations
- Create, edit, and delete notes; sorted by most recent

### Export & Cut-Off
- One-tap export: inventory records + barcodes + personnel signatures in a single operation
- Automatically **closes the cut-off** on the server after a successful export
- Device ID included on every record for full auditability

---

## Screen Tour

### Home Menu

```
╔══════════════════════════════╗
║       RGMC Inventory         ║
║            v2.0              ║
║                              ║
║  ┌──────────────────────┐   ║
║  │       Scanner        │   ║
║  └──────────────────────┘   ║
║  ┌──────────────────────┐   ║
║  │      Products        │   ║
║  └──────────────────────┘   ║
║  ┌──────────────────────┐   ║
║  │        Notes         │   ║
║  └──────────────────────┘   ║
║  ┌──────────────────────┐   ║
║  │     Signatures       │   ║
║  └──────────────────────┘   ║
║  ┌──────────────────────┐   ║
║  │       Export         │   ║
║  └──────────────────────┘   ║
╚══════════════════════════════╝
```

The home screen is your launchpad. Five clear entry points — no confusion, no hunting through menus.

---

### Scanner Setup

```
╔══════════════════════════════╗
║       Scanner Setup          ║
║──────────────────────────────║
║  Brand       [BRAND A    ▼]  ║
║  Customer    [Customer 1 ▼]  ║
║  Store       [SM North   ▼]  ║
║  Coordinator [John D.    ▼]  ║
║  Cut-Off     [2024-12    ▼]  ║
║  Location    [Floor 1    ▼]  ║
║  Rack        [  1  ]         ║
║  Encoder     [Jane Smith   ] ║
║                              ║
║  ┌──────────────────────┐   ║
║  │    Enter Inventory   │   ║
║  └──────────────────────┘   ║
╚══════════════════════════════╝
```

Cascading pickers — selecting a **Brand** narrows the Customer list, selecting a **Customer** narrows the Store list. The system loads all master data from the API on first open and caches locally.

---

### Inventory Dashboard

```
╔══════════════════════════════╗
║  NAV: 1,240 | Scan: 1,180    ║
║  Variance: -60               ║
║──────────────────────────────║
║  🔍 Search barcode/desc...   ║
║──────────────────────────────║
║  8850001234567               ║
║  Polo Shirt Blue L           ║
║  NAV: 10  Scanned: 8  -2     ║ ← red
║──────────────────────────────║
║  8850009876543               ║
║  Chinos Khaki 32             ║
║  NAV: 6   Scanned: 6   0     ║ ← grey
║──────────────────────────────║
║  8850005551234               ║
║  Dress Navy S                ║
║  NAV: 4   Scanned: 5  +1     ║ ← green
║──────────────────────────────║
║  [ Import ]  [ Scan Barcode ]║
╚══════════════════════════════╝
```

The header bar shows **total NAV vs scanned vs variance** at a glance. Every row is colour-coded: red for shortage, green for overage, grey for exact match.

---

### Barcode Scanner

```
╔══════════════════════════════╗
║  ┌────────────────────────┐  ║
║  │                        │  ║
║  │    [ LIVE CAMERA ]     │  ║
║  │                        │  ║
║  │   ┌──────────────┐     │  ║
║  │   │              │     │  ║
║  │   │   SCAN HERE  │     │  ║
║  │   │              │     │  ║
║  │   └──────────────┘     │  ║
║  │                        │  ║
║  └────────────────────────┘  ║
║──────────────────────────────║
║  8850001234567               ║
║  Polo Shirt Blue L   ₱ 1,299 ║
║  NAV: 10  Scanned: 9   -1    ║
║──────────────────────────────║
║  [ Back ]  Qty:[1]  [Torch]  ║
╚══════════════════════════════╝
```

Point the camera at a barcode — ML Kit detects it **automatically** (no button tap). The result card slides up immediately showing the item name, price, and live variance. Adjust `Qty` before scanning to log multiple units in one pass.

---

### Products

```
╔══════════════════════════════╗
║       Products               ║
║  Brand      [BRAND A    ▼]   ║
║  Item Group [Tops       ▼]   ║
║  Category   [Polo Shirts▼]   ║
║  ┌──────────────────────┐   ║
║  │    View Products     │   ║
║  └──────────────────────┘   ║
║══════════════════════════════║
║  🔍 Search...                ║
║  ┌──────────┐ ┌──────────┐  ║
║  │  [IMG]   │ │  [IMG]   │  ║
║  │ PS-001   │ │ PS-002   │  ║
║  │ Polo Blu │ │ Polo Wht │  ║
║  │ ₱ 1,299  │ │ ₱ 1,299  │  ║
║  └──────────┘ └──────────┘  ║
╚══════════════════════════════╝
```

Browse the full product catalogue with images. Filter down by brand, item group, and category — then search by stock number or description within results.

---

### Notes

```
╔══════════════════════════════╗
║  Notes                   [+] ║
║──────────────────────────────║
║  Floor count discrepancy     ║
║  2024-12-01 09:42            ║
║                         [🗑] ║
║──────────────────────────────║
║  Rack 3B — damaged items     ║
║  2024-12-01 10:15            ║
║                         [🗑] ║
║──────────────────────────────║
║  Coordinator arrived late    ║
║  2024-12-01 11:00            ║
║                         [🗑] ║
╚══════════════════════════════╝
```

A simple notepad for floor observations. Tap a note to edit it, swipe the delete button to remove it. Sorted newest-first.

---

### Signatures

```
╔══════════════════════════════╗
║  Signatures              [+] ║
║──────────────────────────────║
║  Maria Santos                ║
║  ✅ Signed        [Sign] [🗑]║
║──────────────────────────────║
║  Juan Cruz                   ║
║  ⬜ Not signed    [Sign] [🗑]║
╚══════════════════════════════╝

  ┌────────────────────────┐
  │  Juan Cruz             │
  │  Please sign below:    │
  │                        │
  │  ~~~~~~~~              │
  │       ~~~~             │
  │           ~~~~         │
  │                        │
  │ [Clear] [Save] [Cancel]│
  └────────────────────────┘
```

Add store personnel by name, then collect freehand signatures on a touch canvas. Blue ink, smooth strokes — signatures are stored as PNG bytes and uploaded with the inventory export.

---

### Export

```
╔══════════════════════════════╗
║  Export Inventory            ║
║                              ║
║  Cut-Off Date                ║
║  [2024-12                ▼]  ║
║                              ║
║                              ║
║  ┌──────────────────────┐   ║
║  │ Export All &         │   ║
║  │ Close Cut-Off        │   ║
║  └──────────────────────┘   ║
╚══════════════════════════════╝
```

One button sends everything — inventory records, scanned barcodes, and personnel signatures — to the server in sequence, then closes the cut-off date. If any step fails the error is shown and the cut-off stays open so you can retry safely.

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                      UI Layer                        │
│  MainActivity (single activity)                      │
│  ├── MainFragment          ├── ScannerSetupFragment  │
│  ├── ScannerInventoryFrag  ├── BarcodeScannerFrag    │
│  ├── ProductMainFragment   ├── ProductListFragment   │
│  ├── NotesFragment         ├── NoteEntryFragment     │
│  ├── SigningListFragment    ├── SigningFragment       │
│  └── ExportFragment                                  │
└────────────────────┬────────────────────────────────┘
                     │ observes StateFlow / SharedFlow
┌────────────────────▼────────────────────────────────┐
│                  ViewModel Layer                     │
│  ScannerViewModel  ProductViewModel  NoteViewModel   │
│  SigningViewModel  ExportViewModel                   │
└──────────┬─────────────────────────┬────────────────┘
           │ suspend fns             │ suspend fns
┌──────────▼──────────┐  ┌──────────▼──────────────┐
│   Repository Layer  │  │   Repository Layer       │
│  BrandRepository    │  │  StoreInventoryRepo      │
│  StoreRepository    │  │  ProductRepository       │
│  NoteRepository     │  │                          │
└──────────┬──────────┘  └──────────┬───────────────┘
           │                        │
┌──────────▼──────────┐  ┌──────────▼───────────────┐
│   Room (SQLite)     │  │   Retrofit + OkHttp       │
│  AppDatabase        │  │  ApiService               │
│  16 entities        │  │  rgmcinventoryapi-...     │
│  16 DAOs            │  │  .run.app/api/*           │
└─────────────────────┘  └──────────────────────────┘
```

**Single Activity** — `MainActivity` hosts the `NavHostFragment`. Navigation Component handles all back-stack and fragment transactions. ViewModels are scoped to the activity so scanner state (selected brand, store, cut-off) persists across the scanning flow.

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 2.0.21 |
| Build | Android Gradle Plugin | 8.7.0 |
| UI | Android Views + ViewBinding | — |
| Navigation | Navigation Component | 2.8.4 |
| State management | ViewModel + StateFlow | 2.8.7 |
| Local DB | Room | 2.6.1 |
| Networking | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Barcode | Google ML Kit Barcode Scanning | 17.3.0 |
| Camera | CameraX | 1.4.1 |
| JSON | Gson | 2.11.0 |
| Concurrency | Kotlin Coroutines | 1.9.0 |
| Min SDK | Android 7.0 (API 24) | — |
| Target SDK | Android 15 (API 35) | — |

---

## API Reference

All endpoints are relative to:
```
https://rgmcinventoryapi-935246372408.asia-southeast1.run.app/api/
```

| Method | Endpoint | Description |
|---|---|---|
| GET | `brand` | All brands |
| GET | `brand/coordinator` | All brand coordinators |
| GET | `customerstore` | All stores |
| GET | `store/customer` | All customers |
| GET | `storeinventorylocation` | All locations |
| GET | `itemgroup` | All item groups |
| GET | `category` | All categories |
| GET | `product/{categoryId}` | Products by category (with images) |
| GET | `storeinventory/navlist/{storeId}` | NAV quantities for store |
| POST | `storeinventory/barcodelist` | Pull existing barcodes for a cut-off |
| POST | `storeinventory/invlist` | Pull existing inventory for a cut-off |
| POST | `storeinventory/save/inventory` | Upload inventory records |
| POST | `storeinventory/save/barcode` | Upload barcode records |
| POST | `storeinventory/save/personnel` | Upload signed personnel records |
| GET | `storeinventorycutoff/{storeId}` | Cut-offs for a store |
| POST | `storeinventorycutoff/create` | Create a new cut-off |
| POST | `storeinventorycutoff/close` | Close a cut-off after export |
| GET | `storeinventory/appversion` | App version check |

---

## Database Schema

All data lives in a single Room database: `rgmc_inventory.db`

| Table | Key Columns | Purpose |
|---|---|---|
| `brands` | `brandId` | Brand master data |
| `categories` | `categoryId`, `brandId`, `itemGroupId` | Product categories |
| `item_groups` | `itemGroupId` | Item group master |
| `customer_stores` | `storeId`, `brandId`, `customerId` | Store list |
| `customers` | `customerBrand` (PK) | Customer-brand mapping |
| `brand_coordinators` | `brandCoor` (PK) | Coordinator per brand |
| `cut_off_dates` | `cutOff` (PK) | Available cut-off periods |
| `store_inventory_locations` | `locationId` | Floor locations |
| `store_inventory_cutoffs` | `storeCutOff` (PK) | Store × cut-off active flags |
| `store_inventory_nav` | `barcode`, `storeId` | NAV qty + running actual qty |
| `store_inventories` | auto PK, `barcode`, `storeId`, `cutOffDate` | Every individual scan |
| `barcodes` | auto PK, `text`, `storeId`, `cutOffDate` | Every scanned barcode record |
| `store_inventory_personnel` | auto PK, `storeId`, `cutOffDate` | Personnel + signature bytes |
| `product_images` | `stockNumber` (PK) | Product data + image bytes |
| `notes` | auto PK | Free-text notes |
| `settings` | `id = 1` (singleton) | Saved session state |

---

## Building the APK

**Requirements**
- JDK 17+
- Android SDK with build-tools 35.0.0 and platform android-35
- Gradle 8.14 (auto-downloaded via wrapper, or use an installed copy)

**Steps**

```bash
# Clone / navigate to project
cd C:\RGMC\Source\git\RGMCInventory-v2

# Build debug APK
gradle assembleDebug

# Or, if using the wrapper once set up
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

**Using the installed Gradle binary directly (Windows):**
```powershell
$gradle = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.14-bin\<hash>\gradle-8.14\bin\gradle.bat"
& $gradle assembleDebug --no-daemon
```

---

## Installation

### Via ADB (USB debugging)
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Manual install
1. Copy `app-debug.apk` to the Android device (USB, email, cloud storage)
2. On the device: **Settings → Install unknown apps** → allow your file manager
3. Open the APK file and tap **Install**

### Permissions requested at runtime
| Permission | Used for |
|---|---|
| `CAMERA` | Barcode scanning with ML Kit |
| `FLASHLIGHT` | Torch toggle on scan screen |
| `INTERNET` | API sync and export |
| `READ_MEDIA_IMAGES` | Product image access (Android 13+) |

---

> Built with Kotlin for RGMC · v2.0 · 2026

# MindWalk

A mindful walking companion for Android. MindWalk generates personalised walking routes through your city based on your mood and mindfulness preference — green spaces, quiet streets, or interesting sights — and tracks your progress with a leaf-collection journey board.

---

## Table of Contents

1. [Features](#features)
2. [Architecture overview](#architecture-overview)
3. [Tech stack](#tech-stack)
4. [Installation](#installation)
   - [Prerequisites](#prerequisites)
   - [Clone and open](#clone-and-open)
   - [Backend configuration](#backend-configuration)
   - [Build and run](#build-and-run)
   - [Install a pre-built APK](#install-a-pre-built-apk)
5. [User manual](#user-manual)
   - [First launch — city setup](#first-launch--city-setup)
   - [Home screen](#home-screen)
   - [Planning a walk](#planning-a-walk)
   - [Route preview](#route-preview)
   - [Active walk](#active-walk)
   - [Post-walk reflection](#post-walk-reflection)
   - [Journey screen](#journey-screen)
   - [Monthly goals](#monthly-goals)
   - [Saved routes](#saved-routes)
   - [Changing your city](#changing-your-city)
6. [Data flow diagram](#data-flow-diagram)
7. [Project structure](#project-structure)
8. [Generating documentation](#generating-documentation)
9. [Permissions](#permissions)

---

## Features

| Feature | Details |
|---|---|
| Route generation | Python backend on Azure selects mindful routes using a custom graph algorithm |
| Three mindfulness modes | **Green** (parks & nature), **Quiet** (low-traffic streets), **Sight** (landmarks & viewpoints) |
| Interactive map | OSMDroid tile map for location picking and route preview |
| Leaf collection | One leaf earned per completed walk; displayed in a visual board on the Journey screen |
| Monthly goals | Set a target for number of walks, distance (km), or active time (minutes) |
| Saved routes | Save and replay any generated route; delete when no longer needed |
| Walk history stats | Walks this month, favourite mode, progress toward goal |
| City switching | Change city at any time; backend re-indexes the new city graph automatically |

---



---

## Tech stack

| Layer | Library / Tool | Version |
|---|---|---|
| UI | Jetpack Compose + Material 3 | BOM 2024.06.00 |
| Navigation | Navigation Compose | 2.7.7 |
| ViewModel | Lifecycle ViewModel Compose | 2.7.0 |
| Map | OSMDroid | 6.1.18 |
| HTTP | Retrofit 2 + OkHttp | 2.9.0 / 4.12.0 |
| JSON | Gson | (via Retrofit converter) |
| Database | Room | 2.7.2 |
| Code generation | KSP | 2.2.10-2.0.2 |
| Documentation | Dokka | 2.0.0 |
| Language | Kotlin | 2.2.10 |
| Min SDK | Android 8.0 (API 26) | |
| Target SDK | Android 15 (API 35) | |

---

## Installation

### Prerequisites

- **Android Studio** Ladybug or newer (includes the required AGP 9.x toolchain)
- **JDK 17** (bundled with Android Studio — no separate install needed)
- **Android device or emulator** running Android 8.0+ (API 26+)
- Internet connection for map tiles and route generation

### Clone and open

```bash
git clone https://github.com/zdebska/MindWalk.git
cd MindWalk
```

Open the cloned folder in Android Studio: **File → Open → select the `MindWalk` directory**.

Wait for Gradle sync to complete (downloads ~200 MB of dependencies on first run).

### Backend configuration

MindWalk sends route requests to a Python backend hosted on Azure. The base URL is hard-coded in `PythonRouteService.kt`. If you are running your own backend instance, update the `BASE_URL` constant in that file:

```kotlin
// app/src/main/java/com/example/mindwalk/service/PythonRouteService.kt
private const val BASE_URL = "http://<your-azure-vm-ip>:5000/"
```

If the backend is unreachable, route generation fails with an error dialog. An `OsrmService` class exists in the codebase but is not wired as an automatic fallback — it is retained as dead code from an earlier prototype.

### Build and run

**From Android Studio:**

1. Connect your Android device via USB and enable **USB debugging** in Developer Options, or start an AVD emulator.
2. Select your device in the toolbar device dropdown.
3. Click **Run ▶** (Shift+F10).

**From the command line (Windows):**

```powershell
.\gradlew.bat assembleDebug
# APK is written to app\build\outputs\apk\debug\app-debug.apk

# Install directly to a connected device
.\gradlew.bat installDebug

# Launch on device
adb shell am start -n com.example.mindwalk/.MainActivity
```

**Release build** (unsigned, minification disabled):

```powershell
.\gradlew.bat assembleRelease
# APK at app\build\outputs\apk\release\app-release-unsigned.apk
```

To sign the release APK for distribution, configure a keystore in `app/build.gradle.kts` under `signingConfigs`.

### Install a pre-built APK

If you have an `.apk` file:

```bash
adb install app-debug.apk
```

Or transfer the APK to your device, open it from Files, and follow the on-screen prompts (you may need to allow installation from unknown sources in Settings → Security).

---

## User manual

### First launch — city setup

On the very first launch, MindWalk shows the **City Setup** screen.

1. Type your city name in the search box (e.g. `Brno`) — suggestions appear as you type via Nominatim.
2. Select your city from the suggestion list, or tap **Use my location** to detect it automatically via GPS.
3. Tap **Set up my city**. MindWalk contacts the backend to index the city's street graph (this takes 30–90 seconds depending on city size). A progress message is shown during indexing.
4. Once ready, you land on the **Home screen**.

Your city is saved and the setup screen never appears again unless you change city manually.

---

### Home screen

The Home screen shows:

- A **greeting** and the current date.
- Your **active city** with a pencil icon — tap it to change city.
- **Preset walk cards** — one-tap shortcuts for common walks:
  - *Touch the grass* — 10 min, Green mode
  - *City look* — 20 min, Sight mode
  - *Calm walk* — 15 min, Quiet mode
  - *Mind reload* — 60 min, Green mode
- **Bottom navigation bar** (Home / Journey / Saved).

Tap any preset card to jump directly to the Plan screen with those settings pre-filled.

---

### Planning a walk

Tap **Plan a walk** or any preset to open the **Plan Your Walk** screen.

| Setting | Description |
|---|---|
| **Duration** | Drag the slider to select walk length (10–60 minutes) |
| **Mindfulness mode** | Green · Quiet · Sight — affects which streets/areas the route prefers |
| **Start location** | Tap the location row to open the map picker, or uses your GPS position |

Tap **Generate route** to request a route from the backend. A loading indicator is shown while the route is computed.

---

### Route preview

The **Route Preview** screen shows the generated route on a full-screen map.

- The route polyline is drawn in green.
- **Start** pin is shown.
- Route stats are displayed at the bottom: distance (km), estimated duration (min) and vibe.
- Tap **New route** to request an alternative route with a different random seed.
- Tap **Start walk** to begin the active walking session.
- Tap **Edit** to change route settings.

---

### Active walk

The **Walking screen** shows:

- The route map with your progress.
- Distance and time remaining.
- Distance you've already walked.
- **Mindful prompts** — periodic gentle reminders to notice your surroundings.
- Tap **End walk** to end the session early, or the walk ends automatically when you reach the destination.

---

### Post-walk reflection

After finishing a walk, the **Reflection screen** appears.

1. Select your mood from the emoji grid (Great / Good / Okay / Not great).
2. Optionally name and save the route for future reuse.
3. Tap **Complete walk** to return Home. A new **leaf** is added to your Journey board.

---

### Journey screen

The **Journey screen** has three sections:

**Stats row** — shows total completed walks, walks completed this month and favourite mode.

**Goal card** — shows your monthly goal progress (see [Monthly goals](#monthly-goals) below).

**Leaf collection board** — every completed walk earns one leaf. Leaves are displayed in a grid with randomised colours, sizes, and rotations. Scroll within the board to see all leaves.

---

### Monthly goals

Tap **Set a goal** (or **Change** on an active goal card) to open the **Set Goal screen**.

1. Choose a goal type:
   - **Walks** — target number of walks this month
   - **Distance** — target total distance in km
   - **Time** — target total active minutes
2. Drag the slider to set your target value. A context hint shows the equivalent weekly rate.
3. Tap **Save goal** (or **Update goal** if one already exists).

The goal card on the Journey screen shows an animated progress bar that fills as you complete walks. Progress is calculated from planned route data (distance and duration from the generated route).

---

### Saved routes

The **Saved Routes** screen lists all routes you have saved.

Each card shows:
- Route name and start location
- Distance, duration, and mindfulness mode badge
- The mood emoji logged at save time
- Date saved

Tap a card to load the route back into the Plan screen for re-walking.  
Tap the **delete icon** to remove a route permanently.

---

### Changing your city

From the Home screen, tap the **pencil icon** next to your city name. The city selection screen opens with your current city pre-filled. Search for and select a new city, then tap **Change city**. The backend re-indexes the new city's graph before returning you to Home.

---


---

## Data flow diagram

```
User input
    │
    ▼
PlanYourWalkScreen
    │  (duration, mode, shape, start/end coords)
    ▼
PlanViewModel.generatePythonRoute()
    │
    ├──► PythonRouteService ──► Azure Python backend
    │         │                   (graph-based route)
    │         │ success ◄─────────────────────────────
    │         │
    │         └── failure ──► routeError = true (error dialog shown to user)
    │
    ▼
RoutePreviewData (polyline points, distance, duration)
    │
    ▼
RoutePreviewScreen (map + stats)
    │
    ▼  (Start walk)
WalkingScreen
    │
    ▼  (Finish)
PostWalkReflectionScreen
    │
    ├── WalkRecord saved ──► WalkRecordDao ──► Room DB
    └── SavedRoute (optional) ──► SavedRouteDao ──► Room DB
            │
            ▼
    WalkHistoryViewModel reads WalkRecord list
            │
            ├── walksThisMonth, distanceThisMonthKm, timeThisMonthMin
            └── compared against MonthlyGoal (from GoalPreferences)
                        │
                        ▼
                JourneyScreen (stats + progress bar + leaf board)
```

---

## Project structure

```
MindWalk/
├── app/src/main/java/com/example/mindwalk/
│   ├── MainActivity.kt               # Single Activity, sets up NavHost
│   ├── PlanOsmScreen.kt              # OSMDroid prototype (not in nav graph)
│   │
│   ├── data/                         # Data models, DAOs, Room DB, preferences
│   │   ├── MindWalkDatabase.kt       # Room database definition
│   │   ├── RouteRepository.kt        # Mediates DAOs and service calls
│   │   ├── SavedRoute.kt / SavedRouteDao.kt
│   │   ├── WalkRecord.kt / WalkRecordDao.kt
│   │   ├── CityPreferences.kt        # SharedPreferences: active city
│   │   ├── GoalPreferences.kt        # SharedPreferences: monthly goal
│   │   ├── MonthlyGoal.kt            # Goal data class (type + target)
│   │   ├── GeoModels.kt / PlanModels.kt / PrepareModels.kt / Converters.kt
│   │
│   ├── service/                      # Network and routing services
│   │   ├── RouteApi.kt               # Retrofit interface for Azure backend
│   │   ├── PythonRouteService.kt     # Primary route service (Azure)
│   │   ├── OsrmService.kt            # Fallback route service (OSRM)
│   │   ├── NominatimService.kt       # City geocoding (Nominatim)
│   │   └── LoopHeuristics.kt         # Geometric loop waypoint helper
│   │
│   ├── navigation/
│   │   ├── Routes.kt                 # Screen route constants
│   │   └── AppNav.kt                 # NavHost wiring all destinations
│   │
│   ├── ui/
│   │   ├── screens/                  # One file per screen composable
│   │   │   ├── HomeScreen.kt
│   │   │   ├── OnboardingScreen.kt
│   │   │   ├── PlanYourWalkScreen.kt
│   │   │   ├── RoutePreviewScreen.kt
│   │   │   ├── LocationPickerScreen.kt
│   │   │   ├── WalkingScreen.kt
│   │   │   ├── PostWalkReflectionScreen.kt
│   │   │   ├── JourneyScreen.kt
│   │   │   ├── SetGoalScreen.kt
│   │   │   ├── SavedRoutesScreen.kt
│   │   │   └── OsmRouteMap.kt        # Reusable map composable
│   │   │
│   │   ├── components/               # Reusable composables
│   │   │   ├── BottomNavBar.kt
│   │   │   ├── PrimaryButton.kt
│   │   │   ├── SectionCard.kt
│   │   │   └── ChoiceChipRow.kt
│   │   │
│   │   ├── viewmodel/
│   │   │   ├── OnboardingViewModel.kt
│   │   │   ├── PlanViewModel.kt
│   │   │   ├── WalkHistoryViewModel.kt
│   │   │   └── SavedRoutesViewModel.kt
│   │   │
│   │   ├── theme/
│   │   │   ├── Color.kt              # Colour palette tokens
│   │   │   ├── Type.kt               # Typography
│   │   │   └── Theme.kt              # MindWalkTheme composable
│   │   │
│   │   └── util/
│   │       └── GeoConverters.kt      # Point ↔ GeoPoint extensions
│   │
├── docs/                             # Dokka-generated HTML documentation
│   └── index.html                    # Open in browser to browse API docs
├── apk/                              # 
│   └── MindWalk.apk                  # Apk file ready to be installed on device
│
├── build.gradle.kts                  # Root build script (plugin versions)
├── app/build.gradle.kts              # App module build script
└── README.md
```

---

## Generating documentation

API documentation is pre-generated in the `docs/` folder. Open `docs/index.html` in any browser.


The docs cover all public classes, functions, and properties with KDoc descriptions.

---

## Permissions

| Permission | Why it is needed |
|---|---|
| `INTERNET` | Route generation (Azure backend), map tiles (OSMDroid), geocoding (Nominatim) |
| `ACCESS_NETWORK_STATE` | Detects offline state before making network calls |
| `ACCESS_FINE_LOCATION` | Precise GPS for "Use my location" and walk start detection |
| `ACCESS_COARSE_LOCATION` | Faster network-based location as a fallback |

Location permission is requested at runtime when the user taps **Use my location**. No location data is transmitted to any server — it is used only on-device to resolve a city name via the Nominatim reverse geocoder.

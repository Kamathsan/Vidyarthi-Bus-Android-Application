# 🚌 Vidyarthi-Bus: Transit Intelligence

![Vidyarthi-Bus Banner](https://img.shields.io/badge/Status-Complete-success) ![Kotlin](https://img.shields.io/badge/Kotlin-100%%-blue) ![Android](https://img.shields.io/badge/Platform-Android-green)

Vidyarthi-Bus is a crowdsourced transit intelligence Android application designed to provide university students with real-time public transport capacity estimations and dynamic routing.

## 📌 Problem Statement
University students relying on public transport face a recurring daily challenge: uncertainty regarding bus capacity. Traditional transit applications only provide scheduled arrival times. Consequently, students often wait extended periods at bus stops only to find that the arriving bus is completely full, resulting in tardiness, missed lectures, and overall dissatisfaction. There is a distinct lack of real-time, crowd-sourced occupancy data for local transit systems.

Vidyarthi-Bus introduces a peer-to-peer data-sharing model to estimate bus capacity, empowering waiting students to make informed decisions about whether to wait, take alternative transport, or delay their departure.

## ✨ Key Features
- **Dynamic Campus Selection:** Users can select their specific university campus (e.g., NITK, MIT, St. Aloysius) from a persistent dashboard.
- **Bus-Specific Real-Time Tracking:** The system identifies exactly which route numbers travel to the selected campus and isolates data per vehicle.
- **Crowdsourced Occupancy Meter:** A dynamic, multi-tier progress indicator displaying real-time bus capacity based on recent student reports.
- **Multi-Bus Visibility:** Simultaneous crowd tracking for both the immediate next arrival and the subsequent following bus.
- **Smart Routing (OSRM API):** Integration with the Open Source Routing Machine to draw precise road geometries from the user's location to the campus.
- **Automated Data Purging:** Crowd reports automatically expire and are purged from the database after 15 minutes to ensure live accuracy.

## 🛠 Tech Stack
- **Frontend / Core:** Kotlin, Android SDK, Android Studio, XML Constraint Layouts, Material Design 3.
- **Backend / Cloud:** Firebase Realtime Database (for low-latency JSON data sync), Firebase Authentication.
- **Mapping & GIS:** OSMDroid (OpenStreetMap API) for map rendering, Project-OSRM (Open Source Routing Machine API) for GeoJSON road path generation.
- **Networking:** Kotlin Coroutines (`Dispatchers.IO`), `HttpURLConnection`, Native JSON Parsing.

## 📸 Screenshots & Demo
<img width="1920" height="1080" alt="Untitled design(1)" src="https://github.com/user-attachments/assets/5a621822-8b7e-4b17-bac3-aa7fb4804f05" />
 Dashboard
<img width="1080" height="2400" alt="app3" src="https://github.com/user-attachments/assets/ee8c672e-4bec-4b0b-ac36-eee96aa3c657" />
Live Map & Routing
<img width="1034" height="1080" alt="Untitled design" src="https://github.com/user-attachments/assets/c3bda266-6e42-4d37-8dd0-104caaff4c81" />
Multi-Bus Crowd Meter

## 🚀 Setup & Installation Instructions
To run this project locally on your machine, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Kamathsan/Vidyarthi-Bus-Android-Application.git
   ```
2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select `File > Open` and navigate to the cloned directory.
3. **Sync Gradle:**
   - Wait for Android Studio to index the files and download required dependencies.
   - If prompted, click `Sync Project with Gradle Files`.
4. **Run the Application:**
   - Connect a physical Android device via USB or start an Android Emulator.
   - Click the green **Run (▶)** button in the top toolbar or use the keyboard shortcut `Shift + F10`.

## 📁 Folder Structure
The core application logic is structured as follows:
```text
app/src/main/
├── java/com/vidyarthibus/app/
│   ├── AuthActivity.kt       # Firebase Authentication & Login flow
│   ├── HomeActivity.kt       # Dashboard & Campus Selection
│   ├── MainActivity.kt       # Core Map, OSRM Routing, & Firebase Sync
│   └── SettingsActivity.kt   # User Preferences & Cache management
├── res/
│   ├── layout/               # XML Material Design UI files
│   ├── drawable/             # Custom vectors and icons
│   ├── values/               # Colors, Strings, and Themes
└── AndroidManifest.xml       # App configurations & Permissions
```

## 🔮 Future Improvements
- **Live GPS Tracking:** Expand functionality to include GPS-based live tracking of the vehicle itself (combining spatial data with occupancy data).
- **Gamification:** Introduce a points system to reward users who frequently contribute accurate crowd reports.
- **Route Expansion:** Scale the hardcoded campus-bus mappings into a fully dynamic backend database covering the entire state.

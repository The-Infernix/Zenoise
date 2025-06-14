
# ZENIOSE (📍Noise Pollution Heatmap via Smartphones)

A crowdsourced real-time noise pollution monitoring system using a mobile app and web heatmap. Built with Leaflet.js, MapTiler SDK, and Firebase Firestore, this project visualizes noise levels and helps users navigate quieter routes using a smart, interactive map.

## 🔥 Features

   - 📡 Real-time heatmap of noise pollution from user-submitted data

   - 🔎 Location search with Nominatim

   - 🧭 Live user location tracking

   - 🚗 Route guidance with Leaflet Routing Machine

   - 🔄 Gesture-based map rotation (just like Google Maps!)

   - ☁️ Firestore-based backend for live data sync

## 🗺️ Tech Stack

| Layer       | Technology                      |
| ----------- | ------------------------------- |
| Frontend    | Leaflet.js + MapTiler SDK       |
| Realtime DB | Firebase Firestore              |
| Location    | Geolocation API + Nominatim API |
| Routing     | Leaflet Routing Machine         |
| Heatmap     | Leaflet.heat plugin             |



## 📦 Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/quote-generator.git
2. Configure Firebase

 Update the Firebase config in your HTML/JS file:

```js
  const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "YOUR_PROJECT_ID.firebaseapp.com",
  projectId: "YOUR_PROJECT_ID"
};
```
3. Get a MapTiler API Key

   Sign up at [MapTiler](https://cloud.maptiler.com) Cloud

   Copy your **API** Key and replace in the code

    ```
    apiKey: "YOUR_MAPTILER_KEY"
    ```

## 🧪 Live Demo

- ⚡ View the heatmap in action by opening heatmap.html in a browser. On mobile, try rotating the map with two fingers! 


## 🚀 How It Works

- Mobile app records noise levels (in dB) and uploads data to Firestore.

- Web heatmap fetches this data every 10 seconds and renders colored intensity.

- Users can track their own position, search for destinations, and get routes.

- Noisy areas appear hotter; quieter zones are cooler.

- Map can be rotated on touch-enabled devices for easier orientation.

## 🔐 Privacy Considerations

   - No personal user data is stored.

   - Geolocation access is opt-in.

   - Noise data is anonymized and only contains location + decibel info.

## 📈 Future Work

   - Smart route recommendation (avoid noisy zones)

   - Heatmap playback (historical noise trends)

   - Noise classification (traffic, construction, etc.)

   - User reputation + flagging invalid readings


## 🙌 Acknowledgements

   - [Leaflet.js](https://leafletjs.com/)

   - [MapTiler](https://www.maptiler.com/)

   - [Firebase Firestore](https://firebase.google.com/)

   - [OpenStreetMap & Nominatim](https://nominatim.org/)

   - [Leaflet Routing Machine](https://www.liedman.net/leaflet-routing-machine/)

   - [Leaflet.heat](https://github.com/Leaflet/Leaflet.heat)

## 📜 License

MIT License – Feel free to fork and contribute.


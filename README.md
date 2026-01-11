# 💬 Chat Messenger App

A modern, real-time chat application for Android, built with Kotlin and a robust Firebase backend. This app provides a seamless messaging experience with features like user authentication, live presence status, image messaging, and push notifications.

---

## ✨ Features

- **👤 User Authentication:** Secure sign-up and sign-in using Firebase Authentication.
- **💬 Real-Time Messaging:** Instant message delivery powered by Firestore's real-time listeners.
- **🖼️ Image Messaging:** Send and receive images in chats, with images uploaded to Firebase Storage.
- **🟢 Online/Offline Presence:** See the live status of other users, thanks to a hybrid Firebase Realtime Database and Firestore implementation.
- **🔔 Push Notifications:** Receive custom notifications for new messages, even when the app is in the background, with the ability to reply directly from the notification.
- **🔧 Profile Customization:** Users can update their display name and profile picture.

---

## 🛠️ Tech Stack & Architecture

This project is built with a modern Android architecture and leverages the full power of the Firebase suite.

- **Language:** [Kotlin](https://kotlinlang.org/)
- **Architecture:** MVVM (Model-View-ViewModel)
- **UI:** Android Views & XML, with Jetpack Navigation for fragment management.
- **Backend Services:**
    - **Firebase Authentication:** For user management.
    - **Firebase Firestore:** Primary database for storing user info, messages, and recent chat lists.
    - **Firebase Realtime Database:** Used specifically for its reliable connection-state management to power the user presence system.
    - **Firebase Storage:** For hosting all user-uploaded images (profile pictures and chat images).
    - **Firebase Cloud Messaging (FCM):** For sending push notifications.
- **Image Loading:** [Glide](https://github.com/bumptech/glide)
- **Networking:** [Retrofit](https://square.github.io/retrofit/) (for potential future API integrations).
- **UI Components:** [Material Design Components](https://material.io/develop/android)

---

## 🚀 Getting Started

To build and run this project, you will need to set up your Firebase project first.

### 1. Firebase Setup (Required)

This project uses Firebase. The file `app/google-services.json` is intentionally excluded from Git.

#### How to get the file

1.  Ask the project owner or team lead for `google-services.json`, or create a new project in the [Firebase Console](https://console.firebase.google.com/).
2.  If creating a new project, add an Android app with the package name `com.example.chatmessengerapp`.
3.  Download the `google-services.json` file.

#### Where to place it

Place the downloaded file in the `app/` directory of the project:

```
YourProject/
└── app/
    ├── src/
    └── google-services.json  <-- Place it here
```

### 2. Build the App

Once the `google-services.json` file is in place, you can build and run the app on an Android device or emulator.

---

## ⚙️ How It Works

### Real-Time Presence System

The app uses a hybrid approach to track user status:

1.  **Realtime Database:** When a user opens the app, a connection is made to the Firebase Realtime Database. The special `.info/connected` node provides an instant, server-verified connection status.
2.  **Last Will:** The app sets a "last will" on the Realtime Database to write `"Offline"` to the user's status node if they disconnect unexpectedly (e.g., app crash, loss of network).
3.  **Firestore Sync:** When the status in the Realtime Database changes (to `"Online"` or `"Offline"`), a listener in the app writes this status to the user's document in **Firestore**. The UI listens for live changes on the Firestore document, ensuring the status is always up-to-date.

### Push Notifications

Notifications are designed to work even when the app is in the background.

- **FCM Data Messages:** The system relies on sending **Data Messages** (not Notification Messages) from the server. This ensures that the message is always delivered to the app's `FirebaseService`.
- **Custom Handling:** The `onMessageReceived` function in `FirebaseService.kt` builds a custom notification, complete with a "Reply" action, giving the user a rich experience outside the app.

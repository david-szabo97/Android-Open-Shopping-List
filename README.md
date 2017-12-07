# Open Shopping List

![](https://i.imgur.com/rbqwXdk.gif)

# Prerequisites
- **Firebase Project**
  - Sign up at https://firebase.google.com/
  - Add a new project
  - Open your new project
  - Add Android app to the project
    - **Android package name**: com.messedcode.openshoppinglist
    - **App nickname**: Open Shopping List
    - **Debug signing certificate SHA-1**: *LEAVE EMPTY*
  - Download configuration file
  - Move configuration file to `/app/src/google-services.json`

# Use Case Goals
Make this application usable in public. (Publishing to Play Store.)

- [ ] Add user authentication
- [ ] Make lists' shareable
- [ ] Copying public lists to your own workspace

# Development Goals
- [ ] Setup proper Proguard
- [ ] Rewrite using MVP
- [ ] Decouple from Firebase
- [ ] Write backend server (NodeJS)
- [ ] Move notifications to backend (right now we are sending them directly from the app which exploits our server key)
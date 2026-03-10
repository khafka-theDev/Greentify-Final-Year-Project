# 🌱 Greentify

## Table of Contents
- [General Info](#general-info)
- [Interfaces](#interfaces)
- [Features](#features)
- [Technologies](#technologies)
- [System Architecture](#system-architecture)
- [Database Model](#database-model)
- [Future Improvements](#future-improvements)

---

# General Info

**Greentify** is a mobile recycling application developed for Android that encourages environmentally friendly behavior through gamification elements such as **points, badges, rewards, and leaderboards**.

The application allows users to record their recycling activities, upload proof of recycling, and earn points based on the **weight and type of materials recycled**. These points can later be redeemed for rewards while also allowing users to compete with others through a **global leaderboard and social features**.

Greentify was developed as part of a **Final Year Project (FYP)** titled:

> **"Preserving Green Environment Through Recycling and Gamification"**

The goal of this project is to **promote sustainable behavior and increase recycling participation** by making recycling more engaging and interactive for users.

---
# Interfaces


Register:
![register](https://github.com/user-attachments/assets/cac67ad2-47ba-4b9d-93ab-f7b8e6d096b7)

Login:
![login](https://github.com/user-attachments/assets/99527f97-0f53-4059-9c37-8b2a0424bdad)

Homepage, you can choose material to start recycling from this page:
![Homepage](https://github.com/user-attachments/assets/8cbfc0a5-dc19-4bbf-aa53-22a17944dca8)

Choose recycling facilites page (to start recycle activity):
![choose recycling facilities](https://github.com/user-attachments/assets/854be737-0c83-4bdf-bf9d-89ff61364e51)

Upload recycling proof page:
![upload recycling proof](https://github.com/user-attachments/assets/93aa82c5-fec5-421a-9f27-20338ead2506)

Profile:
![profile](https://github.com/user-attachments/assets/2c08db1f-467c-425c-b984-2ba9bbdc995b)

Badges level pop-up on profile (clickable feature):
![profile pop up](https://github.com/user-attachments/assets/6a2422f4-ef9a-4ba0-ab06-be2a26227841)

Track recycling progress (bottom part of profile page):
![track recycling progress](https://github.com/user-attachments/assets/400b5114-9606-4e5e-9e1e-9849c18eeacd)

View points received page:
![POINTS RECEIVE](https://github.com/user-attachments/assets/7f78dde2-dfca-4a5e-81d2-ddfe6c7b02dc)

Claim reward page:
![claim reward 2](https://github.com/user-attachments/assets/e981d019-2a5f-4af0-88a1-cf795e141371)

Leaderboard ranking page:
![leaderboard](https://github.com/user-attachments/assets/14b3f28a-ce3c-4c3c-bb05-ca334833efb9)

Add friend page:
![AD FRIEND](https://github.com/user-attachments/assets/8d6e08da-4773-4200-b507-ce39bab23047)

Logout and tips page:
![logut](https://github.com/user-attachments/assets/3f7c4872-9a70-4898-8714-5b66321b5aa4)

---

# Features

## User Authentication
- Firebase Authentication
- Email and password registration/login
- Secure user account management

---

## Recycling Submission

Users can submit recycling activities by:

- Selecting recycling material types:
  - Paper
  - Plastic
  - E-Waste
- Finding nearby recycling centers using **Google Maps API**
- Uploading proof of recycling (image)
- Entering the **weight of recycled materials**

Points are automatically calculated based on the recycled material.

---

## Gamification System

Greentify integrates several gamification elements to motivate users.

### Points System
- Users earn points based on recycling weight
- Points accumulate as **Green Credits**

### Badges
- Users unlock achievements based on recycling milestones

### Reward System
- Users can redeem their points for rewards
- Rewards are managed by the admin system

### Leaderboard
- Global ranking system
- Users can compare their recycling contributions with others

---

## Social Features
- Add and manage friends
- Compare recycling achievements with friends
- View public leaderboard rankings

---

## Recycling Facility Finder
- Uses **Google Maps API**
- Detects user's current location
- Displays nearby recycling facilities
- Provides navigation via Google Maps

---

# Technologies

## Mobile Development
- Kotlin
- Java
- Android Studio
- XML UI Layout

## Backend & Cloud Services
- **Firebase Authentication** – user login and account management
- **Firebase Realtime Database** – leaderboard and real-time data
- **Firebase Firestore** – recycling history and app data storage
- **Cloudinary** – image storage for recycling proof

## APIs
- Google Maps API
- Google Places API
- Fused Location Provider API

---

# System Architecture

Greentify uses a **cloud-based architecture** where Firebase services manage authentication and real-time data while Cloudinary handles image storage.


User
│
▼
Android Application (Kotlin/Java)
│
├── Firebase Authentication
│
├── Firebase Firestore
│ └── Recycling submissions
│
├── Firebase Realtime Database
│ └── Leaderboard & user points
│
└── Cloudinary
└── Recycling proof images


---

# Database Model

## Firestore Structure
users
└── userId
├── username
├── email
├── greenCredits
└── badges

recycling_submissions
└── submissionId
├── userId
├── materialType
├── weight
├── pointsEarned
├── recyclingCenter
└── imageUrl


## Realtime Database Example
leaderboard
└── userId
├── username
├── points
└── avatarUrl


---

# Getting Started

## Prerequisites

You will need the following tools installed:

- Android Studio
- Android SDK
- Emulator or physical Android device
- Firebase project
- Cloudinary account

---
Future Improvements

Planned improvements for the application include:

AI-based waste classification using camera detection

Advanced analytics for recycling behavior

Push notifications for recycling reminders

Expansion of social features

Integration with real-world recycling organizations

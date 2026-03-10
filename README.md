# 🌱 Greentify

## Table of Contents
- [General Info](#general-info)
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

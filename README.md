# LyricLens
A powerful lyric analysis and playlist generation tool that integrates with Spotify to visualize the emotional landscape of your music.

## Overview
LyricLens is a full-stack application designed to analyze the sentiment and emotional content of song lyrics. By integrating seamlessly with Spotify, it allows users to analyze their existing playlists and generate new ones based on specific emotional criteria. Whether you're looking for upbeat tracks or melancholic melodies, LyricLens helps you curate the perfect vibe.

## Features
- 🎵 **Lyric Analysis**: Deep dive into song lyrics to extract sentiment polarity and subjectivity using NLP.
- 🎧 **Spotify Integration**: Secure OAuth2 authentication to fetch user playlists and save generated ones directly to your account.
- 📊 **Interactive Dashboard**: Visualize analysis results with dynamic charts and graphs.
- ⚡ **Modern Architecture**: Built with a high-performance FastAPI backend and a responsive React frontend.
- 🛡️ **Secure**: Robust authentication and token management.

## Architecture
LyricLens consists of two main components:

### Backend (`/backend`)
- **Framework**: FastAPI (Python)
- **Database**: SQLite (with SQLAlchemy ORM)
- **Analysis**: TextBlob for NLP/Sentiment Analysis
- **External API**: Spotipy for Spotify Web API interaction

### Frontend (`/client`)
- **Framework**: React (Vite)
- **Styling**: TailwindCSS
- **Routing**: React Router
- **State Management**: React Hooks

## Quick Start

### Prerequisites
- Python 3.8+
- Node.js 16+
- Spotify Developer Account (for Client ID and Secret)

### 1. Setup Backend
Navigate to the backend directory and set up the environment:

```bash
cd backend
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

Create a `.env` file in `backend/` with your credentials:
```env
SPOTIFY_CLIENT_ID=your_client_id
SPOTIFY_CLIENT_SECRET=your_client_secret
SPOTIFY_REDIRECT_URI=http://localhost:8000/callback
SECRET_KEY=your_secret_key
```

Run the server:
```bash
uvicorn app.main:app --reload
```
The API will be available at `http://localhost:8000`.

### 2. Setup Client
Navigate to the client directory and install dependencies:

```bash
cd client
npm install
```

Run the development server:
```bash
npm run dev
```
The application will be available at `http://localhost:5173`.

## Project Structure
```
lyriclens/
├── backend/             # FastAPI Backend
│   ├── app/
│   │   ├── routers/     # API Endpoints (auth, playlists, analysis)
│   │   ├── models.py    # Database Models
│   │   └── main.py      # Application Entry Point
│   ├── requirements.txt # Python Dependencies
│   └── lyriclens.db     # SQLite Database
├── client/              # React Frontend
│   ├── src/
│   │   ├── pages/       # Page Components
│   │   └── main.jsx     # Entry Point
│   ├── package.json     # Node Dependencies
│   └── tailwind.config.js
└── README.md            # Project Documentation
```

## Building and Testing

### Backend
Run tests using pytest (if configured):
```bash
cd backend
pytest
```

### Frontend
Build for production:
```bash
cd client
npm run build
```
Preview the build:
```bash
npm run preview
```

## Dependencies
- **FastAPI**: High-performance web framework for building APIs.
- **Uvicorn**: ASGI web server implementation.
- **SQLAlchemy**: SQL toolkit and Object Relational Mapper.
- **TextBlob**: Simple, Pythonic text processing.
- **React**: Library for building user interfaces.
- **Vite**: Next Generation Frontend Tooling.
- **TailwindCSS**: Utility-first CSS framework.

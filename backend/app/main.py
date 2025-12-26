from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .models import init_db
from .routers import auth, playlists, analysis, export

app = FastAPI(title="LyricLens Backend")

# CORS
origins = [
    "http://localhost:5173", # Vite default
    "http://127.0.0.1:5173",
    "http://localhost:3000",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
def on_startup():
    init_db()

@app.get("/")
def read_root():
    return {"message": "Welcome to LyricLens API"}

app.include_router(auth.router)
app.include_router(playlists.router)
app.include_router(analysis.router)
app.include_router(export.router)

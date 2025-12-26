// Updated API utility for FastAPI backend
const API_BASE_URL = "http://127.0.0.1:8000";

export async function analyzeLyrics({ lyrics, song, artist }) {
  // This endpoint might not be directly used if we analyze by playlist, 
  // but keeping it for compatibility if needed, though the backend doesn't have it exactly.
  // We'll assume this is for single track analysis if we add it later.
  console.warn("Single track analysis not fully implemented in backend yet.");
  return {};
}

export async function getSpotifyUserAndPlaylists(userId) {
  const response = await fetch(`${API_BASE_URL}/auth/users/${userId}`);
  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Status: ${response.status} ${text}`);
  }
  return await response.json();
}

export async function analyzePlaylists(userId, playlistIds) {
  const response = await fetch(`${API_BASE_URL}/analyze/`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ user_id: userId, playlist_ids: playlistIds }),
  });
  if (!response.ok) {
    throw new Error("Failed to analyze playlists");
  }
  return await response.json();
}

export async function createBatchPlaylists(userId, playlists) {
  const response = await fetch(`${API_BASE_URL}/playlists/batch-create`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ user_id: userId, playlists }),
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Status: ${response.status} ${text}`);
  }
  return await response.json();
}
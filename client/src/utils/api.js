// Placeholder for API utility
export async function analyzeLyrics({ lyrics, song, artist }) {
  const response = await fetch("http://localhost:8080/api/lyrics/analyze", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ lyrics, song, artist }),
  });
  if (!response.ok) {
    throw new Error("Failed to analyze lyrics");
  }
  return await response.json();
}

export async function getSpotifyUserAndPlaylists(userId) {
  const response = await fetch(`http://localhost:8080/auth/spotify/api/spotify/user/${userId}`);
  if (!response.ok) {
    throw new Error("Failed to fetch Spotify user data");
  }
  return await response.json();
}

export async function analyzePlaylists(userId, playlistIds) {
  const response = await fetch("http://localhost:8080/auth/spotify/analyze-playlists", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ userId, playlistIds }),
  });
  if (!response.ok) {
    throw new Error("Failed to analyze playlists");
  }
  return await response.json();
} 
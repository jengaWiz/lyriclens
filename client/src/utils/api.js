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
import React, { useState } from "react";

const LyricsForm = ({ onAnalyze, loading }) => {
  const [song, setSong] = useState("");
  const [artist, setArtist] = useState("");
  const [lyrics, setLyrics] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!lyrics.trim()) return;
    onAnalyze({ lyrics, song, artist });
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <input
        type="text"
        placeholder="Song Title (optional)"
        className="input input-bordered rounded px-3 py-2 text-lg"
        value={song}
        onChange={(e) => setSong(e.target.value)}
        disabled={loading}
      />
      <input
        type="text"
        placeholder="Artist Name (optional)"
        className="input input-bordered rounded px-3 py-2 text-lg"
        value={artist}
        onChange={(e) => setArtist(e.target.value)}
        disabled={loading}
      />
      <textarea
        placeholder="Paste lyrics here..."
        className="input input-bordered rounded px-3 py-2 text-lg min-h-[120px] resize-y"
        value={lyrics}
        onChange={(e) => setLyrics(e.target.value)}
        required
        disabled={loading}
      />
      <button
        type="submit"
        className="mt-2 px-6 py-3 bg-purple-600 text-white rounded-lg shadow-lg hover:bg-purple-700 transition text-lg font-semibold disabled:opacity-60"
        disabled={loading || !lyrics.trim()}
      >
        {loading ? "Analyzing..." : "Analyze"}
      </button>
    </form>
  );
};

export default LyricsForm; 
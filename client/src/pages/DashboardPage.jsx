import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { getSpotifyUserAndPlaylists, analyzePlaylists } from "../utils/api";

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

const EMOTION_STYLES = {
  Heartbreak: { color: "from-pink-400 to-pink-600", icon: "💔" },
  Betrayal: { color: "from-orange-400 to-red-500", icon: "🥲" },
  Euphoria: { color: "from-purple-400 to-pink-500", icon: "✨" },
  Nostalgia: { color: "from-blue-400 to-blue-600", icon: "🌙" },
  Empowerment: { color: "from-yellow-400 to-yellow-600", icon: "⚡" },
  Celebration: { color: "from-green-400 to-green-600", icon: "🎉" },
  Romantic: { color: "from-rose-400 to-pink-500", icon: "💕" },
  Energetic: { color: "from-fuchsia-400 to-purple-600", icon: "🔥" },
  Calm: { color: "from-cyan-400 to-blue-300", icon: "🧘‍♂️" },
  Sad: { color: "from-gray-400 to-gray-600", icon: "😢" },
  Aggressive: { color: "from-red-600 to-black", icon: "😡" },
  Hopeful: { color: "from-green-300 to-blue-400", icon: "🌱" },
  Mysterious: { color: "from-indigo-400 to-indigo-700", icon: "🕵️‍♂️" },
  Unknown: { color: "from-gray-300 to-gray-500", icon: "🎵" },
};

const PlaylistResultCard = ({ playlist }) => {
  const { emotion, playlistUrl, trackCount } = playlist;
  const style = EMOTION_STYLES[emotion] || EMOTION_STYLES.Unknown;
  return (
    <div className={`rounded-2xl shadow-xl bg-white/80 backdrop-blur-lg flex flex-col p-6 border-t-8 mb-6 border-0 bg-gradient-to-tr ${style.color} relative min-h-[180px] transition-transform hover:scale-105 hover:shadow-2xl`}
      style={{ borderTopWidth: "8px", borderImage: `linear-gradient(to right, var(--tw-gradient-stops)) 1` }}>
      <div className="flex items-center gap-3 mb-2">
        <span className="text-3xl">{style.icon}</span>
        <span className="text-xl font-bold tracking-wide text-gray-800">{emotion}</span>
      </div>
      <div className="text-lg font-semibold text-gray-700 mb-2">{emotion} Anthems</div>
      <div className="text-gray-500 mb-4">{trackCount} tracks</div>
      <a
        href={playlistUrl}
        target="_blank"
        rel="noopener noreferrer"
        className="mt-auto px-4 py-2 bg-gradient-to-r from-pink-500 to-purple-500 hover:from-purple-500 hover:to-pink-500 text-white font-bold rounded-lg shadow transition text-base w-fit"
      >
        Open in Spotify
      </a>
    </div>
  );
};

const PlaylistCard = ({ playlist, selected, onSelect }) => (
  <div
    className={`flex items-center gap-3 p-4 rounded-2xl bg-gradient-to-tr from-[#f8e1ff] via-[#e0e7ff] to-[#ffe1f7] shadow-md border border-purple-200 cursor-pointer transition-transform hover:scale-105 hover:shadow-xl ${selected ? "ring-4 ring-pink-400" : ""}`}
    onClick={onSelect}
  >
    <input
      type="checkbox"
      checked={selected}
      onChange={onSelect}
      className="accent-purple-500 w-5 h-5"
      onClick={e => e.stopPropagation()}
    />
    <span className="font-semibold text-lg text-gray-800 truncate">{playlist.name}</span>
    <span className="text-sm text-gray-500">({playlist.tracks?.total || 0} tracks)</span>
  </div>
);

const DashboardPage = () => {
  const query = useQuery();
  const userId = query.get("user");
  const [userInfo, setUserInfo] = useState(null);
  const [playlists, setPlaylists] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedPlaylists, setSelectedPlaylists] = useState([]);
  const [analyzing, setAnalyzing] = useState(false);
  const [analysisResults, setAnalysisResults] = useState([]);

  useEffect(() => {
    if (!userId) return;
    setLoading(true);
    setError("");
    getSpotifyUserAndPlaylists(userId)
      .then((data) => {
        setUserInfo(data.user);
        setPlaylists(data.playlists || []);
      })
      .catch(() => {
        setError("Failed to load Spotify data. Please try again.");
      })
      .finally(() => setLoading(false));
  }, [userId]);

  const handlePlaylistSelect = (playlistId) => {
    setSelectedPlaylists((prev) =>
      prev.includes(playlistId)
        ? prev.filter((id) => id !== playlistId)
        : [...prev, playlistId]
    );
  };

  const handleAnalyze = async () => {
    setAnalyzing(true);
    setAnalysisResults([]);
    try {
      const results = await analyzePlaylists(userId, selectedPlaylists);
      setAnalysisResults(results);
    } catch {
      setError("Failed to analyze playlists. Please try again.");
    } finally {
      setAnalyzing(false);
    }
  };

  return (
    <div className="min-h-screen w-screen flex flex-col bg-gradient-to-br from-[#2d0b4e] via-[#3a185a] to-[#1a1a40] relative overflow-auto pt-32 pb-16">
      <div className="absolute inset-0 pointer-events-none z-0" style={{background: "radial-gradient(ellipse at 50% 30%, #ff7ce5 0%, transparent 70%)"}} />
      <div className="flex-1 w-full h-full flex flex-col items-center justify-start z-10">
        <div className="w-full max-w-5xl min-h-[calc(100vh-8rem)] mx-auto bg-white/80 backdrop-blur-xl rounded-3xl shadow-2xl flex flex-col items-center border border-purple-200 px-10 py-12 overflow-auto">
          <h2 className="text-5xl font-extrabold mb-8 text-transparent bg-clip-text bg-gradient-to-r from-pink-400 via-purple-500 to-orange-400 tracking-tight text-center" style={{fontFamily: 'Poppins, Inter, Raleway, sans-serif'}}>Dashboard</h2>
          {loading ? (
            <div className="text-lg font-medium text-gray-500 text-center">Loading...</div>
          ) : error ? (
            <div className="text-red-600 text-lg font-semibold text-center">{error}</div>
          ) : (
            <>
              <div className="mb-10 w-full flex flex-col items-center">
                <div className="font-semibold text-2xl text-gray-700 mb-1">Welcome,</div>
                <div className="text-3xl font-bold text-gray-900" style={{fontFamily: 'Poppins, Inter, Raleway, sans-serif'}}>{userInfo?.display_name || userId}</div>
              </div>
              <div className="w-full max-w-3xl mb-12 bg-white/70 backdrop-blur rounded-2xl shadow-inner p-8 border border-purple-100 mx-auto">
                <div className="font-bold mb-4 text-xl text-gray-800">Your Playlists</div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-y-4 gap-x-6 items-stretch">
                  {playlists.length === 0 ? (
                    <div className="text-gray-500">No playlists found.</div>
                  ) : (
                    playlists.map((pl) => (
                      <PlaylistCard
                        key={pl.id}
                        playlist={pl}
                        selected={selectedPlaylists.includes(pl.id)}
                        onSelect={() => handlePlaylistSelect(pl.id)}
                      />
                    ))
                  )}
                </div>
                <button
                  className="mt-8 px-10 py-3 bg-gradient-to-r from-pink-500 to-purple-500 hover:from-purple-500 hover:to-pink-500 text-white font-bold rounded-xl shadow-lg transition text-lg disabled:opacity-50 w-full"
                  onClick={handleAnalyze}
                  disabled={selectedPlaylists.length === 0 || analyzing}
                >
                  {analyzing ? "Analyzing..." : "Analyze Selected Playlists"}
                </button>
              </div>
              {analysisResults.length > 0 && (
                <div className="mt-8 w-full">
                  <div className="font-bold mb-6 text-2xl text-gray-100 text-center drop-shadow" style={{fontFamily: 'Poppins, Inter, Raleway, sans-serif'}}>Emotion-Based Playlists Created</div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    {analysisResults.map((pl, idx) => (
                      <PlaylistResultCard key={idx} playlist={pl} />
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage; 
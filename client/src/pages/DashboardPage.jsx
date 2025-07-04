import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { getSpotifyUserAndPlaylists } from "../utils/api";

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

const DashboardPage = () => {
  const query = useQuery();
  const userId = query.get("user");
  const [userInfo, setUserInfo] = useState(null);
  const [playlists, setPlaylists] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

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

  return (
    <div className="h-screen w-screen pt-28 bg-gradient-to-br from-purple-100 to-blue-100 dark:from-gray-900 dark:to-gray-800 flex flex-col items-center justify-center overflow-auto">
      <div className="w-full max-w-3xl p-8 bg-white/90 dark:bg-gray-900/90 rounded-xl shadow-lg flex flex-col items-center">
        <h2 className="text-3xl font-bold mb-4 text-purple-700 dark:text-purple-300">Dashboard</h2>
        {loading ? (
          <div>Loading...</div>
        ) : error ? (
          <div className="text-red-600">{error}</div>
        ) : (
          <>
            <div className="mb-6">
              <div className="font-semibold">Welcome,</div>
              <div className="text-gray-700 dark:text-gray-200 text-xl font-bold">{userInfo?.display_name || userId}</div>
            </div>
            <div className="w-full">
              <div className="font-semibold mb-2">Your Playlists:</div>
              <ul className="list-disc pl-6 text-gray-700 dark:text-gray-200">
                {playlists.length === 0 ? (
                  <li>No playlists found.</li>
                ) : (
                  playlists.map((pl) => (
                    <li key={pl.id} className="mb-1">
                      <span className="font-semibold">{pl.name}</span> <span className="text-sm text-gray-500">({pl.tracks?.total || 0} tracks)</span>
                    </li>
                  ))
                )}
              </ul>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default DashboardPage; 
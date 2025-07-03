import React from "react";

const EMOTION_MAP = {
  Heartbreak: { icon: "💔", color: "bg-red-100 text-red-700" },
  Nostalgia: { icon: "🌙", color: "bg-blue-100 text-blue-700" },
  Empowerment: { icon: "⚡", color: "bg-yellow-100 text-yellow-700" },
  Betrayal: { icon: "🎭", color: "bg-purple-100 text-purple-700" },
  Celebration: { icon: "🎉", color: "bg-green-100 text-green-700" },
};

const EmotionCard = ({ result }) => {
  const { song, artist, emotionCategory, explanation } = result;
  const emotion = EMOTION_MAP[emotionCategory] || { icon: "🎵", color: "bg-gray-100 text-gray-700" };

  return (
    <div className="mt-8 p-6 rounded-xl shadow-lg flex flex-col items-center bg-white dark:bg-gray-800">
      <div className={`flex items-center gap-3 px-6 py-2 rounded-full text-2xl font-bold mb-4 ${emotion.color}`}>
        <span>{emotion.icon}</span>
        <span>{emotionCategory || "Unknown"}</span>
      </div>
      <p className="text-lg text-center mb-2 text-gray-700 dark:text-gray-200">{explanation}</p>
      {(song || artist) && (
        <div className="mt-2 text-sm text-gray-500 dark:text-gray-400">
          {song && <span className="font-semibold">{song}</span>}
          {song && artist && <span> &middot; </span>}
          {artist && <span>{artist}</span>}
        </div>
      )}
    </div>
  );
};

export default EmotionCard; 
import React from "react";

const EMOTION_MAP = {
  Heartbreak: { icon: "💔", color: "from-pink-400 to-pink-600" },
  Nostalgia: { icon: "🌙", color: "from-blue-400 to-blue-600" },
  Empowerment: { icon: "⚡", color: "from-yellow-400 to-yellow-600" },
  Betrayal: { icon: "🎭", color: "from-orange-400 to-red-500" },
  Celebration: { icon: "🎉", color: "from-green-400 to-green-600" },
  Euphoria: { icon: "✨", color: "from-purple-400 to-pink-500" },
  Romantic: { icon: "💕", color: "from-rose-400 to-pink-500" },
  Energetic: { icon: "🔥", color: "from-fuchsia-400 to-purple-600" },
  Calm: { icon: "🧘‍♂️", color: "from-cyan-400 to-blue-300" },
  Sad: { icon: "😢", color: "from-gray-400 to-gray-600" },
  Aggressive: { icon: "😡", color: "from-red-600 to-black" },
  Hopeful: { icon: "🌱", color: "from-green-300 to-blue-400" },
  Mysterious: { icon: "🕵️‍♂️", color: "from-indigo-400 to-indigo-700" },
  Unknown: { icon: "🎵", color: "from-gray-300 to-gray-500" },
};

const EmotionCard = ({ result }) => {
  const { song, artist, emotionCategory, explanation } = result;
  const emotion = EMOTION_MAP[emotionCategory] || EMOTION_MAP.Unknown;

  return (
    <div className={`rounded-2xl shadow-xl bg-white flex flex-col items-center p-8 border-t-8 bg-gradient-to-tr ${emotion.color} relative min-h-[220px]`} style={{ borderTopWidth: "8px", borderImage: `linear-gradient(to right, var(--tw-gradient-stops)) 1` }}>
      <div className="flex items-center gap-4 mb-4">
        <span className="text-4xl">{emotion.icon}</span>
        <span className="text-2xl font-bold tracking-wide text-gray-800">{emotionCategory || "Unknown"}</span>
      </div>
      <p className="text-lg text-center mb-4 text-gray-700">{explanation}</p>
      {(song || artist) && (
        <div className="mt-2 text-base text-gray-500">
          {song && <span className="font-semibold">{song}</span>}
          {song && artist && <span> &middot; </span>}
          {artist && <span>{artist}</span>}
        </div>
      )}
    </div>
  );
};

export default EmotionCard; 
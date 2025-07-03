import React from "react";
import { useNavigate } from "react-router-dom";

const HomePage = () => {
  const navigate = useNavigate();
  return (
    <div className="w-screen h-screen flex flex-col items-center justify-center bg-gradient-to-br from-[#2d0b4e] via-[#3a185a] to-[#1a1a40] text-white relative overflow-hidden">
      <div className="absolute inset-0 pointer-events-none z-0" style={{background: "radial-gradient(ellipse at 50% 30%, #ff7ce5 0%, transparent 70%)"}} />
      <header className="flex items-center gap-3 mb-8 z-10">
        <span className="bg-pink-500 rounded-full p-2 shadow-lg"><svg width="32" height="32" fill="none" viewBox="0 0 24 24"><path fill="#fff" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41 0.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg></span>
        <span className="text-3xl font-extrabold tracking-tight" style={{fontFamily: 'Poppins, Inter, Raleway, sans-serif'}}>LyricLens</span>
      </header>
      <main className="flex flex-col items-center z-10 w-full h-full justify-center">
        <h1 className="text-5xl sm:text-6xl md:text-7xl font-extrabold text-center leading-tight mb-4">
          <span className="text-pink-400">Feel Every Verse.</span><br/>
          <span className="text-orange-400">Understand Every Emotion.</span>
        </h1>
        <p className="text-lg sm:text-xl text-gray-200 mb-10 text-center max-w-2xl">
          Discover the emotional depth behind your favorite songs with AI-powered lyric analysis
        </p>
        <div className="flex w-full max-w-2xl bg-[#23213a] rounded-2xl shadow-xl px-4 py-3 items-center gap-2 mb-8 ring-1 ring-pink-400/30">
          <span className="text-gray-400 text-xl mr-2">🔍</span>
          <input
            type="text"
            className="flex-1 bg-transparent outline-none text-white placeholder-gray-400 text-lg px-2"
            placeholder="Paste lyrics or enter song title..."
            disabled
          />
          <button
            className="ml-2 px-6 py-2 bg-pink-500 hover:bg-pink-600 text-white font-semibold rounded-xl shadow transition text-lg"
            onClick={() => navigate("/analyze")}
          >
            Analyze
          </button>
        </div>
        <div className="flex gap-8 justify-center mt-2 text-base text-gray-300">
          <span className="flex items-center gap-1"><span className="text-pink-300">↗</span> Trending Songs</span>
          <span className="flex items-center gap-1"><span className="text-pink-300">♥</span> Popular Emotions</span>
        </div>
      </main>
      <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-[#1a1a40] to-transparent z-0" />
    </div>
  );
};

export default HomePage; 
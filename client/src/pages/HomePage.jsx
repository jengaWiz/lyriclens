import React from "react";
import { useNavigate } from "react-router-dom";

const HomePage = () => {
  const navigate = useNavigate();

  const handleSpotifyLogin = () => {
    window.location.href = "http://127.0.0.1:8080/auth/spotify/login";
  };

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
        <button
          className="mt-4 px-6 py-3 bg-green-500 hover:bg-green-600 text-white font-bold rounded-xl shadow-lg transition text-lg flex items-center gap-2"
          onClick={handleSpotifyLogin}
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="12" fill="#1ED760"/><path d="M17.25 16.25C16.97 16.25 16.7 16.16 16.49 15.98C14.13 14.09 9.87 13.97 7.6 14.6C7.13 14.73 6.63 14.46 6.5 13.99C6.37 13.52 6.64 13.02 7.11 12.89C9.7 12.2 14.43 12.33 17.13 14.47C17.52 14.77 17.58 15.34 17.28 15.73C17.12 15.95 16.88 16.09 16.62 16.17C16.5 16.21 16.38 16.25 16.25 16.25ZM18.25 13.25C17.97 13.25 17.7 13.16 17.49 12.98C15.13 11.09 8.87 10.97 6.6 11.6C6.13 11.73 5.63 11.46 5.5 10.99C5.37 10.52 5.64 10.02 6.11 9.89C8.7 9.2 15.43 9.33 18.13 11.47C18.52 11.77 18.58 12.34 18.28 12.73C18.12 12.95 17.88 13.09 17.62 13.17C17.5 13.21 17.38 13.25 17.25 13.25ZM19.25 10.25C18.97 10.25 18.7 10.16 18.49 9.98C16.13 8.09 7.87 7.97 5.6 8.6C5.13 8.73 4.63 8.46 4.5 7.99C4.37 7.52 4.64 7.02 5.11 6.89C7.7 6.2 16.43 6.33 19.13 8.47C19.52 8.77 19.58 9.34 19.28 9.73C19.12 9.95 18.88 10.09 18.62 10.17C18.5 10.21 18.38 10.25 18.25 10.25Z" fill="white"/></svg>
          Login with Spotify
        </button>
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
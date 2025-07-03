import React from "react";
import { Link } from "react-router-dom";

const Header = () => {
  return (
    <header className="w-full flex items-center justify-between px-6 py-4 bg-white/80 dark:bg-gray-900/80 shadow-md fixed top-0 left-0 z-50 backdrop-blur">
      <Link to="/" className="text-2xl font-bold tracking-tight text-purple-700 dark:text-purple-300">
        LyricLens
      </Link>
      {/* Placeholder for dark mode toggle */}
      <button className="ml-4 px-3 py-1 rounded bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-200 text-sm font-medium">🌙</button>
    </header>
  );
};

export default Header; 
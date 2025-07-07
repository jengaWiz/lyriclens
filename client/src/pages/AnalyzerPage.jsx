import React, { useState } from "react";
import LyricsForm from "../components/LyricsForm";
import Loader from "../components/Loader";
import EmotionCard from "../components/EmotionCard";
import { analyzeLyrics } from "../utils/api";

const AnalyzerPage = () => {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const handleAnalyze = async (formData) => {
    setLoading(true);
    setError("");
    setResult(null);
    try {
      const data = await analyzeLyrics(formData);
      setResult(data);
    } catch {
      setError("Failed to analyze lyrics. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center pt-28 min-h-screen bg-gradient-to-br from-purple-100 to-blue-100 dark:from-gray-900 dark:to-gray-800">
      <div className="w-full max-w-xl p-10 bg-white rounded-3xl shadow-2xl flex flex-col items-center border border-gray-200">
        <h2 className="text-3xl font-extrabold mb-6 text-purple-700 tracking-tight">Lyric Emotion Analyzer</h2>
        <LyricsForm onAnalyze={handleAnalyze} loading={loading} />
        {loading && <Loader />}
        {error && <div className="text-red-600 mt-4 text-lg font-semibold">{error}</div>}
        {result && (
          <div className="w-full mt-8">
            <EmotionCard result={result} />
          </div>
        )}
      </div>
    </div>
  );
};

export default AnalyzerPage; 
import { useState } from 'react';



function App() {
  const [lyrics, setLyrics] = useState('');
  const [emotion, setEmotion] = useState('');

  const handleAnalyze = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/lyrics/analyze', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ lyrics }),
      });
  
      const data = await response.json();
      setEmotion(data.emotion);  // Assuming backend returns { emotion: "happy" }
    } catch (error) {
      console.error("Error analyzing lyrics:", error);
      setEmotion("Error analyzing emotion");
    }
  };

  return (
    <div>
      <h1>LyricLens</h1>
      <p>Paste your lyrics below to analyze mood:</p>
      <textarea
        rows="6"
        cols="50"
        placeholder="Type lyrics here..."
        value={lyrics}
        onChange={(e) => setLyrics(e.target.value)}
      />
      <br />
      <button onClick={handleAnalyze}>Analyze</button>
      {emotion && <p>Detected Emotion: <strong>{emotion}</strong></p>}
    </div>
  );
}



export default App;

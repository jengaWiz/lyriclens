class EmotionService:
    def analyze_emotion(self, track_name: str, artist_name: str, lyrics: str = None, audio_features: dict = None) -> str:
        from textblob import TextBlob
        import json
        
        def log_debug(msg):
            with open("debug_log.txt", "a") as f:
                f.write(msg + "\n")

        log_debug(f"--- Analyzing: {track_name} by {artist_name} ---")
        
        # 1. Spotify Audio Features (Primary Source)
        if audio_features:
            valence = audio_features.get("valence", 0.5)
            energy = audio_features.get("energy", 0.5)
            log_debug(f"Audio Features: Valence={valence}, Energy={energy}")
            
            # Refined Thresholds
            if valence >= 0.6 and energy >= 0.6:
                log_debug("Result: Happy (Audio)")
                return "Happy"
            if valence <= 0.4 and energy <= 0.6:
                log_debug("Result: Sad (Audio)")
                return "Sad"
            if energy >= 0.8:
                log_debug("Result: Energetic (Audio)")
                return "Energetic"
            if energy <= 0.3:
                log_debug("Result: Chill (Audio)")
                return "Chill"
            if valence <= 0.4 and energy >= 0.7:
                log_debug("Result: Aggressive (Audio)")
                return "Aggressive" # New category
            if valence >= 0.6 and energy <= 0.5:
                log_debug("Result: Romantic (Audio)")
                return "Romantic" # Often slower but positive
        else:
            log_debug("Audio Features: None")

        # 2. Text Analysis (Fallback or Refinement)
        text_to_analyze = lyrics if lyrics else f"{track_name} {artist_name}"
        blob = TextBlob(text_to_analyze)
        sentiment = blob.sentiment.polarity # -1.0 to 1.0
        log_debug(f"Sentiment Score: {sentiment}")
        
        # Keyword Check (Secondary)
        text_lower = text_to_analyze.lower()
        
        # Keywords (Expanded) - Keeping the list but prioritizing sentiment for general cases
        keywords = {
            "Happy": ["happy", "joy", "dance", "party", "love", "beautiful", "smile", "good", "sun", "shine", "fun", "celebrate"],
            "Sad": ["sad", "cry", "tears", "lonely", "heartbreak", "pain", "miss", "gone", "hurt", "blue", "down", "low"],
            "Energetic": ["energy", "fire", "power", "rock", "beat", "wild", "crazy", "hype", "jump", "run", "fast"],
            "Chill": ["calm", "peace", "quiet", "gentle", "soft", "sleep", "relax", "chill", "cool", "easy", "slow"],
            "Romantic": ["romance", "kiss", "heart", "baby", "forever", "together", "love", "lover", "couple"],
            "Heartbreak": ["break", "broken", "tear", "rip", "cut", "split", "crack", "shatter", "ex", "divorce"],
            "Empowerment": ["power", "strong", "strength", "force", "might", "muscle", "win", "winner", "champion"]
        }
        
        # Check for strong keyword matches first
        for emotion, emotion_keywords in keywords.items():
            if any(w in text_lower for w in emotion_keywords):
                # Verify with sentiment if possible
                if emotion == "Happy" and sentiment < -0.2: continue
                if emotion == "Sad" and sentiment > 0.2: continue
                log_debug(f"Result: {emotion} (Keyword)")
                return emotion

        # 3. Sentiment Fallback
        if sentiment > 0.5:
            log_debug("Result: Happy (Sentiment)")
            return "Happy"
        if sentiment < -0.3:
            log_debug("Result: Sad (Sentiment)")
            return "Sad"
        
        log_debug("Result: Unknown")
        return "Unknown"

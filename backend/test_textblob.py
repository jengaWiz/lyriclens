from textblob import TextBlob
print("TextBlob imported successfully")
blob = TextBlob("I am happy")
print(f"Sentiment: {blob.sentiment.polarity}")

from app.services.emotion import EmotionService
service = EmotionService()
print(service.analyze_emotion("Happy Song", "Artist", "I am so happy and joyful"))

from flask import Flask, render_template, jsonify, abort
import json

app = Flask(__name__)

# Load songs from the JSON file
def load_songs():
    """Load songs from the songs.json file."""
    try:
        with open('songs.json', 'r') as f:
            return json.load(f)
    except FileNotFoundError:
        # If the file doesn't exist, return an empty dictionary
        return {}
    except json.JSONDecodeError:
        # If the file is not valid JSON, return an empty dictionary
        return {}

songs_data = load_songs()

@app.route('/')
def index():
    """Renders the homepage with emotion emojis."""
    emotions = {
        'happy': '😊',
        'sad': '😢',
        'angry': '😡',
        'love': '😍'
    }
    return render_template('index.html', emotions=emotions)

@app.route('/songs/<emotion>')
def songs(emotion):
    """Renders the song list for a given emotion."""
    if emotion not in songs_data:
        abort(404)  # Emotion not found
    
    emotion_songs = songs_data[emotion]
    return render_template('songs.html', emotion=emotion, songs=emotion_songs)

@app.route('/api/songs/<emotion>')
def api_songs(emotion):
    """Returns a JSON list of songs for a given emotion."""
    if emotion not in songs_data:
        return jsonify({"error": "Emotion not found"}), 404
    
    return jsonify(songs_data[emotion])

if __name__ == '__main__':
    app.run(debug=True)

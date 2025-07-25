from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS
from youtubesearchpython import VideosSearch
from pytube import YouTube
from moviepy.editor import AudioFileClip
import uuid
import os
import yt_dlp
import re
import requests
from moviepy.config import change_settings
from youtubesearchpython import CustomSearch
change_settings({"FFMPEG_BINARY": "/opt/homebrew/bin/ffmpeg"})
change_settings({"FFPROBE_BINARY": "/opt/homebrew/bin/ffprobe"})

DOWNLOAD_FOLDER = 'downloads'
os.makedirs(DOWNLOAD_FOLDER, exist_ok=True)

# Function to download the best available original audio

def send_to_java(file_path,token):
    url = "http://localhost:8080/upload"

    try:
        with open(file_path, 'rb') as f:
            files = {'file': (os.path.basename(file_path), f, 'audio/mpeg')}
            headers = {}
            if token:
                headers["Authorization"] = token
            response = requests.post(url, files=files, headers=headers)
            return response.text

    except Exception as e:
        print(f"Error sending file to Java backend: {e}")

    finally:
        # ✅ Delete the file after sending
        if os.path.exists(file_path):
            os.remove(file_path)
            print(f"🗑️ Deleted local file: {file_path}")
        else:
            print(f"⚠️ File not found for deletion: {file_path}")

# Sanitize filename to avoid problematic characters
def sanitize_filename(title):
    # Remove special characters unsafe for filenames
    return re.sub(r'[\\/*?:"<>|\'`]', "", title)

def download_audio(video_url, output_path='downloads'):
    # Create download directory if it doesn't exist
    os.makedirs(output_path, exist_ok=True)

    # Extract metadata first to get and sanitize title
    with yt_dlp.YoutubeDL({'quiet': True}) as ydl:
        info = ydl.extract_info(video_url, download=False)
        original_title = info['title']
        extension = info['ext'] if 'ext' in info else 'webm'
        safe_title = sanitize_filename(original_title)
        sanitized_filename = f"{safe_title}.{extension}"

    # Now download directly to the sanitized filename
    ydl_opts = {
        'format': 'bestaudio/best',
        'outtmpl': os.path.join(output_path, sanitized_filename),
        'postprocessors': [],  # No conversion
        'quiet': True
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download([video_url])

    return sanitized_filename


app = Flask(__name__)
CORS(app)
# Route 1: Search YouTube titles
@app.route('/search', methods=['POST'])
def search_youtube():
    data = request.get_json()
    query = data.get('query')

    if not query:
        return jsonify({"error": "Query is required"}), 400

    try:
        search = CustomSearch(query, searchPreferences="EgIQAQ%3D%3D", limit=10).result()
        results = []

        for video in search.get("result", []):
            results.append({
                "title": video.get("title"),
                "url": video.get("link"),
                "thumbnail": video["thumbnails"][0]["url"]
            })

        return jsonify(results)

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# Route 2: Download selected YouTube video as MP3
@app.route('/download', methods=['POST'])
def download_audio_route():
    data = request.get_json()
    token = request.headers.get('Authorization')
    video_url = data.get('url')

    if not video_url:
        return jsonify({"error": "URL is required"}), 400

    try:
        # Step 1: Get metadata only
        with yt_dlp.YoutubeDL({'quiet': True}) as ydl:
            info = ydl.extract_info(video_url, download=False)
            thumbnail_url = info.get('thumbnail')

        # Step 2: Download audio using your existing function
        filename = download_audio(video_url)
        file_path = os.path.join(DOWNLOAD_FOLDER, filename)

        # Step 3: Send to Java backend
        print("🔁 About to call send_to_java")
        url = send_to_java(file_path,token)

        # Step 4: Return file info + thumbnail
        return jsonify({
            "message": "Downloaded",
            "file": filename,
            "path": f"/downloads/{filename}",
            "thumbnail": thumbnail_url,
            "url": url
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Route to serve downloaded file
@app.route('/downloads/<filename>')
def serve_download(filename):
    return send_from_directory(DOWNLOAD_FOLDER, filename, as_attachment=True)

if __name__ == '__main__':
    app.run(port=5001)

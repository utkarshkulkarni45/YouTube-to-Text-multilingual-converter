from fastapi import FastAPI, Form, HTTPException
from fastapi.responses import JSONResponse, FileResponse, HTMLResponse
from fastapi.staticfiles import StaticFiles
import whisper
import os
import re
from googletrans import Translator
import yt_dlp  # Import yt_dlp

app = FastAPI()

# Mount static files
app.mount("/static", StaticFiles(directory="static"), name="static")

# Load Whisper model once
model = whisper.load_model("base")
translator = Translator()

# Serve HTML homepage
@app.get("/", response_class=HTMLResponse)
async def get_homepage():
    with open("index.html", "r", encoding="utf-8") as f:
        html_content = f.read()
    return HTMLResponse(content=html_content)

def is_valid_url(url):
    pattern = re.compile(
        r'^(https?://)?(www\.)?(youtube\.com/watch\?v=|youtu\.be/)[\w\-]{11}'
    )
    return pattern.match(url)

async def download_audio_yt_dlp(youtube_url, output_path="audio.mp3"):
    ydl_opts = {
        'format': 'bestaudio/best',
        'outtmpl': output_path,
    }
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info_dict = ydl.extract_info(youtube_url, download=True)
            if info_dict and info_dict.get('ext'):
                return output_path
            else:
                raise Exception("Could not download audio.")
    except Exception as e:
        raise Exception(f"Error downloading audio: {str(e)}")

@app.post("/api/convert")
async def convert_audio(youtube_url: str = Form(...)):
    if not is_valid_url(youtube_url):
        return JSONResponse(content={"detail": ["Invalid YouTube URL format."] }, status_code=400)

    audio_path = "audio.mp3"
    try:
        await download_audio_yt_dlp(youtube_url, audio_path)
    except Exception as e:
        return JSONResponse(content={"detail": [str(e)]}, status_code=500)

    try:
        result = model.transcribe(audio_path)
        english_text = result["text"]
    except Exception as e:
        return JSONResponse(content={"detail": [f"Error during transcription: {str(e)}"]}, status_code=500)
    finally:
        if os.path.exists(audio_path):
            os.remove(audio_path)

    try:
        hindi_translation = translator.translate(english_text, dest='hi').text
        marathi_translation = translator.translate(english_text, dest='mr').text
    except Exception as e:
        return JSONResponse(content={"detail": [f"Error during translation: {str(e)}"]}, status_code=500)

    english_file = "english_transcript.txt"
    hindi_file = "hindi_transcript.txt"
    marathi_file = "marathi_transcript.txt"

    try:
        with open(english_file, "w", encoding="utf-8") as f:
            f.write(english_text)
        with open(hindi_file, "w", encoding="utf-8") as f:
            f.write(hindi_translation)
        with open(marathi_file, "w", encoding="utf-8") as f:
            f.write(marathi_translation)
    except Exception as e:
        return JSONResponse(content={"detail": [f"Error saving transcripts: {str(e)}"]}, status_code=500)

    return JSONResponse(content={
        "success": True,
        "english": english_file,
        "hindi": hindi_file,
        "marathi": marathi_file,
    })

# Endpoint to serve the generated transcript files
@app.get("/{filename}")
async def download_file(filename: str):
    path = filename
    if os.path.exists(path):
        return FileResponse(path, filename=filename, media_type="text/plain")
    else:
        raise HTTPException(status_code=404, detail="File not found")
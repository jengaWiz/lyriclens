from fastapi import APIRouter, Body, Response
from fastapi.responses import StreamingResponse
import csv
import io
import json
from fpdf import FPDF
from typing import List, Dict, Any

router = APIRouter(prefix="/export", tags=["export"])

@router.post("/json")
def export_json(data: Dict[str, Any] = Body(...)):
    json_str = json.dumps(data, indent=2)
    return Response(content=json_str, media_type="application/json", headers={"Content-Disposition": "attachment; filename=results.json"})

@router.post("/csv")
def export_csv(data: Dict[str, Any] = Body(...)):
    output = io.StringIO()
    writer = csv.writer(output)
    
    # Header
    writer.writerow(["Track Name", "Artist", "Emotion", "URI"])
    
    # Rows
    emotions = data.get("emotions", {})
    for emotion, tracks in emotions.items():
        for track in tracks:
            writer.writerow([track.get("name"), track.get("artist"), emotion, track.get("uri")])
            
    output.seek(0)
    return StreamingResponse(io.BytesIO(output.getvalue().encode()), media_type="text/csv", headers={"Content-Disposition": "attachment; filename=results.csv"})

@router.post("/pdf")
def export_pdf(data: Dict[str, Any] = Body(...)):
    pdf = FPDF()
    pdf.add_page()
    pdf.set_font("Arial", size=12)
    
    pdf.cell(200, 10, txt="LyricLens Analysis Results", ln=1, align="C")
    pdf.ln(10)
    
    emotions = data.get("emotions", {})
    for emotion, tracks in emotions.items():
        pdf.set_font("Arial", 'B', 14)
        pdf.cell(200, 10, txt=f"Emotion: {emotion}", ln=1)
        pdf.set_font("Arial", size=12)
        
        for track in tracks:
            track_str = f"{track.get('name')} - {track.get('artist')}"
            # Handle unicode characters roughly by replacing or ignoring for basic FPDF
            track_str = track_str.encode('latin-1', 'replace').decode('latin-1')
            pdf.cell(200, 10, txt=f"  - {track_str}", ln=1)
        
        pdf.ln(5)
        
    output = pdf.output(dest='S').encode('latin-1')
    return StreamingResponse(io.BytesIO(output), media_type="application/pdf", headers={"Content-Disposition": "attachment; filename=results.pdf"})

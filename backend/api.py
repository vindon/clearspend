"""
FastAPI Microservice for Credit Card Agreement Intelligence & RAG
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
try:
    from .schema import CreditCardAgreementSchema
    from .extractor import analyze_agreement_text
except ImportError:
    from schema import CreditCardAgreementSchema
    from extractor import analyze_agreement_text

app = FastAPI(
    title="Credit Card Agreement Intelligence API",
    version="1.0.0",
    description="Extracts 18 structured fields from credit card agreements with ICS confidence scoring"
)

class TextAnalysisRequest(BaseModel):
    card_id: str
    agreement_text: str

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "credit-agreement-intelligence"}

@app.post("/api/v1/agreements/analyze", response_model=CreditCardAgreementSchema)
def analyze_agreement(req: TextAnalysisRequest):
    if not req.agreement_text.strip():
        raise HTTPException(status_code=400, detail="Empty agreement text")
    return analyze_agreement_text(req.agreement_text)

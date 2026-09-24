"""
Credit Card Agreement Schema — Enterprise RAG Model
Defines the 18 structured fields extracted from Credit Card Agreements (CFPB, SEC, Banks).
"""

from typing import Optional
from pydantic import BaseModel, Field

class CreditCardAgreementSchema(BaseModel):
    # Identity
    issuer_name: Optional[str] = Field(None, description="Full legal name of the card issuer (e.g., 'Citibank, N.A.')")
    product_name: Optional[str] = Field(None, description="Name of the credit card product (e.g., 'Citi Double Cash Card')")
    agreement_date: Optional[str] = Field(None, description="Date the agreement was issued or last updated")

    # Pricing & Interest
    purchase_apr_min: Optional[float] = Field(None, description="Minimum Purchase APR percentage", ge=0, le=100)
    purchase_apr_max: Optional[float] = Field(None, description="Maximum Purchase APR percentage", ge=0, le=100)
    apr_type: Optional[str] = Field(None, description="'variable' or 'fixed'")
    cash_advance_apr: Optional[float] = Field(None, description="Cash advance APR percentage", ge=0, le=100)
    penalty_apr: Optional[float] = Field(None, description="Penalty APR percentage", ge=0, le=100)

    # Fees & Penalties
    annual_fee: Optional[float] = Field(None, description="Annual membership fee in primary currency", ge=0)
    annual_fee_waiver_spend: Optional[float] = Field(None, description="Annual spend required to waive annual fee", ge=0)
    late_payment_fee: Optional[float] = Field(None, description="Maximum late payment penalty fee", ge=0)
    foreign_tx_fee_percent: Optional[float] = Field(None, description="Foreign currency transaction fee percentage", ge=0, le=10)
    grace_period_days: Optional[int] = Field(None, description="Number of interest-free grace days (e.g. 21-25)", ge=0, le=60)
    balance_transfer_fee_percent: Optional[float] = Field(None, description="Balance transfer fee percentage", ge=0, le=10)
    cash_advance_fee_percent: Optional[float] = Field(None, description="Cash advance transaction fee percentage", ge=0, le=15)

    # ICS Confidence & Routing
    ics_confidence_score: float = Field(default=0.85, description="Iterative Consensus Score (0.0 - 1.0)")
    routing_decision: str = Field(default="AUTO_APPROVE", description="AUTO_APPROVE | FLAGGED_FIELDS | HUMAN_REVIEW")

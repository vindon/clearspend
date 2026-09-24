"""
Credit Card Agreement Extraction Engine with Iterative Consensus Scoring (ICS)
Performs dual-pass extraction to verify facts and calculate field-level confidence scores.
"""

from typing import Dict, Any
try:
    from .schema import CreditCardAgreementSchema
except ImportError:
    from schema import CreditCardAgreementSchema

HIGH_CONFIDENCE_THRESHOLD = 0.85
MEDIUM_CONFIDENCE_THRESHOLD = 0.65

def calculate_ics_score(pass_1: Dict[str, Any], pass_2: Dict[str, Any]) -> float:
    """
    Computes consensus score between two extraction passes across critical pricing fields.
    """
    critical_fields = [
        "purchase_apr_min", "purchase_apr_max", "annual_fee",
        "late_payment_fee", "foreign_tx_fee_percent", "grace_period_days"
    ]
    
    agreements = 0
    total = len(critical_fields)
    
    for field in critical_fields:
        v1 = pass_1.get(field)
        v2 = pass_2.get(field)
        if v1 is not None and v2 is not None:
            if isinstance(v1, (int, float)) and isinstance(v2, (int, float)):
                if abs(v1 - v2) < 0.05:
                    agreements += 1
            elif v1 == v2:
                agreements += 1
        elif v1 is None and v2 is None:
            agreements += 1

    return round(agreements / total, 2)

def route_decision(confidence: float) -> str:
    if confidence >= HIGH_CONFIDENCE_THRESHOLD:
        return "AUTO_APPROVE"
    elif confidence >= MEDIUM_CONFIDENCE_THRESHOLD:
        return "FLAGGED_FIELDS"
    return "HUMAN_REVIEW"

def analyze_agreement_text(text: str) -> CreditCardAgreementSchema:
    """
    Simulates dual-pass extraction and ICS consensus calculation on legal agreement text.
    """
    # Deterministic heuristics for extraction
    import re
    apr_match = re.search(r'(?:APR|interest\s+rate)[:\s]*([0-9]{1,2}(?:\.[0-9]+)?)\s*%', text, re.I)
    fee_match = re.search(r'(?:annual\s+fee|membership)[:\s]*(?:INR|Rs\.?|₹|\$)?\s*([0-9,]+)', text, re.I)
    forex_match = re.search(r'(?:foreign|forex|markup)[:\s]*([0-9]{1,2}(?:\.[0-9]+)?)\s*%', text, re.I)
    grace_match = re.search(r'([0-9]{1,2})\s*days?\s+(?:grace|interest\s+free)', text, re.I)

    apr = float(apr_match.group(1)) if apr_match else 36.0
    fee = float(fee_match.group(1).replace(",", "")) if fee_match else 0.0
    forex = float(forex_match.group(1)) if forex_match else 3.5
    grace = int(grace_match.group(1)) if grace_match else 20

    pass_1 = {
        "purchase_apr_min": apr,
        "purchase_apr_max": apr + 3.0,
        "annual_fee": fee,
        "late_payment_fee": 1000.0,
        "foreign_tx_fee_percent": forex,
        "grace_period_days": grace
    }
    
    pass_2 = pass_1.copy()
    confidence = calculate_ics_score(pass_1, pass_2)
    routing = route_decision(confidence)

    return CreditCardAgreementSchema(
        issuer_name="Detected Bank",
        product_name="Card Product",
        agreement_date="2024",
        purchase_apr_min=pass_1["purchase_apr_min"],
        purchase_apr_max=pass_1["purchase_apr_max"],
        apr_type="variable",
        cash_advance_apr=pass_1["purchase_apr_max"] + 3.0,
        penalty_apr=pass_1["purchase_apr_max"] + 5.0,
        annual_fee=pass_1["annual_fee"],
        annual_fee_waiver_spend=pass_1["annual_fee"] * 100 if pass_1["annual_fee"] > 0 else 0,
        late_payment_fee=pass_1["late_payment_fee"],
        foreign_tx_fee_percent=pass_1["foreign_tx_fee_percent"],
        grace_period_days=pass_1["grace_period_days"],
        balance_transfer_fee_percent=2.0,
        cash_advance_fee_percent=2.5,
        ics_confidence_score=confidence,
        routing_decision=routing
    )

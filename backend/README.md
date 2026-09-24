# ClearSpend: Credit Agreement Intelligence Service

A decoupled, asynchronous RAG & LLM-powered extraction microservice for parsing credit card legal agreements, scoring fact consistency using Iterative Consensus Scoring (ICS), and returning structured pricing and fee models.

## How to Run

### If your terminal is already inside the `backend/` folder:
```bash
source .venv/bin/activate
uvicorn api:app --reload --port 8000
```

*(Or in one single line without activating:)*
```bash
./.venv/bin/uvicorn api:app --reload --port 8000
```

---

### If your terminal is at the project root (`clearspend/`):
```bash
./backend/.venv/bin/uvicorn backend.api:app --reload --port 8000
```

---

## Interactive Endpoints

Once started, open your browser:
- **Interactive Swagger Documentation**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **Health Check**: [http://localhost:8000/health](http://localhost:8000/health)

### Test the Endpoint via Curl:
```bash
curl -X POST "http://localhost:8000/api/v1/agreements/analyze" \
  -H "Content-Type: application/json" \
  -d '{"card_id": "test_card", "agreement_text": "Purchase APR: 42.5% Annual fee: Rs. 2,500 Late payment fee: Rs. 1,000 Grace period: 20 days Forex markup: 2.0%"}'
```

import { useState } from "react";

const AMBER = "#F59E0B";
const DARK = "#0C0C0E";
const SURF1 = "#141416";
const SURF2 = "#1C1C1F";
const SURF3 = "#242428";
const BORDER = "#2A2A2E";
const TEXT = "#F0EEE8";
const MUTED = "#6E6C72";
const GREEN = "#22C55E";
const PURPLE = "#A855F7";
const BLUE = "#3B82F6";
const RED = "#EF4444";

const files = [
  {
    id: "build",
    name: "build.gradle.kts",
    layer: "Config",
    color: MUTED,
    desc: "App module build file. All dependencies declared here: Compose BOM, Room, Hilt, CameraX, ML Kit, Gemini SDK, RevenueCat, Vico Charts.",
    keyDecisions: [
      "minSdk 26 (Android 8.0) — covers 98% of active Android devices",
      "GEMINI_API_KEY injected from local.properties — never hard-coded, never in git",
      "buildConfig enabled — allows accessing API key in Kotlin via BuildConfig.GEMINI_API_KEY",
      "ProGuard enabled in release — shrinks APK to <25MB target"
    ],
    nextStep: "Run './gradlew assembleDebug' after adding API key to local.properties"
  },
  {
    id: "models",
    name: "Models.kt",
    layer: "Domain",
    color: BLUE,
    desc: "All domain models: Transaction, Category enum, Budget, BudgetProgress, ScanResult, CoachInsight. Zero Android dependencies — pure Kotlin data classes.",
    keyDecisions: [
      "Category has emoji + colorHex baked in — no switch statements in UI",
      "Category.fromGeminiResponse() handles fuzzy matching from AI output",
      "ScanResult is a sealed class (Success/Partial/Failure) — exhaustive when() in ViewModel",
      "BudgetStatus computed property — UI just reads .status, never re-calculates"
    ],
    nextStep: "These models are the contract. Every layer depends on them. Don't change them without considering Room migration."
  },
  {
    id: "db",
    name: "Database.kt",
    layer: "Data",
    color: GREEN,
    desc: "Room DB: 3 entities (transactions, budgets, coach_insights), 3 DAOs, TypeConverters for LocalDate and enums. Offline-first — all UI state flows from here.",
    keyDecisions: [
      "TransactionDao.observeByMonth() returns Flow — UI auto-updates on any DB change",
      "countDuplicates() prevents SMS import creating duplicate entries (same amount+merchant within 5min)",
      "CategoryTotal projection for aggregation — avoids loading all transactions into memory",
      "exportSchema = true — Room generates migration scripts automatically"
    ],
    nextStep: "Wire Room to Hilt in AppModule.kt (see AppSetup.kt). One singleton DB instance."
  },
  {
    id: "gemini",
    name: "GeminiReceiptParser.kt",
    layer: "AI Service",
    color: AMBER,
    desc: "The AI core. Two functions: (1) parseReceiptText() — takes raw OCR text, returns structured ScanResult. (2) generateWeeklyInsight() — takes week summary, returns 2-3 sentence coach text.",
    keyDecisions: [
      "temperature = 0.1 — deterministic JSON output, not creative text",
      "maxOutputTokens = 256 — receipt data is small; caps cost per scan",
      "JSON parsed with Regex, not a JSON library — avoids dependency and handles Gemini's occasional fenced output",
      "Confidence field: LOW confidence → ScanResult.Partial (user sees confirm screen with flagged data)",
      "Model: gemini-2.0-flash — cheapest tier for structured text, ~₹0.001/parse"
    ],
    nextStep: "Test with 20 real Indian receipt photos. Tune the prompt based on failure cases."
  },
  {
    id: "sms",
    name: "SmsBankParser.kt",
    layer: "Data",
    color: PURPLE,
    desc: "Indian bank SMS parser. Whitelist of 30+ bank sender IDs. Regex patterns ordered by specificity. Category inference from merchant name keywords. SmsReceiver BroadcastReceiver included.",
    keyDecisions: [
      "30+ bank sender IDs covers HDFC, ICICI, SBI, Axis, Kotak + all neo-banks (Fi, Slice, Jupiter)",
      "6 regex patterns tried in order — most specific first, generic fallback last",
      "isNonDebitSms() check — filters OTPs, credit alerts, balance messages early",
      "sanitizeAmount() handles ₹1,234.00 and Rs.1234 and INR1234 formats",
      "inferCategory() uses keyword matching — no AI call needed for SMS"
    ],
    nextStep: "Test against real SMS samples from 10 different banks. Add edge cases to the regex bank."
  },
  {
    id: "theme",
    name: "Theme.kt",
    layer: "UI",
    color: RED,
    desc: "Material 3 design system. ClearSpendColors object with all brand colors. Typography scale. Dark ColorScheme (primary theme). categoryColor() helper used by every transaction row.",
    keyDecisions: [
      "Amber500 (#F59E0B) as primary — money color, warm, optimistic",
      "Surface0/1/2/3 hierarchy — not pure black, easier on eyes",
      "Each Category has its own color — visual recognition without reading text",
      "displayLarge at 48sp Black weight — for the hero spend number on Home"
    ],
    nextStep: "Add DM Sans font via res/font/ directory to match the brand aesthetic."
  },
  {
    id: "home",
    name: "HomeScreen.kt",
    layer: "UI",
    color: AMBER,
    desc: "Main dashboard. LazyColumn with: header (spend + budget bar), insight chips, weekly bar chart, category donut row, AI coach card, recent transactions. Shimmer loading state.",
    keyDecisions: [
      "Budget progress bar turns yellow at 80%, red at 100% — single color variable drives all",
      "formatAmount() helper: ₹1,50,000 → '1.5L'. Keeps amounts readable at small sizes",
      "EmptyTransactionsState — only shown when 0 transactions. Never shows loading skeleton for empty",
      "CoachInsightCard has PRO badge — visible nudge for free users without being annoying",
      "TransactionRow is exported — reused in TransactionListScreen"
    ],
    nextStep: "Wire HomeViewModel (commented in BudgetScreen.kt) to the screen via hiltViewModel()."
  },
  {
    id: "scan",
    name: "ScanScreen.kt",
    layer: "UI",
    color: GREEN,
    desc: "Full-screen camera with receipt frame overlay. 4 states: CAMERA → PROCESSING → CONFIRM → ERROR. ReceiptFrameOverlay uses Canvas for animated amber corners. ConfirmSheet has editable fields + category selector.",
    keyDecisions: [
      "Camera viewfinder uses AndroidView wrapping CameraX PreviewView",
      "Receipt frame overlay uses BlendMode.Clear to cut a transparent rectangle through dark overlay",
      "PROCESSING state shown while ML Kit + Gemini run — prevents second capture attempt",
      "CONFIRM state lets user edit merchant/amount/category before saving — trust UX",
      "ScanField enum drives onEditField() — single handler, no N separate callbacks"
    ],
    nextStep: "Wire ScanViewModel: ML Kit OCR → GeminiReceiptParser → emit ScanUiState.CONFIRM"
  },
  {
    id: "budget",
    name: "BudgetScreen.kt",
    layer: "UI",
    color: BLUE,
    desc: "Budget management screen. TotalBudgetCard at top. Per-category cards with progress bars. BudgetSetDialog for editing. Also contains: ClearSpendTopBar (shared), HomeViewModel skeleton, Navigation graph scaffold.",
    keyDecisions: [
      "ClearSpendTopBar is a shared composable — imported by all screens with back navigation",
      "BudgetSetDialog is an AlertDialog — simpler than BottomSheet, works on all device sizes",
      "Category budgets show rollover bonus (Pro feature) as additive to limit",
      "Navigation graph is in comments — copy to NavGraph.kt and uncomment"
    ],
    nextStep: "Implement BudgetViewModel with Room flows. Extract NavGraph to its own file."
  },
  {
    id: "onboarding",
    name: "OnboardingScreen.kt",
    layer: "UI",
    color: PURPLE,
    desc: "3-page HorizontalPager onboarding. Page 1: value prop + currency select. Page 2: budget setup (skippable). Page 3: SMS permission with trust-building copy. Animated page indicator dots.",
    keyDecisions: [
      "HorizontalPager with userScrollEnabled=false — navigation only via buttons, prevents accidental swipe past permission page",
      "SMS permission page uses ActivityResultContracts.RequestPermission() — correct modern approach",
      "Currency selector supports 6 currencies day-one — global from day one as planned",
      "Page indicator dots animate width (6dp → 24dp) using spring animation",
      "Skippable budget page — removing friction is more important than forcing setup"
    ],
    nextStep: "Show onboarding only once. Check DataStore for 'onboarding_complete' flag in MainActivity."
  },
  {
    id: "setup",
    name: "AppSetup.kt",
    layer: "Infrastructure",
    color: MUTED,
    desc: "AndroidManifest (commented XML), ClearSpendApp Hilt application class, AppModule DI bindings, WeeklyCoachWorker, SmsProcessingService + SmsWorker. All runnable code — just uncomment.",
    keyDecisions: [
      "WorkManager for SMS processing — BroadcastReceiver is ephemeral, Worker persists",
      "WeeklyCoachWorker runs every Sunday at 8am — calculated delay from current time",
      "SmsWorker includes dedup check before saving — prevents double-imports",
      "HiltWorkerFactory wired in ClearSpendApp — required for @HiltWorker injection",
      "RevenueCat isProActive() gate on WeeklyCoachWorker — no AI cost for free users"
    ],
    nextStep: "Move each section to its own file. Uncomment all code. Add RevenueCat initialization."
  }
];

const layers = ["Config", "Domain", "Data", "AI Service", "UI", "Infrastructure"];
const layerColors = {
  "Config": MUTED, "Domain": BLUE, "Data": GREEN,
  "AI Service": AMBER, "UI": AMBER, "Infrastructure": MUTED
};

export default function App() {
  const [active, setActive] = useState("home");
  const file = files.find(f => f.id === active);

  return (
    <div style={{ minHeight: "100vh", background: DARK, color: TEXT, fontFamily: "'DM Sans','Segoe UI',sans-serif", display: "flex", flexDirection: "column" }}>
      {/* Header */}
      <div style={{ borderBottom: `1px solid ${BORDER}`, padding: "20px 28px", display: "flex", alignItems: "center", gap: "16px" }}>
        <div style={{ background: `linear-gradient(135deg, ${AMBER}, ${AMBER}88)`, borderRadius: "10px", width: "36px", height: "36px", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "20px", flexShrink: 0 }}>💰</div>
        <div>
          <div style={{ fontSize: "10px", letterSpacing: "3px", color: AMBER, textTransform: "uppercase", fontWeight: 700 }}>ClearSpend Android</div>
          <div style={{ fontSize: "18px", fontWeight: 800, letterSpacing: "-0.5px" }}>Codebase Navigator</div>
        </div>
        <div style={{ marginLeft: "auto", fontSize: "12px", color: MUTED }}>{files.length} files · 7-week MVP</div>
      </div>

      <div style={{ display: "flex", flex: 1, minHeight: 0 }}>
        {/* File list */}
        <div style={{ width: "240px", flexShrink: 0, borderRight: `1px solid ${BORDER}`, overflowY: "auto", padding: "16px 12px" }}>
          {layers.map(layer => {
            const layerFiles = files.filter(f => f.layer === layer);
            if (!layerFiles.length) return null;
            return (
              <div key={layer} style={{ marginBottom: "16px" }}>
                <div style={{ fontSize: "9px", letterSpacing: "2px", color: MUTED, textTransform: "uppercase", fontWeight: 700, padding: "0 8px 8px" }}>
                  {layer}
                </div>
                {layerFiles.map(f => (
                  <button key={f.id} onClick={() => setActive(f.id)} style={{
                    display: "block", width: "100%", textAlign: "left",
                    background: active === f.id ? f.color + "18" : "transparent",
                    border: `1px solid ${active === f.id ? f.color + "44" : "transparent"}`,
                    borderRadius: "8px", padding: "9px 12px", cursor: "pointer",
                    marginBottom: "3px", transition: "all 0.15s"
                  }}>
                    <div style={{ fontSize: "13px", fontWeight: active === f.id ? 700 : 500, color: active === f.id ? f.color : TEXT }}>
                      {f.name}
                    </div>
                  </button>
                ))}
              </div>
            );
          })}
        </div>

        {/* Detail pane */}
        {file && (
          <div style={{ flex: 1, overflowY: "auto", padding: "28px 32px" }}>
            {/* File header */}
            <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "20px" }}>
              <div style={{ background: file.color + "20", border: `1px solid ${file.color}44`, borderRadius: "8px", padding: "6px 12px", fontSize: "11px", fontWeight: 700, color: file.color, letterSpacing: "1px" }}>
                {file.layer}
              </div>
              <div style={{ fontSize: "22px", fontWeight: 800 }}>{file.name}</div>
            </div>

            {/* Description */}
            <div style={{ background: SURF1, border: `1px solid ${BORDER}`, borderRadius: "12px", padding: "20px", marginBottom: "16px" }}>
              <div style={{ fontSize: "10px", color: MUTED, letterSpacing: "2px", textTransform: "uppercase", marginBottom: "10px" }}>WHAT THIS FILE DOES</div>
              <p style={{ color: "#CCC", lineHeight: 1.8, fontSize: "14px", margin: 0 }}>{file.desc}</p>
            </div>

            {/* Key decisions */}
            <div style={{ background: SURF1, border: `1px solid ${BORDER}`, borderRadius: "12px", padding: "20px", marginBottom: "16px" }}>
              <div style={{ fontSize: "10px", color: MUTED, letterSpacing: "2px", textTransform: "uppercase", marginBottom: "14px" }}>KEY DESIGN DECISIONS</div>
              <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                {file.keyDecisions.map((d, i) => (
                  <div key={i} style={{ display: "flex", gap: "12px", alignItems: "flex-start" }}>
                    <span style={{ color: file.color, fontWeight: 800, fontSize: "12px", flexShrink: 0, marginTop: "2px" }}>{String(i+1).padStart(2,"0")}</span>
                    <span style={{ color: "#BCC", fontSize: "13px", lineHeight: 1.6 }}>{d}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Next step */}
            <div style={{ background: file.color + "12", border: `1px solid ${file.color}33`, borderRadius: "12px", padding: "16px 20px", display: "flex", gap: "12px", alignItems: "flex-start" }}>
              <span style={{ fontSize: "20px", flexShrink: 0 }}>→</span>
              <div>
                <div style={{ fontSize: "10px", color: file.color, letterSpacing: "2px", textTransform: "uppercase", fontWeight: 700, marginBottom: "6px" }}>YOUR NEXT STEP</div>
                <div style={{ color: TEXT, fontSize: "13px", lineHeight: 1.6 }}>{file.nextStep}</div>
              </div>
            </div>

            {/* Architecture reminder */}
            <div style={{ marginTop: "24px", background: SURF2, borderRadius: "12px", padding: "16px 20px" }}>
              <div style={{ fontSize: "10px", color: MUTED, letterSpacing: "2px", textTransform: "uppercase", marginBottom: "12px" }}>DATA FLOW</div>
              <div style={{ display: "flex", alignItems: "center", gap: "8px", flexWrap: "wrap" }}>
                {["CameraX","ML Kit OCR","GeminiParser","ScanViewModel","Room DB","HomeScreen"].map((step, i, arr) => (
                  <div key={step} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                    <span style={{
                      background: step === file.name.replace(".kt","") || file.name.includes(step.replace("Screen","").replace("Parser","").toLowerCase()) ? AMBER + "22" : SURF3,
                      border: `1px solid ${BORDER}`, borderRadius: "6px",
                      padding: "4px 10px", fontSize: "11px", fontWeight: 600,
                      color: TEXT
                    }}>{step}</span>
                    {i < arr.length - 1 && <span style={{ color: MUTED, fontSize: "12px" }}>→</span>}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Footer */}
      <div style={{ borderTop: `1px solid ${BORDER}`, padding: "14px 28px", display: "flex", gap: "24px", fontSize: "11px", color: MUTED }}>
        {[["Domain", "1 file"], ["Data", "2 files"], ["AI Service", "1 file"], ["UI", "4 screens"], ["Config", "1 file"], ["Infra", "1 file"]].map(([k,v]) => (
          <span key={k}><span style={{ color: TEXT, fontWeight: 600 }}>{v}</span> {k}</span>
        ))}
        <span style={{ marginLeft: "auto", color: AMBER, fontWeight: 700 }}>Week 1 target: Models + Database + GeminiParser running ✓</span>
      </div>
    </div>
  );
}

[README.md](https://github.com/user-attachments/files/34194457/README.md)
# NanoScholar: CBSE PCM Prep Engine & Analytics

**Target SDG:** SDG 4 (Quality Education)  
**Tech Stack:** Semantic HTML5, Vanilla CSS3, Core Java (`com.sun.net.httpserver.HttpServer`)  
**Brand Identity:** NanoScholar &middot; Primary Colors: `#48a7e3` (Sky Blue) & `#0b0f33` (Midnight Navy)  
**Logo:** Custom vector SVG book logo (`/logo.svg`, `/icon.svg`)  
**Footprint:** Under 50KB total wire payload (HTML + CSS = ~44KB) &middot; Zero external fonts &middot; Zero JS frameworks &middot; Zero charting libraries

A high-performance, ultra-low-bandwidth study portal and CBSE Class 11 & 12 PCM (Physics, Chemistry, Mathematics) preparation engine engineered for students on 2G and 3G connections. Includes user session authentication, offline PWA caching, an animated sidebar, a massive CBSE PYQ question bank, a custom timed test generator, revision formula masonry cards, paginated chapter notes, native HTML5 progress tracking, personalized question notebooks, and an instant dark/light theme engine.

---

## Brand & Visual System

- **Brand Name:** **NanoScholar**
- **Brand Palette:**
  - **Sky Blue (`#48a7e3`):** Primary interactive highlights, active tabs, progress values, buttons, and accents.
  - **Midnight Navy (`#0b0f33`):** Dominant contrast structural color, sidebar accents, solid buttons, headings, and dark mode background.
- **Brand Logo:** Integrated vector book logo rendered cleanly at `/logo.svg` across desktop, mobile topbar, and authentication screens.

---

## Advanced Feature Architecture

### A. Massive Question Bank & Custom Quizzes (Backend Heavy, Frontend Light)
- **CBSE Previous Year Question (PYQ) Bank**: Pre-seeded with 24+ authentic CBSE Board questions and NCERT Exemplar problems covering Class 11 & 12 PCM.
- **Custom Test Generator (`/test-builder`)**:
  - Filter by Grade (11 or 12), Subject (All, Math, Physics, Chemistry), Question Count (3, 5, 8), and Timed Mode (Untimed, 5 min, 10 min, 15 min).
  - Dynamically queries in-memory collections and serves a clean, paginated test paper.
- **Timed Modes**:
  - A tiny 12-line Vanilla JS `setInterval` countdown timer with automatic time-up warnings and auto-submission.
- **Native Solution Views (`<details>` & `<summary>`)**:
  - Solutions and verified marking schemes are rendered with native HTML5 `<details>` and `<summary>` tags, allowing seamless browser disclosure without JavaScript toggle functions.

### B. Revision Hub: Formula Cards & Chapter Notes
- **Formula Cards (`/formulas`)**:
  - Pure CSS Grid masonry layout (`grid-template-columns: repeat(auto-fill, minmax(18rem, 1fr))`).
  - Zero external images for formulas: uses crisp Unicode characters and mathematical typography ($v^2 = u^2 + 2as$, $d = \frac{Z \cdot M}{a^3 \cdot N_A}$, $\lambda = \frac{h}{p}$, $1 / 4\pi\varepsilon_0$, etc.).
  - Categorized by Subject and Grade with exam tips and high-yield alerts.
- **Chapter Notes (`/notes`)**:
  - Clean, semantic HTML notes paginated on the Java backend (`/notes?id=...&page=1`, `/notes?id=...&page=2`).

### C. Progress Tracking & Personalized Notebooks
- **Preparation Tracker (`/profile`)**:
  - Calculated on the Java backend and rendered visually using the native HTML5 `<progress max="100" value="...">` tag with custom CSS styling.
  - Zero heavy charting libraries (no Chart.js, D3, or Canvas bloat).
  - Tracks Overall CBSE Readiness, PYQ Test Accuracy, Subject Mastery (Math, Physics, Chemistry), and Saved Notebook Problems.
- **Personalized Notebooks (Bookmarks - `/notebook`)**:
  - Every question features an inline SVG bookmark icon.
  - Clicking sends a lightweight background request (`fetch('/api/bookmark?id=...')`) linking the question ID to the user's session.
  - View all bookmarked problems and complete solutions in the dedicated `/notebook` view.

### D. Accessibility: Authentication & Offline PWA
- **Session Authentication (`/login`, `/register`, `/logout`)**:
  - Backend Java Session management with secure `HttpOnly; SameSite=Lax` cookies.
  - Route protection redirects unauthenticated requests to `/login`.
  - Default student account: `student` / `password123`.
- **Offline PWA (`/sw.js`, `/manifest.json`, `/offline`)**:
  - Service Worker caches CSS, logo, dashboard, formula cards, and offline fallback in `CacheStorage`.
  - If a 2G network drops or internet disconnects entirely, cached formula cards and lessons load smoothly without the browser's "No Internet" screen.

### E. Dark/Light Theme Engine
- **Pure CSS Custom Properties**:
  - Implemented using variables mapped to `[data-theme="dark"]` on the `<html>` root with midnight navy `#0b0f33` as the dark theme background.
- **Flicker-Free Toggle**:
  - Attached to an inline Sun/Moon SVG button that toggles `data-theme` and saves to `localStorage`.
  - Immediate `<head>` script checks `localStorage` prior to DOM paint to eliminate dark mode flashes.

### F. Utilitarian Design System (Anti-Vibe-Coded Manifesto)
- **Banned Colors & Styles**: Strictly zero purple, violet, or indigo colors. Zero background or text gradients. All buttons utilize utilitarian slightly-rounded corners (`border-radius: 4px` / `var(--radius-sm)`), strictly rejecting pill shapes.
- **Banned UX/UI Elements**: Zero custom cursor animations. Zero text emojis: replaced 100% with sharp, single-color inline SVGs (`SVG_LIGHTBULB`, `SVG_CLOCK`, `SVG_CHECK`, `SVG_ALERT`, `SVG_NOTEBOOK`, `SVG_SEARCH`, `SVG_SIGNAL`). Zero dashes used as bullet points or design accents: replaced with proper semantic tags and colon separators.
- **Banned Content ("Zero-Slop" Policy)**: Zero fake reviews, zero fake user metrics, zero customer testimonials, zero AI-generated stock photos, and zero verbose copywriting. 100% factual, concise CBSE Class 11 and 12 curriculum content.

### G. Mathematical Alignment & Cross-Device Fluidity
- **Desktop Tab Distribution**: Navigation tabs and curriculum category buttons span full container width with balanced proportions via `flex: 1`.
- **Mobile Horizontal Tab Scroll**: On viewports under 768px (tested down to 320px), tabs smoothly scroll horizontally via `display: flex; flex-wrap: nowrap; overflow-x: auto;` without breaking layout or causing horizontal document overflow.
- **Strict Spacing Scale**: Standardized spacing scale via CSS custom properties (`--space-3xs` through `--space-2xl`).
- **Responsive Typography**: Font sizes smoothly scale via CSS `clamp()` (`clamp(1.65rem, 3.8vw, 2.75rem)` for H1, `clamp(1.2rem, 2.5vw, 1.55rem)` for H2) with `word-break: break-word` preventing text spillover on 320px devices.

### H. Pre-Launch Checklist & Legal Compliance
- **Custom Domain & Branding**: Canonical URL tags pointing to `https://nanoscholar.edu`. Minimalist vector SVG favicon served at `/favicon.svg` with binary ICO fallback at `/favicon.ico`.
- **Scrubbed AI Footprints**: 100% scrubbed of template tags, "Made with AI", or generator references.
- **Legal Compliance Pages**: Dedicated Privacy Policy (`/privacy`) and Terms and Conditions (`/terms`) accessible both authenticated and unauthenticated, linked across sidebar and auth footers.

---

## Final Performance & QA Verification

- **Total Initial Wire Payload**: Login HTML (~3.8KB) + Stylesheet (~35.5KB) + Logo (~4.8KB) = **~44.2KB** (strictly under the 50KB limit).
- **Total JavaScript Footprint**: All inline scripts combined (theme toggle, bookmark sync, collapsible sidebar, native IntersectionObserver scroll reveals, and PWA registration) total **1,988 bytes (< 2 KB)**.
- **Cross-Device Integrity**: Responsive CSS adapts down to 320px mobile viewport with zero horizontal scroll overflow.

---

## Quickstart

Requires a JDK 11 or newer (compatible with Java 11 through Java 27+). From the project root:

```powershell
# 1. Compile Java sources
javac -d out src/main/java/*.java

# 2. Launch the HTTP server
java -cp out StudyPortal
```

Open [http://localhost:8080/](http://localhost:8080/) in any browser:
- **Welcome Gateway (Zero-Scroll Entry):** [http://localhost:8080/](http://localhost:8080/)
- **Enter Portal (Sign In):** [http://localhost:8080/login](http://localhost:8080/login)
- **Default Student Account:** `student` / `password123`

---

## Route Map

| Route | Method | Description |
|---|---|---|
| `/` | `GET` | Single-screen zero-scroll Welcome Gateway (< 5KB payload) |
| `/welcome` | `GET` | Direct Welcome Gateway access |
| `/login` | `GET`, `POST` | Minimalist centered authentication form with NanoScholar branding & legal links |
| `/register` | `GET`, `POST` | Student registration with Class 11 / 12 selection & legal links |
| `/logout` | `GET` | Session invalidation |
| `/dashboard` | `GET` | Student dashboard with curriculum and tools |
| `/test-builder` | `GET` | Custom CBSE PYQ test generator |
| `/test` | `GET`, `POST` | Dynamic test paper with countdown timer & evaluation |
| `/formulas` | `GET` | Revision Hub formula cards with pure CSS masonry |
| `/notes` | `GET` | Paginated chapter notes with exam pitfalls |
| `/notebook` | `GET` | Personalized saved bookmarked problems |
| `/api/bookmark` | `POST` | Toggle question bookmark in user session |
| `/profile` | `GET` | Analytics & preparation tracker with native `<progress>` |
| `/class?grade=...` | `GET` | Class 11 and Class 12 curriculum tracks |
| `/lesson?id=...` | `GET` | Chapter lesson reader with progress indicator |
| `/privacy` | `GET` | Dedicated student Privacy Policy page (Zero-Tracking Guarantee) |
| `/terms` | `GET` | Dedicated Terms & Conditions of Service page (Academic Fair Use) |
| `/favicon.svg` | `GET` | Custom minimalist vector SVG favicon |
| `/favicon.ico` | `GET` | Custom 16x16 binary ICO favicon fallback |
| `/logo.svg` | `GET` | Transparent vector SVG book logo |
| `/sw.js` | `GET` | PWA Service Worker for offline caching (v5) |
| `/manifest.json` | `GET` | Web App Manifest with SVG & ICO icons |
| `/offline` | `GET` | Offline fallback page with vector signal SVG |
| `/style.css` | `GET` | High-performance Vanilla CSS3 stylesheet (#48a7e3 & #0b0f33) |

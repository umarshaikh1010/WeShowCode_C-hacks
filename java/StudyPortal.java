import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class StudyPortal {
    // In-memory data structures
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();
    private static final Map<String, String> SESSIONS = new ConcurrentHashMap<>();
    private static final List<Subject> SUBJECTS = new ArrayList<>();
    private static final Map<String, Subject> SUBJECT_MAP = new HashMap<>();
    private static final Map<String, Lesson> LESSON_MAP = new HashMap<>();
    private static final List<Question> QUESTION_BANK = new ArrayList<>();
    private static final Map<Integer, Question> QUESTION_MAP = new HashMap<>();
    private static final List<Formula> FORMULAS = new ArrayList<>();
    private static final Map<String, ChapterNote> NOTES = new HashMap<>();

    // Inline Minimized SVGs
    private static final String SVG_HAMBURGER = "<svg width=\"22\" height=\"22\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><line x1=\"3\" y1=\"12\" x2=\"21\" y2=\"12\"></line><line x1=\"3\" y1=\"6\" x2=\"21\" y2=\"6\"></line><line x1=\"3\" y1=\"18\" x2=\"21\" y2=\"18\"></line></svg>";
    private static final String SVG_CLOSE = "<svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><line x1=\"18\" y1=\"6\" x2=\"6\" y2=\"18\"></line><line x1=\"6\" y1=\"6\" x2=\"18\" y2=\"18\"></line></svg>";
    private static final String SVG_USER = "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2\"></path><circle cx=\"12\" cy=\"7\" r=\"4\"></circle></svg>";
    private static final String SVG_HOME = "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z\"></path><polyline points=\"9 22 9 12 15 12 15 22\"></polyline></svg>";
    private static final String SVG_BOOK = "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M4 19.5A2.5 2.5 0 0 1 6.5 17H20\"></path><path d=\"M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z\"></path></svg>";
    private static final String SVG_TEST = "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z\"></path><polyline points=\"14 2 14 8 20 8\"></polyline><line x1=\"9\" y1=\"15\" x2=\"15\" y2=\"15\"></line><line x1=\"9\" y1=\"11\" x2=\"11\" y2=\"11\"></line></svg>";
    private static final String SVG_FORMULA = "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M4 7h16\"></path><path d=\"M9 20h6\"></path><path d=\"M12 4v16\"></path><path d=\"M8 12h8\"></path></svg>";
    private static final String SVG_NOTEBOOK = "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z\"></path></svg>";
    private static final String SVG_NOTES = "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z\"></path><path d=\"M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z\"></path></svg>";
    private static final String SVG_LOGOUT = "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4\"></path><polyline points=\"16 17 21 12 16 7\"></polyline><line x1=\"21\" y1=\"12\" x2=\"9\" y2=\"12\"></line></svg>";
    private static final String SVG_THEME = "<svg width=\"17\" height=\"17\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><circle cx=\"12\" cy=\"12\" r=\"5\"></circle><line x1=\"12\" y1=\"1\" x2=\"12\" y2=\"3\"></line><line x1=\"12\" y1=\"21\" x2=\"12\" y2=\"23\"></line><line x1=\"4.22\" y1=\"4.22\" x2=\"5.64\" y2=\"5.64\"></line><line x1=\"18.36\" y1=\"18.36\" x2=\"19.78\" y2=\"19.78\"></line><line x1=\"1\" y1=\"12\" x2=\"3\" y2=\"12\"></line><line x1=\"21\" y1=\"12\" x2=\"23\" y2=\"12\"></line><line x1=\"4.22\" y1=\"19.78\" x2=\"5.64\" y2=\"18.36\"></line><line x1=\"18.36\" y1=\"5.64\" x2=\"19.78\" y2=\"4.22\"></line></svg>";
    private static final String SVG_MATH = "<svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><line x1=\"4\" y1=\"19\" x2=\"20\" y2=\"19\"></line><polyline points=\"4 15 9 9 14 14 20 5\"></polyline></svg>";
    private static final String SVG_PHYSICS = "<svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><ellipse cx=\"12\" cy=\"12\" rx=\"10\" ry=\"4\" transform=\"rotate(30 12 12)\"></ellipse><ellipse cx=\"12\" cy=\"12\" rx=\"10\" ry=\"4\" transform=\"rotate(-30 12 12)\"></ellipse><circle cx=\"12\" cy=\"12\" r=\"2\" fill=\"currentColor\"></circle></svg>";
    private static final String SVG_CHEMISTRY = "<svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M10 2v7.5l-5.5 9A2 2 0 0 0 6.2 21.5h11.6a2 2 0 0 0 1.7-3l-5.5-9V2h-4z\"></path><line x1=\"8.5\" y1=\"2\" x2=\"15.5\" y2=\"2\"></line><line x1=\"7\" y1=\"15\" x2=\"17\" y2=\"15\"></line></svg>";
    private static final String SVG_ARROW = "<svg width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><line x1=\"5\" y1=\"12\" x2=\"19\" y2=\"12\"></line><polyline points=\"12 5 19 12 12 19\"></polyline></svg>";
    private static final String SVG_SIDEBAR_TOGGLE = "<svg width=\"17\" height=\"17\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><rect x=\"3\" y=\"3\" width=\"18\" height=\"18\" rx=\"2\" ry=\"2\"></rect><line x1=\"9\" y1=\"3\" x2=\"9\" y2=\"21\"></line><path d=\"M15 10l-2 2 2 2\"></path></svg>";
    private static final String SVG_LIGHTBULB = "<svg width=\"15\" height=\"15\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M9 18h6\"></path><path d=\"M10 22h4\"></path><path d=\"M12 2a7 7 0 0 0-7 7c0 2.5 1.5 4.5 3 6h8c1.5-1.5 3-3.5 3-6a7 7 0 0 0-7-7z\"></path></svg>";
    private static final String SVG_CLOCK = "<svg width=\"15\" height=\"15\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><circle cx=\"12\" cy=\"12\" r=\"10\"></circle><polyline points=\"12 6 12 12 16 14\"></polyline></svg>";
    private static final String SVG_CHECK = "<svg width=\"22\" height=\"22\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><polyline points=\"20 6 9 17 4 12\"></polyline></svg>";
    private static final String SVG_ALERT = "<svg width=\"22\" height=\"22\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><circle cx=\"12\" cy=\"12\" r=\"10\"></circle><line x1=\"12\" y1=\"8\" x2=\"12\" y2=\"12\"></line><line x1=\"12\" y1=\"16\" x2=\"12.01\" y2=\"16\"></line></svg>";
    private static final String SVG_SEARCH = "<svg width=\"36\" height=\"36\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><circle cx=\"11\" cy=\"11\" r=\"8\"></circle><line x1=\"21\" y1=\"21\" x2=\"16.65\" y2=\"16.65\"></line></svg>";
    private static final String SVG_SIGNAL = "<svg width=\"36\" height=\"36\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><line x1=\"2\" y1=\"20\" x2=\"2\" y2=\"20\"></line><line x1=\"7\" y1=\"20\" x2=\"7\" y2=\"16\"></line><line x1=\"12\" y1=\"20\" x2=\"12\" y2=\"12\"></line><line x1=\"17\" y1=\"20\" x2=\"17\" y2=\"8\"></line><line x1=\"22\" y1=\"20\" x2=\"22\" y2=\"4\"></line><line x1=\"2\" y1=\"2\" x2=\"22\" y2=\"22\" stroke-width=\"2.5\"></line></svg>";

    public static void main(String[] args) throws IOException {
        seedData();
        int port = 8080;
        if (args != null && args.length > 0) {
            port = parseInt(args[0], 8080);
        } else if (System.getenv("PORT") != null) {
            port = parseInt(System.getenv("PORT"), 8080);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.createContext("/", StudyPortal::handleRequest);
        server.start();
        System.out.println("NanoScholar CBSE Prep Engine running on port " + port);
        System.out.println("Gateway Entry: http://localhost:" + port + "/ (Zero-Scroll Welcome)");
        System.out.println("Enter Portal:  http://localhost:" + port + "/login");
    }

    private static void seedData() {
        // Seed default student account
        USERS.put("student", new User("student", "password123", 11));

        // ---------------- CLASS 11 SUBJECTS & LESSONS ----------------
        Subject s11Math = new Subject("11-MATH", 11, "Mathematics", "Sets, relations, functions & trigonometric foundations", "math", "blue");
        Lesson l11Math1 = new Lesson("11-MATH-1", "11-MATH", "Sets, Relations & Functions", "4 min read",
            "In modern mathematics, a set is a well-defined collection of distinct objects. Understanding sets allows us to formalize every other mathematical structure.\n\nA relation R from set A to set B is a subset of the Cartesian product A × B. A relation on set A is called reflexive if every element relates to itself ((a, a) ∈ R for all a ∈ A), symmetric if (a, b) ∈ R implies (b, a) ∈ R, and transitive if (a, b) ∈ R and (b, c) ∈ R implies (a, c) ∈ R. When a relation satisfies all three conditions, it forms an equivalence relation, partitioning the set into disjoint equivalence classes.\n\nA function is a special type of relation where each element of the domain maps to exactly one element in the codomain. If every distinct element in the domain produces a distinct output, the function is injective (one-to-one). If the range equals the codomain, it is surjective (onto). A function that is both injective and surjective is a bijection, which allows an inverse function to exist.");
        Lesson l11Math2 = new Lesson("11-MATH-2", "11-MATH", "Trigonometric Principles & Identities", "5 min read",
            "Trigonometry extends geometric triangle measurements to circular functions defined across all real numbers. Measuring angles in radians links arc length s to radius r through s = r·θ, grounding angles in direct distance ratios.\n\nThe fundamental Pythagorean identity sin²(θ) + cos²(θ) = 1 arises immediately from the equation of the unit circle x² + y² = 1. From this core relationship, compound angle formulas emerge:\n\ncos(A ± B) = cos(A)cos(B) ∓ sin(A)sin(B)\nsin(A ± B) = sin(A)cos(B) ± cos(A)sin(B)\n\nThese formulas serve as the engine for Fourier analysis, wave mechanics, and alternating current electronics.");
        s11Math.addLesson(l11Math1);
        s11Math.addLesson(l11Math2);
        addSubject(s11Math);

        Subject s11Phys = new Subject("11-PHYS", 11, "Physics", "Kinematics, dynamics, friction & fundamental laws of motion", "physics", "coral");
        Lesson l11Phys1 = new Lesson("11-PHYS-1", "11-PHYS", "Kinematics & Motion in 1D", "4 min read",
            "Kinematics describes the motion of objects without reference to the forces causing that motion. Displacement is the net change in position: Δx = x_final - x_initial.\n\nWhen acceleration is constant (uniform acceleration), calculus yields the classic kinematic equations:\n1) v = u + at\n2) s = ut + (1/2)at²\n3) v² = u² + 2as\n\nThese three relationships govern trajectory analysis, projectile mechanics, and vehicle deceleration calculations.");
        Lesson l11Phys2 = new Lesson("11-PHYS-2", "11-PHYS", "Newton's Laws & Friction Dynamics", "5 min read",
            "Classical dynamics rests upon Isaac Newton's three laws of motion. The First Law establishes inertia. The Second Law defines force quantitatively as F = dp/dt = ma. The Third Law establishes action-reaction pairs.\n\nFriction is the contact force resisting relative lateral motion. Static friction prevents sliding up to f_s ≤ μ_s·N. Kinetic friction f_k = μ_k·N opposes continuous sliding motion.");
        s11Phys.addLesson(l11Phys1);
        s11Phys.addLesson(l11Phys2);
        addSubject(s11Phys);

        Subject s11Chem = new Subject("11-CHEM", 11, "Chemistry", "Atomic orbitals, quantum numbers & chemical bonding structures", "chemistry", "sage");
        Lesson l11Chem1 = new Lesson("11-CHEM-1", "11-CHEM", "Atomic Structure & Quantum Model", "5 min read",
            "Early atomic models could not explain atomic emission spectra. The quantum mechanical model resolved this by acknowledging wave-particle duality (λ = h/p).\n\nWerner Heisenberg's Uncertainty Principle established Δx · Δp ≥ h / (4π). An electron's quantum state is described by four quantum numbers: Principal (n), Azimuthal (l), Magnetic (m_l), and Spin (m_s).");
        Lesson l11Chem2 = new Lesson("11-CHEM-2", "11-CHEM", "Chemical Bonding & Molecular Shapes", "5 min read",
            "Atoms form bonds to achieve octet stability. Valence Shell Electron Pair Repulsion (VSEPR) theory predicts 3D geometries based on repulsion between electron pairs: Linear (180°), Trigonal planar (120°), and Tetrahedral (109.5°).");
        s11Chem.addLesson(l11Chem1);
        s11Chem.addLesson(l11Chem2);
        addSubject(s11Chem);

        // ---------------- CLASS 12 SUBJECTS & LESSONS ----------------
        Subject s12Math = new Subject("12-MATH", 12, "Mathematics", "Matrices, determinants, continuity & differential calculus", "math", "blue");
        Lesson l12Math1 = new Lesson("12-MATH-1", "12-MATH", "Matrices & Systems of Equations", "5 min read",
            "A matrix is a rectangular array of numbers arranged in m rows and n columns. For a square matrix A, its determinant det(A) determines invertibility: if det(A) ≠ 0, A⁻¹ = (1/det(A))·adj(A).");
        Lesson l12Math2 = new Lesson("12-MATH-2", "12-MATH", "Continuity & Differential Calculus", "5 min read",
            "A function f(x) is continuous at x = c if lim_{x→c} f(x) = f(c). Differentiability requires a unique tangent slope. The Chain Rule d/dx[f(g(x))] = f'(g(x))·g'(x) powers composite calculus.");
        s12Math.addLesson(l12Math1);
        s12Math.addLesson(l12Math2);
        addSubject(s12Math);

        Subject s12Phys = new Subject("12-PHYS", 12, "Physics", "Electrostatics, Coulomb's law & electromagnetic induction", "physics", "coral");
        Lesson l12Phys1 = new Lesson("12-PHYS-1", "12-PHYS", "Electrostatics & Coulomb's Law", "5 min read",
            "Coulomb's Law states that electrostatic force between two point charges is F = (1 / 4πε₀) · (|q₁ q₂| / r²). Gauss's Law states that total electric flux equals ∮ E · dA = Q_enclosed / ε₀.");
        Lesson l12Phys2 = new Lesson("12-PHYS-2", "12-PHYS", "Electromagnetic Induction & Faraday's Law", "5 min read",
            "Faraday's Law states that induced EMF is ε = -N · (dΦ_B / dt). The negative sign embodies Heinrich Lenz's Law: induced current opposes the change in magnetic flux that produced it.");
        s12Phys.addLesson(l12Phys1);
        s12Phys.addLesson(l12Phys2);
        addSubject(s12Phys);

        Subject s12Chem = new Subject("12-CHEM", 12, "Chemistry", "Solid state unit cells, crystal lattices & reaction kinetics", "chemistry", "sage");
        Lesson l12Chem1 = new Lesson("12-CHEM-1", "12-CHEM", "Solid State & Crystal Lattices", "5 min read",
            "The unit cell is the fundamental repeating unit of a crystal. Three cubic forms exist: Simple Cubic (1 atom, 52.4% packing), BCC (2 atoms, 68% packing), and FCC (4 atoms, 74% packing). Density d = (Z·M) / (a³·N_A).");
        Lesson l12Chem2 = new Lesson("12-CHEM-2", "12-CHEM", "Chemical Kinetics & Reaction Rates", "5 min read",
            "Reaction rate measures concentration change per unit time. For first-order reactions, half-life t₁/₂ = ln(2)/k ≈ 0.693/k is independent of initial concentration. Arrhenius equation: k = A·e^(-Ea/RT).");
        s12Chem.addLesson(l12Chem1);
        s12Chem.addLesson(l12Chem2);
        addSubject(s12Chem);

        // ---------------- MASSIVE CBSE PCM QUESTION BANK (PYQs & MOCKS) ----------------
        // Class 11 Mathematics
        addQuestion(new Question(1, "11-MATH", 11, "Sets & Relations", "CBSE 2023", "Standard",
            "Let set A = {1, 2, 3}. What is the total number of non-empty subsets of A?",
            new String[]{"6", "7", "8", "9"}, 1,
            "Total number of subsets of a set with n elements is 2ⁿ. Here n = 3, so total subsets = 2³ = 8. Subtracting the empty set ∅ gives 8 - 1 = 7 non-empty subsets."));

        addQuestion(new Question(2, "11-MATH", 11, "Sets & Relations", "CBSE 2022 Term-1", "Standard",
            "What condition must a relation R on set A satisfy to be classified as an equivalence relation?",
            new String[]{"Reflexive only", "Symmetric and transitive only", "Reflexive, symmetric, and transitive", "Injective and surjective"}, 2,
            "An equivalence relation on set A must simultaneously satisfy: (i) Reflexivity: (a, a) ∈ R ∀ a ∈ A; (ii) Symmetry: (a, b) ∈ R ⇒ (b, a) ∈ R; (iii) Transitivity: (a, b) ∈ R and (b, c) ∈ R ⇒ (a, c) ∈ R."));

        addQuestion(new Question(3, "11-MATH", 11, "Trigonometry", "CBSE 2020", "Advanced",
            "What is the exact value of cos(75°)?",
            new String[]{"(√6 - √2) / 4", "(√6 + √2) / 4", "(√3 - 1) / (2√2)", "1 / (√6 - √2)"}, 0,
            "Using compound angle identity cos(A + B) = cos(A)cos(B) - sin(A)sin(B) with A = 45° and B = 30°: cos(45°+30°) = cos(45°)cos(30°) - sin(45°)sin(30°) = (1/√2)(√3/2) - (1/√2)(1/2) = (√3 - 1)/(2√2) = (√6 - √2)/4."));

        addQuestion(new Question(4, "11-MATH", 11, "Trigonometry", "CBSE Sample Paper", "Standard",
            "If tan(θ) = 3/4 and θ lies in the third quadrant, what is the value of sec(θ)?",
            new String[]{"5/4", "-5/4", "-4/5", "4/3"}, 1,
            "sec²(θ) = 1 + tan²(θ) = 1 + 9/16 = 25/16. In Quadrant III, cosine and secant are negative, so sec(θ) = -√(25/16) = -5/4."));

        // Class 11 Physics
        addQuestion(new Question(5, "11-PHYS", 11, "Kinematics", "CBSE 2023", "Standard",
            "For an object with initial velocity 'u' moving with constant acceleration 'a', which equation computes final velocity 'v' across displacement 's' without time 't'?",
            new String[]{"v = u + at", "v² = u² + 2as", "s = ut + (1/2)at²", "v² = 2as - u²"}, 1,
            "The third kinematic relation derived from calculus v(dv/ds) = a integrates to v² - u² = 2as, giving v² = u² + 2as."));

        addQuestion(new Question(6, "11-PHYS", 11, "Kinematics", "CBSE 2021", "Standard",
            "A car traveling at speed 'v' skids to a stop in distance 's'. If the speed is doubled to '2v', what will be the stopping distance under the same braking friction?",
            new String[]{"2s", "3s", "4s", "8s"}, 2,
            "From v_final² = v_initial² - 2as, stopping distance s = v² / (2a). Since deceleration 'a' is constant, stopping distance is proportional to the square of velocity (s ∝ v²). Doubling velocity quadruples stopping distance to (2)²s = 4s."));

        addQuestion(new Question(7, "11-PHYS", 11, "Laws of Motion", "CBSE 2020 Delhi", "Advanced",
            "A block of mass m rests on a rough horizontal surface with coefficient of static friction μ_s. What is the minimum horizontal force required to initiate motion?",
            new String[]{"μ_s · m · g", "m · g / μ_s", "μ_s · m", "(1 - μ_s) · m · g"}, 0,
            "The maximum static frictional force opposes applied force up to f_s(max) = μ_s · N. On a level plane, normal force N = mg, so F_min = μ_s · m · g."));

        addQuestion(new Question(8, "11-PHYS", 11, "Laws of Motion", "NCERT Exemplar", "Standard",
            "An elevator of mass m is accelerating upwards with acceleration 'a'. What is the apparent weight recorded by a scale inside?",
            new String[]{"m(g - a)", "m(g + a)", "mg", "m · a"}, 1,
            "Setting up the vertical equation of motion: N - mg = ma ⇒ Apparent Weight N = m(g + a)."));

        // Class 11 Chemistry
        addQuestion(new Question(9, "11-CHEM", 11, "Atomic Structure", "CBSE 2023", "Standard",
            "According to the de Broglie relationship, what is the wavelength λ associated with a particle of mass m moving with velocity v?",
            new String[]{"λ = h / (m · v)", "λ = h · m · v", "λ = (m · v) / h", "λ = h² / (2m · v)"}, 0,
            "Louis de Broglie proposed that matter exhibits wave properties with wavelength λ = h / p = h / (m · v), where h is Planck's constant (6.626 × 10⁻³⁴ J·s)."));

        addQuestion(new Question(10, "11-CHEM", 11, "Atomic Structure", "CBSE 2022", "Standard",
            "How many orbitals are associated with principal quantum number n = 3?",
            new String[]{"3", "6", "9", "18"}, 2,
            "For principal quantum number n, the total number of orbitals is given by n². For n = 3: n² = 3² = 9 orbitals (one 3s, three 3p, five 3d). Maximum electron capacity is 2n² = 18 electrons."));

        addQuestion(new Question(11, "11-CHEM", 11, "Chemical Bonding", "CBSE 2020", "Standard",
            "According to VSEPR theory, what is the spatial geometry and bond angle for methane (CH₄)?",
            new String[]{"Linear (180°)", "Trigonal Planar (120°)", "Tetrahedral (109.5°)", "Octahedral (90°)"}, 2,
            "Carbon in CH₄ possesses 4 bonding pairs and 0 lone pairs. To minimize electrostatic repulsion in 3D space, the 4 sp³ hybrid orbitals orient toward the corners of a regular tetrahedron with 109.5° bond angles."));

        addQuestion(new Question(12, "11-CHEM", 11, "Chemical Bonding", "CBSE Sample Paper", "Advanced",
            "Which of the following molecules has a net dipole moment of zero due to symmetrical cancellation?",
            new String[]{"H₂O", "NH₃", "CO₂", "SO₂"}, 2,
            "CO₂ has a linear geometry (O=C=O). The two equal C=O bond dipoles point in exactly opposite directions (180°), canceling each other out to produce a resultant molecular dipole moment μ = 0 D."));

        // Class 12 Mathematics
        addQuestion(new Question(13, "12-MATH", 12, "Matrices", "CBSE 2023", "Standard",
            "If matrix A is an invertible square matrix of order 3 and det(A) = 4, what is det(adj A)?",
            new String[]{"4", "16", "64", "1/4"}, 1,
            "For any n × n square matrix A, det(adj A) = (det(A))^(n-1). Here n = 3 and det(A) = 4, so det(adj A) = 4^(3 - 1) = 4² = 16."));

        addQuestion(new Question(14, "12-MATH", 12, "Matrices", "CBSE 2022 Term-1", "Standard",
            "If the determinant of a square matrix A is zero (det(A) = 0), what does it indicate about its multiplicative inverse A⁻¹?",
            new String[]{"A⁻¹ is the identity matrix", "A⁻¹ does not exist (A is singular)", "A⁻¹ equals A", "A⁻¹ has determinant 1"}, 1,
            "A square matrix is invertible if and only if its determinant is non-zero. When det(A) = 0, matrix A is singular and no multiplicative inverse exists."));

        addQuestion(new Question(15, "12-MATH", 12, "Calculus", "CBSE 2020 All India", "Advanced",
            "What is the derivative of f(x) = x^x with respect to x for x > 0?",
            new String[]{"x · x^(x - 1)", "x^x · (1 + ln x)", "x^x · ln x", "x^(x - 1)"}, 1,
            "Using logarithmic differentiation: let y = x^x ⇒ ln y = x · ln x. Differentiating both sides: (1/y)(dy/dx) = ln x + x(1/x) = 1 + ln x ⇒ dy/dx = y(1 + ln x) = x^x(1 + ln x)."));

        addQuestion(new Question(16, "12-MATH", 12, "Calculus", "CBSE Sample Paper", "Standard",
            "At which points can a continuous function f(x) achieve local maximum or minimum values?",
            new String[]{"Only where f'(x) > 0", "Where f'(x) = 0 or f'(x) does not exist (critical points)", "Only at boundary points", "Where f''(x) = 0"}, 1,
            "By Fermat's Theorem on stationary points, local extrema occur only at critical points where the first derivative f'(x) equals 0 or where f'(x) fails to exist."));

        // Class 12 Physics
        addQuestion(new Question(17, "12-PHYS", 12, "Electrostatics", "CBSE 2023", "Standard",
            "Two identical point charges separated by distance r exert electrostatic force F on each other. If distance r is halved, what is the new force?",
            new String[]{"F / 2", "2F", "4F", "F / 4"}, 2,
            "By Coulomb's inverse-square law, F ∝ 1/r². Halving distance r to r/2 scales the force by 1 / (1/2)² = 4, yielding 4F."));

        addQuestion(new Question(18, "12-PHYS", 12, "Electrostatics", "CBSE 2021", "Advanced",
            "According to Gauss's Law, what is the total electric flux through a closed spherical surface enclosing a net dipole (+q and -q)?",
            new String[]{"2q / ε₀", "q / ε₀", "0", "-q / ε₀"}, 2,
            "Gauss's Law states ∮ E · dA = Q_enclosed / ε₀. For an electric dipole, Q_enclosed = (+q) + (-q) = 0. Therefore total electric flux is zero."));

        addQuestion(new Question(19, "12-PHYS", 12, "Electromagnetic Induction", "CBSE 2020", "Standard",
            "Which fundamental law dictates that an induced current always opposes the magnetic flux change that created it?",
            new String[]{"Coulomb's Law", "Ampère's Circuital Law", "Lenz's Law", "Biot-Savart Law"}, 2,
            "Lenz's Law (represented by the negative sign in ε = -dΦ/dt) enforces energy conservation: induced currents oppose the change in magnetic flux that induced them."));

        addQuestion(new Question(20, "12-PHYS", 12, "Electromagnetic Induction", "NCERT Exemplar", "Advanced",
            "A straight wire conductor of length L moves with velocity v perpendicular to uniform magnetic field B. What is the motional EMF induced across its ends?",
            new String[]{"B · L · v", "B² · L · v", "B · L / v", "Zero"}, 0,
            "Lorentz force on free charge carriers q within the moving wire is F = q(v × B). The induced electric field E = v · B produces motional EMF ε = E · L = B · L · v."));

        // Class 12 Chemistry
        addQuestion(new Question(21, "12-CHEM", 12, "Solid State", "CBSE 2023", "Standard",
            "What is the total number of atoms per unit cell in a Face-Centered Cubic (FCC / CCP) crystal lattice?",
            new String[]{"1", "2", "4", "6"}, 2,
            "In FCC: 8 corner atoms × (1/8 contribution) = 1 atom; 6 face-centered atoms × (1/2 contribution) = 3 atoms. Total net atoms Z = 1 + 3 = 4 atoms."));

        addQuestion(new Question(22, "12-CHEM", 12, "Solid State", "CBSE 2022 Term-1", "Standard",
            "What is the percentage packing efficiency of a Body-Centered Cubic (BCC) unit cell?",
            new String[]{"52.4%", "68.0%", "74.0%", "82.5%"}, 1,
            "Packing efficiency of BCC unit cell is 68.0% (Simple Cubic = 52.4%, FCC = 74.0%). The unoccupied space (void fraction) in BCC is 32.0%."));

        addQuestion(new Question(23, "12-CHEM", 12, "Chemical Kinetics", "CBSE 2020", "Standard",
            "For a first-order chemical reaction, how does the half-life (t₁/₂) depend on initial reactant concentration [A]₀?",
            new String[]{"Directly proportional to [A]₀", "Inversely proportional to [A]₀", "Completely independent of [A]₀", "Proportional to [A]₀²"}, 2,
            "For a first-order reaction, integrated rate kinetics yield t₁/₂ = ln(2) / k ≈ 0.693 / k. The concentration term cancels out, making half-life independent of initial reactant concentration."));

        addQuestion(new Question(24, "12-CHEM", 12, "Chemical Kinetics", "CBSE Sample Paper", "Advanced",
            "According to the Arrhenius equation k = A · e^(-Ea / RT), what does a plot of ln(k) versus (1 / T) yield?",
            new String[]{"A parabola with minimum at Ea", "A straight line with slope -Ea / R", "A straight line with slope +Ea / R", "An exponential curve"}, 1,
            "Taking natural logarithms of Arrhenius equation gives ln(k) = ln(A) - (Ea / R) · (1 / T). Comparing with y = mx + c reveals a straight line with negative slope m = -Ea / R and y-intercept ln(A)."));

        // ---------------- REVISION HUB: FORMULA CARDS (PURE CSS MASONRY) ----------------
        // Class 11 Math
        FORMULAS.add(new Formula("F-11-M-1", 11, "Mathematics", "Trigonometry", "Compound & Double Angle Identities",
            "sin(A ± B) = sin A cos B ± cos A sin B\ncos(A ± B) = cos A cos B ∓ sin A sin B\nsin(2θ) = 2 sin θ cos θ\ncos(2θ) = cos²θ - sin²θ = 2cos²θ - 1",
            "Fundamental addition theorems for transforming sums into products and calculating non-standard angle values.",
            "CBSE High-Yield: Always remember the sign flip in cos(A + B) = cos A cos B - sin A sin B."));

        FORMULAS.add(new Formula("F-11-M-2", 11, "Mathematics", "Sets & Functions", "Subsets & Cartesian Cardinality",
            "n(P(A)) = 2^n\nn(A × B) = n(A) · n(B)\nn(A ∪ B) = n(A) + n(B) - n(A ∩ B)",
            "Cardinality equations for power sets, relations, and inclusion-exclusion principle across sets.",
            "CBSE Tip: Total relations from set A to set B is 2^(n(A)·n(B))."));

        // Class 11 Physics
        FORMULAS.add(new Formula("F-11-P-1", 11, "Physics", "Kinematics", "Equations of Uniformly Accelerated Motion",
            "v = u + at\ns = ut + ½at²\nv² = u² + 2as\ns_nth = u + ½a(2n - 1)",
            "Kinematic relations connecting initial velocity u, final velocity v, acceleration a, time t, and displacement s.",
            "Exam Alert: Only valid when acceleration 'a' is strictly constant. For variable acceleration use calculus: v = ds/dt, a = dv/dt."));

        FORMULAS.add(new Formula("F-11-P-2", 11, "Physics", "Dynamics", "Friction & Angle of Repose",
            "f_s ≤ μ_s · N\nf_k = μ_k · N\ntan(θ_repose) = μ_s",
            "Static and kinetic contact friction equations governing threshold sliding and inclined plane stability.",
            "Exam Alert: Static friction is self-adjusting up to its maximum value μ_s·N."));

        // Class 11 Chemistry
        FORMULAS.add(new Formula("F-11-C-1", 11, "Chemistry", "Atomic Structure", "De Broglie & Heisenberg Uncertainty",
            "λ = h / p = h / (m · v)\nΔx · Δp ≥ h / (4π)\nΔx · m · Δv ≥ h / (4π)",
            "Core quantum duality relationships linking momentum to matter wavelength and setting fundamental measurement limits.",
            "CBSE Tip: h = 6.626 × 10⁻³⁴ J·s. Make sure mass is converted to kilograms (kg) before computing."));

        FORMULAS.add(new Formula("F-11-C-2", 11, "Chemistry", "Chemical Bonding", "VSEPR Geometry & Formal Charge",
            "Formal Charge = V - N - ½B\nSteric No. = (Bond Pairs) + (Lone Pairs)",
            "Predicting molecular stability, resonance structure viability, and 3D geometric spatial arrangement.",
            "CBSE Tip: Lone pair - lone pair repulsion > lone pair - bond pair repulsion > bond pair - bond pair repulsion."));

        // Class 12 Math
        FORMULAS.add(new Formula("F-12-M-1", 12, "Mathematics", "Matrices", "Determinants & Inversion Formula",
            "A · adj(A) = adj(A) · A = |A| · I\nA⁻¹ = (1 / |A|) · adj(A)\n|adj(A)| = |A|^(n - 1)",
            "Matrix inverse determination and algebraic properties of the classical adjoint.",
            "CBSE High-Yield: A is invertible if and only if |A| ≠ 0. If |A| = 0 and (adj A)B ≠ 0, the system is inconsistent."));

        FORMULAS.add(new Formula("F-12-M-2", 12, "Mathematics", "Calculus", "Differentiation Chain Rule & Product Rule",
            "d/dx [u · v] = u'v + uv'\nd/dx [u / v] = (u'v - uv') / v²\nd/dx [f(g(x))] = f'(g(x)) · g'(x)\nd/dx [a^x] = a^x · ln(a)",
            "Differential calculus operational theorems for composite, product, and quotient expressions.",
            "CBSE Tip: For expressions with variable base and variable exponent (like x^x or (sin x)^x), always apply logarithmic differentiation."));

        // Class 12 Physics
        FORMULAS.add(new Formula("F-12-P-1", 12, "Physics", "Electrostatics", "Coulomb's Law & Gauss's Flux",
            "F = (1 / 4πε₀) · (|q₁ q₂| / r²)\nE = F / q₀\nΦ_E = ∮ E · dA = Q_enclosed / ε₀",
            "Electrostatic force between point charges and total outward electric flux through closed Gaussian surfaces.",
            "CBSE Constants: 1 / (4πε₀) = 9 × 10⁹ N·m²/C²; ε₀ = 8.854 × 10⁻¹² F/m."));

        FORMULAS.add(new Formula("F-12-P-2", 12, "Physics", "Electromagnetism", "Faraday's Law & Self-Inductance",
            "Φ_B = B · A · cos(θ)\nε = -N · (dΦ_B / dt)\nε = -L · (dI / dt)\nU = ½L · I²",
            "Electromagnetic induction equations governing EMF generation in transformers, motors, and inductors.",
            "CBSE Alert: The minus sign in Faraday's law is Lenz's law. Ensure angle θ is measured between magnetic field B and normal vector to area A."));

        // Class 12 Chemistry
        FORMULAS.add(new Formula("F-12-C-1", 12, "Chemistry", "Solid State", "Unit Cell Density & Packing Fractions",
            "d = (Z · M) / (a³ · N_A)\nSC: r = a / 2 (52.4%)\nBCC: r = (√3 / 4)a (68%)\nFCC: r = a / (2√2) (74%)",
            "Crystal lattice density calculations linking microscopic unit cell edge length 'a' to macroscopic density 'd'.",
            "CBSE Tip: Z = 1 for Simple Cubic, Z = 2 for BCC, Z = 4 for FCC. If 'a' is in picometers (pm), multiply by 10⁻¹⁰ cm before cubing."));

        FORMULAS.add(new Formula("F-12-C-2", 12, "Chemistry", "Chemical Kinetics", "First-Order Rate & Arrhenius Equation",
            "k = (2.303 / t) · log([A]₀ / [A])\nt₁/₂ = 0.693 / k\nlog(k₂ / k₁) = (Ea / 2.303R) · [(T₂ - T₁) / (T₁ T₂)]",
            "Rate constant integration and temperature dependence of reaction velocities.",
            "CBSE High-Yield: First order half-life is completely independent of initial reactant concentration. Gas constant R = 8.314 J·K⁻¹·mol⁻¹."));

        // ---------------- CHAPTER NOTES (PAGINATED ON BACKEND) ----------------
        ChapterNote noteKinematics = new ChapterNote("note-11-phys-kinematics", 11, "Physics", "Kinematics", "Motion in a Straight Line & Vectors");
        noteKinematics.addPage("<h2>1. Displacement, Velocity & Acceleration</h2>"
            + "<p><strong>Displacement (Δx):</strong> The shortest directed distance from initial to final position. A vector quantity that can be positive, negative, or zero.</p>"
            + "<p><strong>Instantaneous Velocity:</strong> v = lim(Δt→0) [Δx/Δt] = dx/dt. The slope of the position-time (x-t) curve at any point gives instantaneous velocity.</p>"
            + "<p><strong>Instantaneous Acceleration:</strong> a = dv/dt = d²x/dt². The slope of the velocity-time (v-t) curve represents acceleration.</p>"
            + "<p><strong>Area Under Curves:</strong> Area under v-t graph = net displacement. Area under a-t graph = change in velocity (v - u).</p>");
        noteKinematics.addPage("<h2>2. Uniform Acceleration Equations & Derivations</h2>"
            + "<p>When acceleration 'a' is constant:</p>"
            + "<ul><li><strong>Equation 1:</strong> v = u + at (from a = dv/dt)</li>"
            + "<li><strong>Equation 2:</strong> s = ut + ½at² (integrating v = u + at)</li>"
            + "<li><strong>Equation 3:</strong> v² = u² + 2as (eliminating time t)</li>"
            + "<li><strong>Distance in nth second:</strong> s_n = u + (a/2)(2n - 1)</li></ul>"
            + "<p><strong>Free Fall under Gravity:</strong> Replace 'a' with -g (-9.8 m/s²). Time to reach max height t = u/g. Maximum height reached H_max = u² / (2g). Total time of flight T = 2u/g.</p>");
        noteKinematics.addPage("<h2>3. High-Yield CBSE Pitfalls & Problem Solving</h2>"
            + "<p><strong>Common Mistakes in CBSE Board Exams:</strong></p>"
            + "<ul><li>Average speed is <em>not</em> the magnitude of average velocity when the object reverses direction. Average speed = Total Distance / Total Time.</li>"
            + "<li>When acceleration is opposite to velocity, speed decreases (retardation). An object can have zero velocity with non-zero acceleration (e.g. at the apex of vertical throw where v = 0, a = -g).</li>"
            + "<li>Stopping distance is proportional to velocity squared (s ∝ v²), not proportional to velocity!</li></ul>");
        NOTES.put(noteKinematics.getId(), noteKinematics);

        ChapterNote noteMatrices = new ChapterNote("note-12-math-matrices", 12, "Mathematics", "Matrices", "Matrices & Determinants Comprehensive Review");
        noteMatrices.addPage("<h2>1. Matrix Algebra Fundamentals</h2>"
            + "<p>A matrix of order m × n contains m rows and n columns. Two matrices can be added only if they have identical orders.</p>"
            + "<p><strong>Matrix Multiplication:</strong> AB is defined if number of columns in A equals number of rows in B. In general, <strong>AB ≠ BA</strong> (matrix multiplication is non-commutative).</p>"
            + "<p><strong>Transpose Properties:</strong> (A')' = A, (A + B)' = A' + B', (kA)' = kA', and the reversal law: <strong>(AB)' = B'A'</strong>.</p>"
            + "<p><strong>Symmetric & Skew-Symmetric:</strong> A is symmetric if A' = A. A is skew-symmetric if A' = -A (all diagonal elements of a skew-symmetric matrix are 0).</p>");
        noteMatrices.addPage("<h2>2. Adjoint, Determinants & Matrix Inversion</h2>"
            + "<p>For any square matrix A of order n:</p>"
            + "<ul><li>|A'| = |A|</li>"
            + "<li>|AB| = |A| · |B|</li>"
            + "<li>|kA| = kⁿ · |A| (Critical CBSE 1-mark question)</li>"
            + "<li>A · adj(A) = adj(A) · A = |A| · I</li>"
            + "<li>|adj(A)| = |A|^(n - 1)</li>"
            + "<li>Inverse: A⁻¹ = (1 / |A|) · adj(A), valid if |A| ≠ 0.</li></ul>");
        noteMatrices.addPage("<h2>3. Solution of Linear Systems (AX = B)</h2>"
            + "<p>For a system of linear equations represented by AX = B:</p>"
            + "<ul><li><strong>Case 1 (|A| ≠ 0):</strong> System has a unique consistent solution given by X = A⁻¹B.</li>"
            + "<li><strong>Case 2 (|A| = 0 and (adj A)B ≠ O):</strong> System is inconsistent and has <strong>no solution</strong>.</li>"
            + "<li><strong>Case 3 (|A| = 0 and (adj A)B = O):</strong> System may be consistent with infinitely many solutions or inconsistent with no solution.</li></ul>");
        NOTES.put(noteMatrices.getId(), noteMatrices);
    }

    private static void addSubject(Subject subject) {
        SUBJECTS.add(subject);
        SUBJECT_MAP.put(subject.getId(), subject);
        for (Lesson lesson : subject.getLessons()) {
            LESSON_MAP.put(lesson.getId(), lesson);
        }
    }

    private static void addQuestion(Question q) {
        QUESTION_BANK.add(q);
        QUESTION_MAP.put(q.getId(), q);
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // 1. Static Assets & PWA endpoints
        if (path.equals("/style.css")) {
            serveStaticFile(exchange, "style.css", "text/css; charset=utf-8", 3600);
            return;
        }
        if (path.equals("/sw.js")) {
            serveStaticFile(exchange, "sw.js", "application/javascript; charset=utf-8", 0);
            return;
        }
        if (path.equals("/manifest.json")) {
            serveStaticFile(exchange, "manifest.json", "application/manifest+json; charset=utf-8", 3600);
            return;
        }
        if (path.equals("/logo.svg")) {
            serveStaticFile(exchange, "logo.svg", "image/svg+xml", 86400);
            return;
        }
        if (path.equals("/icon.svg")) {
            serveStaticFile(exchange, "icon.svg", "image/svg+xml", 86400);
            return;
        }
        if (path.equals("/favicon.svg")) {
            serveStaticFile(exchange, "favicon.svg", "image/svg+xml", 86400);
            return;
        }
        if (path.equals("/favicon.ico")) {
            serveStaticFile(exchange, "favicon.ico", "image/x-icon", 86400);
            return;
        }
        if (path.equals("/offline")) {
            sendHtml(exchange, offlinePage());
            return;
        }

        // 2. Public Informational & Legal Endpoints
        boolean isGatewayPath = path == null || path.isEmpty() || path.equals("/") || path.equals("/welcome")
            || path.equals("/index") || path.equals("/index.html") || path.equals("/gateway");
        if (isGatewayPath) {
            User user = getAuthenticatedUser(exchange);
            if (user != null) {
                sendHtml(exchange, dashboardPage(user));
            } else {
                sendHtml(exchange, welcomePage());
            }
            return;
        }

        if (path.equals("/privacy")) {
            User user = getAuthenticatedUser(exchange);
            sendHtml(exchange, privacyPage(user));
            return;
        }
        if (path.equals("/terms")) {
            User user = getAuthenticatedUser(exchange);
            sendHtml(exchange, termsPage(user));
            return;
        }

        // 3. Unauthenticated auth endpoints
        if (path.equals("/login")) {
            User user = getAuthenticatedUser(exchange);
            if (user != null) {
                redirect(exchange, "/dashboard");
                return;
            }
            if ("POST".equalsIgnoreCase(method)) {
                handleLoginPost(exchange);
            } else {
                Map<String, String> query = queryParams(exchange);
                sendHtml(exchange, loginPage(query.get("error")));
            }
            return;
        }

        if (path.equals("/register")) {
            User user = getAuthenticatedUser(exchange);
            if (user != null) {
                redirect(exchange, "/dashboard");
                return;
            }
            if ("POST".equalsIgnoreCase(method)) {
                handleRegisterPost(exchange);
            } else {
                Map<String, String> query = queryParams(exchange);
                sendHtml(exchange, registerPage(query.get("error")));
            }
            return;
        }

        if (path.equals("/logout")) {
            handleLogout(exchange);
            return;
        }

        // 3. Protected Routes Authentication Check
        User user = getAuthenticatedUser(exchange);
        if (user == null) {
            redirect(exchange, "/login");
            return;
        }

        // 4. Authenticated Endpoints
        if (path.equals("/") || path.equals("/home") || path.equals("/dashboard")) {
            sendHtml(exchange, dashboardPage(user));
        } else if (path.equals("/class")) {
            Map<String, String> query = queryParams(exchange);
            int grade = parseInt(query.get("grade"), user.getGradeLevel());
            sendHtml(exchange, classPage(user, grade));
        } else if (path.equals("/lesson")) {
            handleLesson(exchange, user);
        } else if (path.equals("/test-builder")) {
            sendHtml(exchange, testBuilderPage(user));
        } else if (path.equals("/test")) {
            if ("POST".equalsIgnoreCase(method)) {
                handleTestSubmit(exchange, user);
            } else {
                handleTestGenerate(exchange, user);
            }
        } else if (path.equals("/formulas")) {
            Map<String, String> query = queryParams(exchange);
            int grade = parseInt(query.get("grade"), 0);
            String subject = query.getOrDefault("subject", "ALL");
            sendHtml(exchange, formulasPage(user, grade, subject));
        } else if (path.equals("/notes")) {
            Map<String, String> query = queryParams(exchange);
            String noteId = query.getOrDefault("id", "note-11-phys-kinematics");
            int page = parseInt(query.get("page"), 1);
            sendHtml(exchange, notesPage(user, noteId, page));
        } else if (path.equals("/notebook")) {
            sendHtml(exchange, notebookPage(user));
        } else if (path.equals("/api/bookmark") || path.equals("/bookmark")) {
            handleBookmarkApi(exchange, user);
        } else if (path.equals("/profile") || path.equals("/analytics")) {
            sendHtml(exchange, profilePage(user));
        } else if (path.equals("/quiz")) {
            if ("POST".equalsIgnoreCase(method)) {
                handleQuizPost(exchange, user);
            } else {
                Map<String, String> query = queryParams(exchange);
                String subjectId = query.getOrDefault("subject", user.getGradeLevel() == 12 ? "12-MATH" : "11-MATH");
                int qid = parseInt(query.get("qid"), -1);
                sendHtml(exchange, quizPage(user, subjectId, qid, null, -1));
            }
        } else {
            sendHtml(exchange, 404, notFoundPage(user));
        }
    }

    // ---------------- AUTH HANDLERS ----------------

    private static void handleLoginPost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(body);
        String username = form.getOrDefault("username", "").trim();
        String password = form.getOrDefault("password", "");

        User user = USERS.get(username);
        if (user != null && user.getPassword().equals(password)) {
            String token = UUID.randomUUID().toString();
            SESSIONS.put(token, username);
            exchange.getResponseHeaders().add("Set-Cookie", "session_id=" + token + "; Path=/; HttpOnly; SameSite=Lax");
            redirect(exchange, "/dashboard");
        } else {
            redirect(exchange, "/login?error=" + urlEncode("Invalid username or password."));
        }
    }

    private static void handleRegisterPost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(body);
        String username = form.getOrDefault("username", "").trim();
        String password = form.getOrDefault("password", "");
        int gradeLevel = parseInt(form.get("gradeLevel"), 11);

        if (username.isEmpty() || password.isEmpty()) {
            redirect(exchange, "/register?error=" + urlEncode("Username and password are required."));
            return;
        }

        if (USERS.containsKey(username)) {
            redirect(exchange, "/register?error=" + urlEncode("Username already taken. Please choose another."));
            return;
        }

        User newUser = new User(username, password, gradeLevel);
        USERS.put(username, newUser);

        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, username);
        exchange.getResponseHeaders().add("Set-Cookie", "session_id=" + token + "; Path=/; HttpOnly; SameSite=Lax");
        redirect(exchange, "/class?grade=" + gradeLevel);
    }

    private static void handleLogout(HttpExchange exchange) throws IOException {
        String token = getSessionToken(exchange);
        if (token != null) {
            SESSIONS.remove(token);
        }
        exchange.getResponseHeaders().add("Set-Cookie", "session_id=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        redirect(exchange, "/");
    }

    private static User getAuthenticatedUser(HttpExchange exchange) {
        String token = getSessionToken(exchange);
        if (token == null) return null;
        String username = SESSIONS.get(token);
        return username != null ? USERS.get(username) : null;
    }

    private static String getSessionToken(HttpExchange exchange) {
        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
        if (cookieHeaders == null) return null;
        for (String header : cookieHeaders) {
            String[] cookies = header.split(";");
            for (String cookie : cookies) {
                String[] pair = cookie.trim().split("=", 2);
                if (pair.length == 2 && "session_id".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    // ---------------- FEATURE A: CUSTOM TEST GENERATOR & PYQ TEST ----------------

    private static String testBuilderPage(User user) {
        String content = "<div class=\"content-shell\"><header class=\"page-header\">"
            + "<p class=\"eyebrow\">CBSE PCM Prep Engine &middot; Test Generator</p>"
            + "<h1>Custom <em>Test Builder.</em></h1>"
            + "<p class=\"page-intro-text\">Configure a targeted CBSE Previous Year Questions (PYQ) mock test. Select your grade, subject, and chapter. All questions include verified marking schemes.</p>"
            + "</header>"
            + "<div class=\"test-builder-card scroll-reveal\">"
            + "<form action=\"/test\" method=\"GET\">"
            + "<div class=\"form-grid-2\">"
            + "<div class=\"form-group\"><label class=\"form-label\" for=\"test-grade\">Target Grade</label>"
            + "<select id=\"test-grade\" name=\"grade\" class=\"form-select\">"
            + "<option value=\"11\" " + (user.getGradeLevel() == 11 ? "selected" : "") + ">Class 11 (PCM)</option>"
            + "<option value=\"12\" " + (user.getGradeLevel() == 12 ? "selected" : "") + ">Class 12 (PCM)</option>"
            + "</select></div>"
            + "<div class=\"form-group\"><label class=\"form-label\" for=\"test-subject\">Subject</label>"
            + "<select id=\"test-subject\" name=\"subject\" class=\"form-select\">"
            + "<option value=\"ALL\">All Subjects (PCM Combo)</option>"
            + "<option value=\"MATH\">Mathematics</option>"
            + "<option value=\"PHYS\">Physics</option>"
            + "<option value=\"CHEM\">Chemistry</option>"
            + "</select></div>"
            + "</div>"
            + "<div class=\"form-grid-2\">"
            + "<div class=\"form-group\"><label class=\"form-label\" for=\"test-count\">Question Volume</label>"
            + "<select id=\"test-count\" name=\"count\" class=\"form-select\">"
            + "<option value=\"3\">3 Questions (Quick Drill · ~5 mins)</option>"
            + "<option value=\"5\" selected>5 Questions (Standard Practice · ~10 mins)</option>"
            + "<option value=\"8\">8 Questions (Full Mock Section · ~15 mins)</option>"
            + "</select></div>"
            + "<div class=\"form-group\"><label class=\"form-label\" for=\"test-time\">Timed Mode (Countdown)</label>"
            + "<select id=\"test-time\" name=\"time\" class=\"form-select\">"
            + "<option value=\"0\">Untimed / Practice Mode</option>"
            + "<option value=\"300\">5 Minutes (Speed Run)</option>"
            + "<option value=\"600\" selected>10 Minutes (Exam Timing)</option>"
            + "<option value=\"900\">15 Minutes (Standard Section)</option>"
            + "</select></div>"
            + "</div>"
            + "<button type=\"submit\" class=\"submit-btn\">Generate Targeted PYQ Test " + SVG_ARROW + "</button>"
            + "</form>"
            + "</div></div>";

        return layout("Test Builder", "test-builder", user, content);
    }

    private static void handleTestGenerate(HttpExchange exchange, User user) throws IOException {
        Map<String, String> query = queryParams(exchange);
        int grade = parseInt(query.get("grade"), user.getGradeLevel());
        String subject = query.getOrDefault("subject", "ALL").toUpperCase();
        int count = Math.max(1, Math.min(parseInt(query.get("count"), 5), 15));
        int timeSeconds = parseInt(query.get("time"), 0);

        // Filter question bank
        List<Question> matching = QUESTION_BANK.stream()
            .filter(q -> q.getGrade() == grade)
            .filter(q -> subject.equals("ALL") || q.getSubjectId().contains(subject))
            .collect(Collectors.toList());

        if (matching.isEmpty()) {
            matching = new ArrayList<>(QUESTION_BANK);
        }

        Collections.shuffle(matching);
        List<Question> selected = matching.subList(0, Math.min(count, matching.size()));

        StringBuilder questionsHtml = new StringBuilder();
        StringBuilder qIdsHidden = new StringBuilder();

        for (int i = 0; i < selected.size(); i++) {
            Question q = selected.get(i);
            boolean isBookmarked = user.isBookmarked(q.getId());
            if (i > 0) qIdsHidden.append(",");
            qIdsHidden.append(q.getId());

            StringBuilder optionsHtml = new StringBuilder();
            String[] options = q.getOptions();
            for (int opt = 0; opt < options.length; opt++) {
                optionsHtml.append("<label class=\"quiz-option\">")
                    .append("<input type=\"radio\" name=\"q_").append(q.getId()).append("\" value=\"").append(opt).append("\">")
                    .append("<span class=\"quiz-option-text\"><strong>").append((char) ('A' + opt)).append(".</strong> ")
                    .append(escape(options[opt])).append("</span>")
                    .append("</label>");
            }

            // Solution view using native <details> and <summary> tag
            String solutionHtml = "<details class=\"solution-details\"><summary class=\"solution-summary\">"
                + "<span class=\"solution-toggle-label\">" + SVG_LIGHTBULB + "<span>View Step-by-Step CBSE Solution &amp; Marking Scheme</span></span>"
                + "<span class=\"solution-chevron\">▼</span></summary>"
                + "<div class=\"solution-content\">"
                + "<p><strong>Correct Answer:</strong> Option " + (char) ('A' + q.getCorrectAnswerIndex()) + ": " + escape(q.getOptions()[q.getCorrectAnswerIndex()]) + "</p>"
                + "<p style=\"margin-top:0.5rem;\"><strong>Step-by-Step Working:</strong> " + escape(q.getExplanation()) + "</p>"
                + "</div></details>";

            questionsHtml.append("<article class=\"test-question-card scroll-reveal\">")
                .append("<div class=\"question-top-bar\">")
                .append("<div class=\"question-tags\">")
                .append("<span class=\"q-tag q-tag-subject\">").append(escape(q.getSubjectId())).append("</span>")
                .append("<span class=\"q-tag q-tag-year\">").append(escape(q.getYearOrSource())).append("</span>")
                .append("<span class=\"q-tag q-tag-diff\">").append(escape(q.getDifficulty())).append("</span>")
                .append("<span style=\"font-size:0.75rem; color:var(--muted); font-weight:600;\">").append(escape(q.getChapter())).append("</span>")
                .append("</div>")
                .append("<button type=\"button\" class=\"bookmark-btn ").append(isBookmarked ? "bookmarked" : "").append("\" onclick=\"toggleBookmark(this, ").append(q.getId()).append(")\" title=\"").append(isBookmarked ? "Remove Bookmark" : "Save to Notebook").append("\">")
                .append(SVG_NOTEBOOK).append("<span>").append(isBookmarked ? "Bookmarked" : "Bookmark").append("</span></button>")
                .append("</div>")
                .append("<h2 class=\"test-q-text\"><strong>Q").append(i + 1).append(".</strong> ").append(escape(q.getQuestionText())).append("</h2>")
                .append("<div class=\"quiz-options\">").append(optionsHtml).append("</div>")
                .append(solutionHtml)
                .append("</article>");
        }

        // Timer script (Under 15 lines of pure Vanilla JS)
        String timerWidget = "";
        String timerScript = "";
        if (timeSeconds > 0) {
            timerWidget = "<div id=\"timer-box\" class=\"timer-box\" data-seconds=\"" + timeSeconds + "\">"
                + "<span class=\"timer-label\">" + SVG_CLOCK + "<span>Timed Exam Mode &middot; Time Remaining:</span></span>"
                + "<span id=\"timer-display\" class=\"timer-val\">00:00</span>"
                + "</div>";

            timerScript = "<script>\n"
                + "(function(){\n"
                + "  var b=document.getElementById('timer-box'),d=document.getElementById('timer-display');\n"
                + "  if(!b||!d)return;\n"
                + "  var s=parseInt(b.getAttribute('data-seconds'),10);\n"
                + "  var iv=setInterval(function(){\n"
                + "    s--;\n"
                + "    if(s<=0){clearInterval(iv);d.textContent='00:00 (Time Expired)';alert('Exam time has expired! Auto-submitting answers.');var f=document.getElementById('test-form');if(f)f.submit();return;}\n"
                + "    var m=Math.floor(s/60),sec=s%60;\n"
                + "    d.textContent=(m<10?'0':'')+m+':'+(sec<10?'0':'')+sec;\n"
                + "  },1000);\n"
                + "})();\n"
                + "</script>\n";
        }

        String content = "<div class=\"content-shell\"><header class=\"page-header\">"
            + "<nav class=\"breadcrumbs\"><a href=\"/dashboard\">Dashboard</a> <span>&rsaquo;</span> <a href=\"/test-builder\">Test Builder</a> <span>&rsaquo;</span> <span>Class " + grade + " Test</span></nav>"
            + "<p class=\"eyebrow\">CBSE PYQ Assessment &middot; Class " + grade + " Track</p>"
            + "<h1>Class <em>" + grade + "</em> PCM Practice Paper</h1>"
            + "<p class=\"page-intro-text\">Answer the questions below. Solutions can be expanded at any time or verified upon submission.</p>"
            + "</header>"
            + timerWidget
            + "<form id=\"test-form\" action=\"/test\" method=\"POST\">"
            + "<input type=\"hidden\" name=\"qids\" value=\"" + qIdsHidden + "\">"
            + "<input type=\"hidden\" name=\"grade\" value=\"" + grade + "\">"
            + questionsHtml
            + "<button type=\"submit\" class=\"submit-btn\" style=\"padding:1rem 1.5rem; font-size:1rem;\">Submit Test &amp; Calculate Score " + SVG_ARROW + "</button>"
            + "</form>"
            + timerScript
            + "</div>";

        sendHtml(exchange, layout("Class " + grade + " Test", "test-builder", user, content));
    }

    private static void handleTestSubmit(HttpExchange exchange, User user) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(body);
        String qidsRaw = form.getOrDefault("qids", "");
        int grade = parseInt(form.get("grade"), user.getGradeLevel());

        List<Question> submittedQuestions = new ArrayList<>();
        if (!qidsRaw.trim().isEmpty()) {
            for (String part : qidsRaw.split(",")) {
                int qid = parseInt(part.trim(), -1);
                Question q = QUESTION_MAP.get(qid);
                if (q != null) submittedQuestions.add(q);
            }
        }

        int totalQuestions = submittedQuestions.size();
        int correctCount = 0;
        int attemptedCount = 0;

        StringBuilder reviewCards = new StringBuilder();

        for (int i = 0; i < submittedQuestions.size(); i++) {
            Question q = submittedQuestions.get(i);
            int selectedAnswer = parseInt(form.get("q_" + q.getId()), -1);
            boolean isAttempted = selectedAnswer >= 0;
            boolean isCorrect = isAttempted && selectedAnswer == q.getCorrectAnswerIndex();

            if (isAttempted) attemptedCount++;
            if (isCorrect) correctCount++;

            String statusTag = !isAttempted
                ? "<span class=\"q-tag\" style=\"background:var(--paper); color:var(--muted);\">Skipped</span>"
                : (isCorrect
                    ? "<span class=\"q-tag\" style=\"background:var(--green-light); color:var(--green);\">Correct (+1)</span>"
                    : "<span class=\"q-tag\" style=\"background:#fbf0ec; color:#a43823;\">Incorrect (0)</span>");

            reviewCards.append("<article class=\"test-question-card scroll-reveal\">")
                .append("<div class=\"question-top-bar\">")
                .append("<div class=\"question-tags\">")
                .append(statusTag)
                .append("<span class=\"q-tag q-tag-subject\">").append(escape(q.getSubjectId())).append("</span>")
                .append("<span class=\"q-tag q-tag-year\">").append(escape(q.getYearOrSource())).append("</span>")
                .append("<span style=\"font-size:0.75rem; color:var(--muted); font-weight:600;\">").append(escape(q.getChapter())).append("</span>")
                .append("</div>")
                .append("<button type=\"button\" class=\"bookmark-btn ").append(user.isBookmarked(q.getId()) ? "bookmarked" : "").append("\" onclick=\"toggleBookmark(this, ").append(q.getId()).append(")\">")
                .append(SVG_NOTEBOOK).append("<span>").append(user.isBookmarked(q.getId()) ? "Bookmarked" : "Bookmark").append("</span></button>")
                .append("</div>")
                .append("<h2 class=\"test-q-text\"><strong>Q").append(i + 1).append(".</strong> ").append(escape(q.getQuestionText())).append("</h2>")
                .append("<p style=\"font-size:0.86rem; margin-bottom:0.75rem;\"><strong>Your Answer:</strong> ")
                .append(selectedAnswer >= 0 ? (char) ('A' + selectedAnswer) + ". " + escape(q.getOptions()[selectedAnswer]) : "<span style=\"color:var(--muted);\">Not Answered</span>")
                .append("</p>")
                // Pre-open solutions on review!
                .append("<details class=\"solution-details\" open><summary class=\"solution-summary\">")
                .append("<span class=\"solution-toggle-label\">").append(SVG_LIGHTBULB).append("<span>Step-by-Step CBSE Solution &amp; Marking Scheme</span></span><span class=\"solution-chevron\">▼</span></summary>")
                .append("<div class=\"solution-content\">")
                .append("<p><strong>Correct Answer:</strong> Option ").append((char) ('A' + q.getCorrectAnswerIndex())).append(": ").append(escape(q.getOptions()[q.getCorrectAnswerIndex()])).append("</p>")
                .append("<p style=\"margin-top:0.5rem;\"><strong>Explanation:</strong> ").append(escape(q.getExplanation())).append("</p>")
                .append("</div></details>")
                .append("</article>");
        }

        // Record test metrics in user session
        user.recordTestResult(attemptedCount, correctCount);
        int percentage = totalQuestions > 0 ? (int) Math.round(((double) correctCount / totalQuestions) * 100) : 0;

        String content = "<div class=\"content-shell\"><header class=\"page-header\">"
            + "<nav class=\"breadcrumbs\"><a href=\"/dashboard\">Dashboard</a> <span>&rsaquo;</span> <a href=\"/test-builder\">Test Builder</a> <span>&rsaquo;</span> <span>Score Report</span></nav>"
            + "<p class=\"eyebrow\">Test Evaluation &middot; CBSE Marking Standards</p>"
            + "<h1>Score Report: <em>" + correctCount + " / " + totalQuestions + "</em></h1>"
            + "<p class=\"page-intro-text\">Accuracy: " + percentage + "% &middot; Attempted: " + attemptedCount + " / " + totalQuestions + ". Your profile statistics and preparation metrics have been updated.</p>"
            + "</header>"
            + "<section style=\"background:var(--white); border:0.0625rem solid var(--line); border-radius:6px; padding:1.5rem; margin-bottom:2rem; display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:1rem;\">"
            + "<div><span class=\"eyebrow\">CBSE Readiness</span><h2 style=\"font-size:1.4rem; font-family:var(--serif); margin-top:0.2rem;\">" + (percentage >= 70 ? "Excellent Conceptual Grasp!" : "Review Solutions Below") + "</h2></div>"
            + "<div style=\"display:flex; gap:0.75rem;\">"
            + "<a href=\"/test-builder\" class=\"card-btn card-btn-primary\">Build Another Test " + SVG_ARROW + "</a>"
            + "<a href=\"/profile\" class=\"card-btn card-btn-quiz\">View Analytics</a>"
            + "</div>"
            + "</section>"
            + reviewCards
            + "</div>";

        sendHtml(exchange, layout("Score Report", "test-builder", user, content));
    }

    // ---------------- FEATURE B: REVISION HUB (FORMULA CARDS & CHAPTER NOTES) ----------------

    private static String formulasPage(User user, int gradeFilter, String subjectFilter) {
        StringBuilder cardsHtml = new StringBuilder();

        for (Formula f : FORMULAS) {
            if (gradeFilter > 0 && f.getGrade() != gradeFilter) continue;
            if (!"ALL".equalsIgnoreCase(subjectFilter) && !f.getSubject().equalsIgnoreCase(subjectFilter)) continue;

            String colorClass = f.getSubject().contains("Math") ? "blue" : (f.getSubject().contains("Phys") ? "coral" : "sage");

            cardsHtml.append("<article class=\"formula-card scroll-reveal ").append(colorClass).append("\">")
                .append("<div class=\"formula-header\">")
                .append("<span class=\"formula-subject-badge\" style=\"background:var(--paper); border:0.0625rem solid var(--line); color:var(--ink);\">Class ").append(f.getGrade()).append(" &middot; ").append(escape(f.getSubject())).append("</span>")
                .append("<span style=\"font-size:0.72rem; color:var(--muted); font-weight:600;\">").append(escape(f.getChapter())).append("</span>")
                .append("</div>")
                .append("<h3>").append(escape(f.getTitle())).append("</h3>")
                .append("<div class=\"formula-math-box\">").append(escape(f.getFormula()).replace("\n", "<br>")).append("</div>")
                .append("<p class=\"formula-desc\">").append(escape(f.getDescription())).append("</p>")
                .append("<div class=\"formula-tip\"><strong>Exam Tip:</strong> ").append(escape(f.getExamTip())).append("</div>")
                .append("</article>");
        }

        String content = "<div class=\"content-shell\"><header class=\"page-header\">"
            + "<p class=\"eyebrow\">Revision Hub &middot; Zero-Image Pure CSS Masonry</p>"
            + "<h1>Formula <em>Cards.</em></h1>"
            + "<p class=\"page-intro-text\">Essential equations, constants, and exam tips for CBSE Class 11 &amp; 12 PCM. Built with pure Unicode math typography for ultra-low bandwidth consumption.</p>"
            + "</header>"
            + "<div class=\"grade-tabs\">"
            + "<a href=\"/formulas?grade=0&subject=ALL\" class=\"grade-tab " + (gradeFilter == 0 && "ALL".equals(subjectFilter) ? "active" : "") + "\">All Formulas (12)</a>"
            + "<a href=\"/formulas?grade=11&subject=ALL\" class=\"grade-tab " + (gradeFilter == 11 ? "active" : "") + "\">Class 11 PCM</a>"
            + "<a href=\"/formulas?grade=12&subject=ALL\" class=\"grade-tab " + (gradeFilter == 12 ? "active" : "") + "\">Class 12 PCM</a>"
            + "<a href=\"/formulas?grade=" + gradeFilter + "&subject=Mathematics\" class=\"grade-tab " + ("Mathematics".equalsIgnoreCase(subjectFilter) ? "active" : "") + "\">Math</a>"
            + "<a href=\"/formulas?grade=" + gradeFilter + "&subject=Physics\" class=\"grade-tab " + ("Physics".equalsIgnoreCase(subjectFilter) ? "active" : "") + "\">Physics</a>"
            + "<a href=\"/formulas?grade=" + gradeFilter + "&subject=Chemistry\" class=\"grade-tab " + ("Chemistry".equalsIgnoreCase(subjectFilter) ? "active" : "") + "\">Chemistry</a>"
            + "</div>"
            + "<section class=\"formulas-grid\" aria-label=\"Formula Cards Grid\">"
            + cardsHtml
            + "</section>"
            + "</div>";

        return layout("Formula Cards", "formulas", user, content);
    }

    private static String notesPage(User user, String noteId, int requestedPage) {
        ChapterNote note = NOTES.get(noteId);
        if (note == null) {
            note = NOTES.get("note-11-phys-kinematics");
        }

        int totalPages = note.getTotalPages();
        int page = Math.max(1, Math.min(requestedPage, totalPages));
        String body = note.getPageContent(page);

        StringBuilder pageNav = new StringBuilder();
        if (page > 1) {
            pageNav.append("<a href=\"/notes?id=").append(note.getId()).append("&page=").append(page - 1).append("\" class=\"card-btn card-btn-quiz\">&larr; Previous Page</a>");
        } else {
            pageNav.append("<span></span>");
        }

        pageNav.append("<span style=\"font-size:0.82rem; color:var(--muted); font-weight:600;\">Page ").append(page).append(" of ").append(totalPages).append("</span>");

        if (page < totalPages) {
            pageNav.append("<a href=\"/notes?id=").append(note.getId()).append("&page=").append(page + 1).append("\" class=\"card-btn card-btn-primary\">Next Page ").append(SVG_ARROW).append("</a>");
        } else {
            pageNav.append("<a href=\"/test-builder\" class=\"card-btn card-btn-primary\">Practice Chapter PYQs ").append(SVG_ARROW).append("</a>");
        }

        String content = "<div class=\"notes-container\"><header class=\"page-header\">"
            + "<nav class=\"breadcrumbs\"><a href=\"/dashboard\">Dashboard</a> <span>&rsaquo;</span> <span>Chapter Notes</span></nav>"
            + "<p class=\"eyebrow\">Class " + note.getGrade() + " &middot; " + escape(note.getSubject()) + "</p>"
            + "<h1>" + escape(note.getTitle()) + "</h1>"
            + "<div class=\"grade-tabs\">"
            + "<a href=\"/notes?id=note-11-phys-kinematics&page=1\" class=\"grade-tab " + (note.getId().contains("kinematics") ? "active" : "") + "\">Class 11 Physics: Kinematics</a>"
            + "<a href=\"/notes?id=note-12-math-matrices&page=1\" class=\"grade-tab " + (note.getId().contains("matrices") ? "active" : "") + "\">Class 12 Math: Matrices</a>"
            + "</div>"
            + "</header>"
            + "<article class=\"notes-body\">" + body + "</article>"
            + "<div class=\"pagination-nav\">" + pageNav + "</div>"
            + "</div>";

        return layout(note.getTitle(), "notes", user, content);
    }

    // ---------------- FEATURE C: PERSONALIZED NOTEBOOK (BOOKMARKS) ----------------

    private static String notebookPage(User user) {
        StringBuilder cards = new StringBuilder();
        int count = 0;

        for (int qid : user.getBookmarkedQuestions()) {
            Question q = QUESTION_MAP.get(qid);
            if (q == null) continue;
            count++;

            StringBuilder optionsHtml = new StringBuilder();
            String[] options = q.getOptions();
            for (int opt = 0; opt < options.length; opt++) {
                optionsHtml.append("<div style=\"padding:0.4rem 0.6rem; font-size:0.86rem; color:var(--ink);\">")
                    .append("<strong>").append((char) ('A' + opt)).append(".</strong> ").append(escape(options[opt]))
                    .append("</div>");
            }

            cards.append("<article class=\"test-question-card scroll-reveal\">")
                .append("<div class=\"question-top-bar\">")
                .append("<div class=\"question-tags\">")
                .append("<span class=\"q-tag q-tag-subject\">").append(escape(q.getSubjectId())).append("</span>")
                .append("<span class=\"q-tag q-tag-year\">").append(escape(q.getYearOrSource())).append("</span>")
                .append("<span style=\"font-size:0.75rem; color:var(--muted); font-weight:600;\">").append(escape(q.getChapter())).append("</span>")
                .append("</div>")
                .append("<button type=\"button\" class=\"bookmark-btn bookmarked\" onclick=\"toggleBookmark(this, ").append(q.getId()).append(")\">")
                .append(SVG_NOTEBOOK).append("<span>Remove</span></button>")
                .append("</div>")
                .append("<h2 class=\"test-q-text\"><strong>Q").append(count).append(".</strong> ").append(escape(q.getQuestionText())).append("</h2>")
                .append("<div style=\"background:var(--paper); border:0.0625rem solid var(--line); border-radius:4px; padding:0.6rem; margin-bottom:1rem;\">")
                .append(optionsHtml).append("</div>")
                .append("<details class=\"solution-details\"><summary class=\"solution-summary\">")
                .append("<span class=\"solution-toggle-label\">").append(SVG_LIGHTBULB).append("<span>View Step-by-Step CBSE Solution</span></span><span class=\"solution-chevron\">▼</span></summary>")
                .append("<div class=\"solution-content\">")
                .append("<p><strong>Correct Answer:</strong> Option ").append((char) ('A' + q.getCorrectAnswerIndex())).append(": ").append(escape(q.getOptions()[q.getCorrectAnswerIndex()])).append("</p>")
                .append("<p style=\"margin-top:0.5rem;\"><strong>Explanation:</strong> ").append(escape(q.getExplanation())).append("</p>")
                .append("</div></details>")
                .append("</article>");
        }

        String bodyHtml = count > 0
            ? cards.toString()
            : "<div class=\"notebook-empty\"><div class=\"notebook-empty-icon\">" + SVG_NOTEBOOK + "</div><h2>Your Notebook is Empty</h2>"
            + "<p style=\"color:var(--muted); max-width:26rem; margin:0.5rem auto 1.5rem;\">Click the bookmark icon on any PYQ or test question to save it here for targeted revision before exams.</p>"
            + "<a href=\"/test-builder\" class=\"card-btn card-btn-primary\">Explore Question Bank " + SVG_ARROW + "</a></div>";

        String content = "<div class=\"content-shell\"><header class=\"page-header\">"
            + "<p class=\"eyebrow\">Personalized Study Notebook &middot; Bookmarks</p>"
            + "<h1>Saved <em>Questions.</em></h1>"
            + "<p class=\"page-intro-text\">Review your bookmarked CBSE PYQs, tricky conceptual problems, and verified solutions in one low-bandwidth view.</p>"
            + "</header>"
            + bodyHtml
            + "</div>";

        return layout("Personal Notebook", "notebook", user, content);
    }

    private static void handleBookmarkApi(HttpExchange exchange, User user) throws IOException {
        Map<String, String> params = queryParams(exchange);
        if (params.isEmpty() && "POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            params = parseForm(body);
        }
        int qid = parseInt(params.get("id"), -1);
        boolean nowBookmarked = false;
        if (qid > 0 && QUESTION_MAP.containsKey(qid)) {
            nowBookmarked = user.toggleBookmark(qid);
        }

        String json = "{\"success\":true,\"questionId\":" + qid + ",\"bookmarked\":" + nowBookmarked + "}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    // ---------------- FEATURE C: PREPARATION TRACKER (NATIVE HTML5 <progress>) ----------------

    private static String profilePage(User user) {
        int readiness = user.getOverallReadiness();
        int accuracy = user.getAccuracyPercent();
        int tests = user.getTestsTaken();
        int testQuestions = user.getTestQuestionsAttempted();
        int bookmarksCount = user.getBookmarkedQuestions().size();
        int lessonsCount = user.getCompletedLessons().size();

        // Calculate subject specific mastery proxies
        int mathProgress = Math.min(100, (lessonsCount * 12) + (user.getTotalScore() * 8));
        int physProgress = Math.min(100, (lessonsCount * 10) + (user.getTotalScore() * 7));
        int chemProgress = Math.min(100, (lessonsCount * 11) + (user.getTotalScore() * 9));

        String content = "<div class=\"profile-container\"><header class=\"page-header\">"
            + "<p class=\"eyebrow\">Student Dashboard &middot; SDG 4 Analytics</p>"
            + "<h1>Preparation <em>Tracker.</em></h1>"
            + "<p class=\"page-intro-text\">Engineered with native HTML5 &lt;progress&gt; elements and zero charting bloat. High-contrast indicators provide actionable preparation metrics.</p>"
            + "</header>"
            + "<div class=\"profile-hero\">"
            + "<div class=\"profile-avatar-large\">" + SVG_USER + "</div>"
            + "<div class=\"profile-details\">"
            + "<h2>" + escape(user.getUsername()) + "</h2>"
            + "<span class=\"profile-grade-pill\">Enrolled: Class " + user.getGradeLevel() + " PCM</span>"
            + "</div>"
            + "</div>"
            // Native HTML5 <progress> Trackers
            + "<section class=\"tracker-grid\" aria-label=\"Preparation Progress Bars\">"
            + "<div class=\"tracker-card scroll-reveal\"><div class=\"tracker-header\"><span class=\"tracker-title\">Overall CBSE Readiness</span><span class=\"tracker-percent\">" + readiness + "%</span></div>"
            + "<progress max=\"100\" value=\"" + readiness + "\"></progress></div>"
            + "<div class=\"tracker-card scroll-reveal\"><div class=\"tracker-header\"><span class=\"tracker-title\">PYQ Test Accuracy</span><span class=\"tracker-percent\">" + accuracy + "%</span></div>"
            + "<progress max=\"100\" value=\"" + accuracy + "\"></progress></div>"
            + "<div class=\"tracker-card scroll-reveal\"><div class=\"tracker-header\"><span class=\"tracker-title\">Mathematics Mastery</span><span class=\"tracker-percent\">" + mathProgress + "%</span></div>"
            + "<progress max=\"100\" value=\"" + mathProgress + "\"></progress></div>"
            + "<div class=\"tracker-card scroll-reveal\"><div class=\"tracker-header\"><span class=\"tracker-title\">Physics Mastery</span><span class=\"tracker-percent\">" + physProgress + "%</span></div>"
            + "<progress max=\"100\" value=\"" + physProgress + "\"></progress></div>"
            + "<div class=\"tracker-card scroll-reveal\"><div class=\"tracker-header\"><span class=\"tracker-title\">Chemistry Mastery</span><span class=\"tracker-percent\">" + chemProgress + "%</span></div>"
            + "<progress max=\"100\" value=\"" + chemProgress + "\"></progress></div>"
            + "<div class=\"tracker-card scroll-reveal\"><div class=\"tracker-header\"><span class=\"tracker-title\">Saved Notebook Problems</span><span class=\"tracker-percent\">" + bookmarksCount + " items</span></div>"
            + "<progress max=\"24\" value=\"" + bookmarksCount + "\"></progress></div>"
            + "</section>"
            // Numerical Stats Grid
            + "<section class=\"stats-grid\" aria-label=\"Learning Statistics\">"
            + "<div class=\"stat-card scroll-reveal\"><div class=\"stat-label\">Mock Tests Taken</div><div class=\"stat-value\">" + tests + " <span>tests</span></div></div>"
            + "<div class=\"stat-card scroll-reveal\"><div class=\"stat-label\">Questions Solved</div><div class=\"stat-value\">" + testQuestions + " <span>PYQs</span></div></div>"
            + "<div class=\"stat-card scroll-reveal\"><div class=\"stat-label\">Points Earned</div><div class=\"stat-value\">" + user.getTotalScore() + " <span>pts</span></div></div>"
            + "<div class=\"stat-card scroll-reveal\"><div class=\"stat-label\">Modules Completed</div><div class=\"stat-value\">" + lessonsCount + " <span>units</span></div></div>"
            + "</section>"
            + "<div class=\"profile-actions\">"
            + "<a href=\"/test-builder\" class=\"card-btn card-btn-primary\" style=\"padding:0.75rem 1.4rem;\">Take Next Custom Test " + SVG_ARROW + "</a>"
            + "<a href=\"/notebook\" class=\"card-btn card-btn-quiz\" style=\"padding:0.75rem 1.4rem;\">Review Saved Notebook (" + bookmarksCount + ")</a>"
            + "</div>"
            + "</div>";

        return layout("My Analytics", "profile", user, content);
    }

    // ---------------- OTHER PAGES (DASHBOARD, CLASS, LESSON, QUIZ, OFFLINE) ----------------

    private static String dashboardPage(User user) {
        StringBuilder cardsHtml = new StringBuilder();
        for (Subject subject : SUBJECTS) {
            cardsHtml.append(renderSubjectCard(subject));
        }

        String userGradeLink = "/class?grade=" + user.getGradeLevel();

        String content = "<div class=\"content-shell\"><header class=\"page-header\">"
            + "<p class=\"eyebrow\">SDG 4 &middot; Quality STEM Education</p>"
            + "<h1>Welcome back, <em>" + escape(user.getUsername()) + ".</em></h1>"
            + "<p class=\"page-intro-text\">Ultra-low bandwidth study portal engineered for rapid learning over 2G/3G connections. Select a class track or continue where you left off.</p>"
            + "</header>"
            + "<div class=\"grade-tabs\">"
            + "<a href=\"/dashboard\" class=\"grade-tab active\">" + SVG_HOME + "<span>All Subjects</span></a>"
            + "<a href=\"/class?grade=11\" class=\"grade-tab\">" + SVG_BOOK + "<span>Class 11</span></a>"
            + "<a href=\"/class?grade=12\" class=\"grade-tab\">" + SVG_BOOK + "<span>Class 12</span></a>"
            + "<a href=\"/test-builder\" class=\"grade-tab\">" + SVG_TEST + "<span>Test Generator</span></a>"
            + "<a href=\"/formulas\" class=\"grade-tab\">" + SVG_FORMULA + "<span>Revision Formulas</span></a>"
            + "</div>"
            + "<section class=\"subjects-grid\" aria-label=\"Curriculum Subjects\">"
            + cardsHtml
            + "</section>"
            + "<section class=\"scroll-reveal\" style=\"background:var(--white); border:0.0625rem solid var(--line); border-radius:6px; padding:1.75rem 2rem; display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:1.25rem;\">"
            + "<div><p class=\"eyebrow\">Recommended Track</p><h2 style=\"font-family:var(--serif); font-size:1.45rem; margin-bottom:0.25rem;\">Class " + user.getGradeLevel() + " Curriculum</h2>"
            + "<p style=\"color:var(--muted); font-size:0.86rem;\">Tailored for your enrolled grade level &middot; 3 STEM subjects &middot; 6 foundational lessons</p></div>"
            + "<a href=\"" + userGradeLink + "\" class=\"card-btn card-btn-primary\" style=\"padding:0.75rem 1.4rem;\">Open Class " + user.getGradeLevel() + " " + SVG_ARROW + "</a>"
            + "</section>"
            + "</div>";

        return layout("Dashboard", "home", user, content);
    }

    private static String classPage(User user, int grade) {
        if (grade != 11 && grade != 12) grade = 11;
        StringBuilder cardsHtml = new StringBuilder();
        for (Subject subject : SUBJECTS) {
            if (subject.getGrade() == grade) {
                cardsHtml.append(renderSubjectCard(subject));
            }
        }

        String content = "<div class=\"content-shell\"><header class=\"page-header\">"
            + "<p class=\"eyebrow\">Curriculum Track &middot; Higher Secondary</p>"
            + "<h1>Class <em>" + grade + "</em> Curriculum</h1>"
            + "<p class=\"page-intro-text\">Structured core STEM curriculum with Mathematics, Physics, and Chemistry. Every lesson is optimized for lightweight text reading and instant conceptual review.</p>"
            + "</header>"
            + "<div class=\"grade-tabs\">"
            + "<a href=\"/class?grade=11\" class=\"grade-tab " + (grade == 11 ? "active" : "") + "\">" + SVG_BOOK + "<span>Class 11</span></a>"
            + "<a href=\"/class?grade=12\" class=\"grade-tab " + (grade == 12 ? "active" : "") + "\">" + SVG_BOOK + "<span>Class 12</span></a>"
            + "<a href=\"/test-builder?grade=" + grade + "\" class=\"grade-tab\">" + SVG_TEST + "<span>Class " + grade + " PYQs</span></a>"
            + "<a href=\"/formulas?grade=" + grade + "\" class=\"grade-tab\">" + SVG_FORMULA + "<span>Class " + grade + " Formulas</span></a>"
            + "</div>"
            + "<section class=\"subjects-grid\" aria-label=\"Class " + grade + " Subjects\">"
            + cardsHtml
            + "</section>"
            + "</div>";

        return layout("Class " + grade + " Track", "class" + grade, user, content);
    }

    private static String renderSubjectCard(Subject subject) {
        String iconSvg = getSubjectIconSvg(subject.getName());
        StringBuilder lessonsList = new StringBuilder();
        for (Lesson lesson : subject.getLessons()) {
            lessonsList.append("<li class=\"lesson-item-mini\">")
                .append("<a href=\"/lesson?id=").append(lesson.getId()).append("\" class=\"lesson-title-mini\">")
                .append(escape(lesson.getTitle())).append("</a>")
                .append("<span class=\"lesson-time-mini\">").append(escape(lesson.getReadTime())).append("</span>")
                .append("</li>");
        }

        return "<article class=\"subject-card scroll-reveal " + subject.getColor() + "\">"
            + "<div class=\"card-top\">"
            + "<div class=\"subject-icon-box\">" + iconSvg + "</div>"
            + "<span class=\"grade-badge-tag\">Class " + subject.getGrade() + "</span>"
            + "</div>"
            + "<h2>" + escape(subject.getName()) + "</h2>"
            + "<p>" + escape(subject.getDescription()) + "</p>"
            + "<ul class=\"lesson-list-mini\">" + lessonsList + "</ul>"
            + "<div class=\"card-actions\">"
            + "<a href=\"/lesson?id=" + subject.getLessons().get(0).getId() + "\" class=\"card-btn card-btn-primary\">Start Lesson " + SVG_ARROW + "</a>"
            + "<a href=\"/test-builder\" class=\"card-btn card-btn-quiz\">Mock Test</a>"
            + "</div>"
            + "</article>";
    }

    private static void handleLesson(HttpExchange exchange, User user) throws IOException {
        Map<String, String> query = queryParams(exchange);
        String lessonId = query.get("id");
        if (lessonId == null || !LESSON_MAP.containsKey(lessonId)) {
            lessonId = user.getGradeLevel() == 12 ? "12-MATH-1" : "11-MATH-1";
        }

        Lesson lesson = LESSON_MAP.get(lessonId);
        user.markLessonCompleted(lessonId);
        Subject subject = SUBJECT_MAP.get(lesson.getSubjectId());

        String[] paragraphs = lesson.getContentChunk().split("\n\n");
        StringBuilder bodyHtml = new StringBuilder();
        for (String p : paragraphs) {
            bodyHtml.append("<p>").append(escape(p).replace("\n", "<br>")).append("</p>");
        }

        List<Lesson> lessons = subject.getLessons();
        int currentIndex = lessons.indexOf(lesson);
        String prevLink = currentIndex > 0
            ? "<a href=\"/lesson?id=" + lessons.get(currentIndex - 1).getId() + "\" class=\"card-btn card-btn-quiz\">&larr; Previous Lesson</a>"
            : "<a href=\"/class?grade=" + subject.getGrade() + "\" class=\"card-btn card-btn-quiz\">&larr; Back to Class " + subject.getGrade() + "</a>";

        String nextLink = currentIndex < lessons.size() - 1
            ? "<a href=\"/lesson?id=" + lessons.get(currentIndex + 1).getId() + "\" class=\"card-btn card-btn-primary\">Next Lesson " + SVG_ARROW + "</a>"
            : "<a href=\"/test-builder\" class=\"card-btn card-btn-primary\">Take Chapter PYQ Test " + SVG_ARROW + "</a>";

        int progressPercent = (int) (((currentIndex + 1) / (double) lessons.size()) * 100);

        String content = "<main class=\"reader-container\"><nav class=\"breadcrumbs\" aria-label=\"Breadcrumb\">"
            + "<a href=\"/dashboard\">Dashboard</a> <span>&rsaquo;</span> "
            + "<a href=\"/class?grade=" + subject.getGrade() + "\">Class " + subject.getGrade() + "</a> <span>&rsaquo;</span> "
            + "<span>" + escape(subject.getName()) + "</span>"
            + "</nav>"
            + "<div class=\"reader-progress\" aria-label=\"Lesson Progress\"><div class=\"reader-progress-bar\" style=\"width:" + progressPercent + "%\"></div></div>"
            + "<article class=\"lesson-article scroll-reveal\">"
            + "<p class=\"eyebrow\">Class " + subject.getGrade() + " &middot; " + escape(subject.getName()) + "</p>"
            + "<h1>" + escape(lesson.getTitle()) + "</h1>"
            + "<div class=\"lesson-meta\">"
            + "<span>Estimated read: <strong>" + escape(lesson.getReadTime()) + "</strong></span>"
            + "<span>Part <strong>0" + (currentIndex + 1) + " of 0" + lessons.size() + "</strong></span>"
            + "</div>"
            + "<div class=\"lesson-body\">" + bodyHtml + "</div>"
            + "<aside class=\"lesson-note\">"
            + "<strong>SDG 4 Key Learning Principle</strong>"
            + "<p>Focus on connecting foundational principles to practical problem solving. Solid comprehension of these definitions will unlock higher secondary mastery.</p>"
            + "</aside>"
            + "<div class=\"lesson-nav-bar\">" + prevLink + nextLink + "</div>"
            + "</article></main>";

        sendHtml(exchange, layout(lesson.getTitle(), "class" + subject.getGrade(), user, content));
    }

    private static void handleQuizPost(HttpExchange exchange, User user) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(body);
        String subjectId = form.getOrDefault("subject", "11-MATH");
        int qid = parseInt(form.get("qid"), -1);
        int answer = parseInt(form.get("answer"), -1);

        Question question = qid > 0 ? QUESTION_MAP.get(qid) : null;
        if (question == null) {
            question = QUESTION_BANK.stream().filter(q -> q.getSubjectId().equals(subjectId)).findFirst().orElse(QUESTION_BANK.get(0));
        }
        boolean isCorrect = answer == question.getCorrectAnswerIndex();

        user.recordQuizResult(isCorrect ? 1 : 0);
        sendHtml(exchange, quizPage(user, subjectId, question.getId(), isCorrect ? "correct" : "incorrect", answer));
    }

    private static String quizPage(User user, String subjectId, int qid, String result, int selectedAnswer) {
        Question question = qid > 0 ? QUESTION_MAP.get(qid) : null;
        if (question == null) {
            question = QUESTION_BANK.stream().filter(q -> q.getSubjectId().equals(subjectId)).findFirst().orElse(QUESTION_BANK.get(0));
        }
        Subject subject = SUBJECT_MAP.get(question.getSubjectId());
        String subjName = subject != null ? subject.getName() : "PCM";

        String content;
        if (result != null) {
            boolean isCorrect = "correct".equals(result);
            content = "<main class=\"quiz-container\"><section class=\"quiz-card result-card scroll-reveal\">"
                + "<div class=\"result-icon " + (isCorrect ? "result-correct" : "result-incorrect") + "\">"
                + (isCorrect ? SVG_CHECK : SVG_ALERT) + "</div>"
                + "<p class=\"eyebrow\">Quiz Evaluation &middot; " + escape(subjName) + "</p>"
                + "<h1>" + (isCorrect ? "Conceptual Mastery Verified!" : "Concept Needs Review") + "</h1>"
                + "<div class=\"result-score\">" + (isCorrect ? "+1 Pt" : "+0 Pts") + "</div>"
                + "<p class=\"result-explanation\">" + escape(question.getExplanation()) + "</p>"
                + "<div style=\"display:flex; justify-content:center; gap:1rem; flex-wrap:wrap;\">"
                + "<a href=\"/quiz?subject=" + question.getSubjectId() + "&qid=" + question.getId() + "\" class=\"card-btn card-btn-quiz\">Retry Question</a>"
                + "<a href=\"/profile\" class=\"card-btn card-btn-primary\">View Profile Progress " + SVG_ARROW + "</a>"
                + "</div>"
                + "</section></main>";
        } else {
            StringBuilder optionsHtml = new StringBuilder();
            String[] options = question.getOptions();
            for (int i = 0; i < options.length; i++) {
                optionsHtml.append("<label class=\"quiz-option\">")
                    .append("<input type=\"radio\" name=\"answer\" value=\"").append(i).append("\" required>")
                    .append("<span class=\"quiz-option-text\"><strong>").append((char) ('A' + i)).append(".</strong> ")
                    .append(escape(options[i])).append("</span>")
                    .append("</label>");
            }

            content = "<main class=\"quiz-container\"><section class=\"quiz-card scroll-reveal\">"
                + "<nav class=\"breadcrumbs\"><a href=\"/dashboard\">&larr; Back to Dashboard</a> <span>&rsaquo;</span> <span>" + escape(subjName) + " Check-in</span></nav>"
                + "<p class=\"eyebrow\">Knowledge Check &middot; " + escape(question.getChapter()) + "</p>"
                + "<h2 class=\"quiz-question-title\">" + escape(question.getQuestionText()) + "</h2>"
                + "<form action=\"/quiz\" method=\"POST\">"
                + "<input type=\"hidden\" name=\"subject\" value=\"" + escape(question.getSubjectId()) + "\">"
                + "<input type=\"hidden\" name=\"qid\" value=\"" + question.getId() + "\">"
                + "<div class=\"quiz-options\">" + optionsHtml + "</div>"
                + "<button type=\"submit\" class=\"submit-btn\">Submit &amp; Verify Answer " + SVG_ARROW + "</button>"
                + "</form>"
                + "</section></main>";
        }

        return layout("Quiz · " + subjName, "dashboard", user, content);
    }

    // ---------------- ZERO-SCROLL WELCOME LANDING GATEWAY (< 15KB) ----------------

    private static String welcomePage() {
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
            + "<meta name=\"theme-color\" content=\"#0b0f33\">"
            + "<meta name=\"description\" content=\"Welcome to NanoScholar: High-Yield Learning. Low-Bandwidth Delivery. Ultra-light CBSE Class 11 &amp; 12 preparation.\">"
            + "<link rel=\"canonical\" href=\"https://nanoscholar.edu\">"
            + "<link rel=\"icon\" type=\"image/svg+xml\" href=\"/favicon.svg\">"
            + "<link rel=\"alternate icon\" href=\"/favicon.ico\">"
            + "<title>Welcome to NanoScholar &middot; High-Yield Learning</title>"
            + "<style>"
            + "* { box-sizing: border-box; margin: 0; padding: 0; }"
            + "html, body { width: 100%; height: 100vh; overflow: hidden; background: #0b0f33; color: #ffffff; font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased; }"
            + ".welcome-container { width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; padding: 1.5rem; text-align: center; }"
            + ".welcome-card { width: 100%; max-width: 32rem; display: flex; flex-direction: column; align-items: center; }"
            + ".welcome-logo-box { width: 3.5rem; height: 3.5rem; display: flex; align-items: center; justify-content: center; margin-bottom: 1.25rem; color: #48a7e3; }"
            + ".welcome-headline { font-size: clamp(1.75rem, 5vw, 2.35rem); font-weight: 700; line-height: 1.15; letter-spacing: -0.02em; color: #ffffff; margin-bottom: 0.65rem; }"
            + ".welcome-slogan { font-size: clamp(0.78rem, 2vw, 0.88rem); font-weight: 700; color: #48a7e3; letter-spacing: 0.09em; text-transform: uppercase; margin-bottom: 0.95rem; }"
            + ".welcome-value-prop { font-size: clamp(0.88rem, 2.2vw, 0.98rem); line-height: 1.55; color: #95a6c4; margin-bottom: 1.75rem; max-width: 27rem; }"
            + ".welcome-actions { display: flex; flex-direction: row; gap: 0.75rem; justify-content: center; flex-wrap: wrap; margin-bottom: 0.5rem; width: 100%; }"
            + ".welcome-btn { display: inline-block; background: #0b0f33; color: #ffffff; border: 1.5px solid #ffffff; padding: 0.85rem 1.85rem; font-size: 0.92rem; font-weight: 650; border-radius: 4px; text-decoration: none; will-change: transform; transition: all 0.15s ease-out; }"
            + ".welcome-btn:hover { background: #ffffff; color: #0b0f33; border-color: #ffffff; transform: scale(1.03); }"
            + ".welcome-btn-secondary { background: transparent; color: #48a7e3; border: 1.5px solid #48a7e3; }"
            + ".welcome-btn-secondary:hover { background: #48a7e3; color: #0b0f33; border-color: #48a7e3; transform: scale(1.03); }"
            + ".welcome-secondary { margin-top: 1rem; font-size: 0.8rem; color: #95a6c4; }"
            + ".welcome-secondary a { color: #48a7e3; text-decoration: underline; text-underline-offset: 3px; font-weight: 600; }"
            + ".welcome-secondary a:hover { color: #ffffff; }"
            + ".welcome-secondary code { background: #162044; color: #e2e8f0; padding: 0.15rem 0.45rem; border-radius: 3px; font-size: 0.82rem; }"
            + ".welcome-legal { margin-top: 1.5rem; font-size: 0.68rem; color: #56647e; }"
            + ".welcome-legal a { color: #56647e; text-decoration: underline; text-underline-offset: 2px; }"
            + ".welcome-legal a:hover { color: #95a6c4; }"
            + "</style>"
            + "</head><body>"
            + "<main class=\"welcome-container\">"
            + "<div class=\"welcome-card\">"
            + "<div class=\"welcome-logo-box\">"
            + "<svg width=\"44\" height=\"44\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M4 19.5A2.5 2.5 0 0 1 6.5 17H20\"></path><path d=\"M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z\"></path><line x1=\"9\" y1=\"7\" x2=\"15\" y2=\"7\"></line><line x1=\"9\" y1=\"11\" x2=\"13\" y2=\"11\"></line></svg>"
            + "</div>"
            + "<h1 class=\"welcome-headline\">Welcome to NanoScholar.</h1>"
            + "<p class=\"welcome-slogan\">High-Yield Learning. Low-Bandwidth Delivery.</p>"
            + "<p class=\"welcome-value-prop\">Tension-free CBSE Class 11 and 12 STEM preparation engineered to load reliably on low-bandwidth connections without data strain.</p>"
            + "<div class=\"welcome-actions\">"
            + "<a href=\"/login\" class=\"welcome-btn\">Enter Portal &rarr;</a>"
            + "<a href=\"/register\" class=\"welcome-btn welcome-btn-secondary\">New Student Registration</a>"
            + "</div>"
            + "<p class=\"welcome-secondary\">Default student login: <code>student</code> / <code>password123</code></p>"
            + "<footer class=\"welcome-legal\"><a href=\"/privacy\">Privacy Policy</a> &middot; <a href=\"/terms\">Terms &amp; Conditions</a></footer>"
            + "</div>"
            + "</main>"
            + "</body></html>";
    }

    private static String loginPage(String error) {
        String errorHtml = error != null && !error.isEmpty()
            ? "<div class=\"auth-error\">" + escape(error) + "</div>"
            : "";

        String content = "<main class=\"auth-wrapper\"><div class=\"auth-card\"><header class=\"auth-header\">"
            + "<div class=\"auth-logo\"><img src=\"/logo.svg\" alt=\"NanoScholar\" class=\"brand-logo-img\"><span>NanoScholar</span></div>"
            + "<p class=\"eyebrow\">SDG 4 · Quality Education</p>"
            + "<h1>Enter Portal</h1><p>Low-bandwidth educational sanctuary for Class 11 &amp; 12.</p></header>"
            + errorHtml
            + "<form action=\"/login\" method=\"POST\">"
            + "<div class=\"form-group\"><label class=\"form-label\" for=\"username\">Username</label>"
            + "<input id=\"username\" name=\"username\" type=\"text\" class=\"form-input\" required autocomplete=\"username\" placeholder=\"e.g. student\" autofocus></div>"
            + "<div class=\"form-group\"><label class=\"form-label\" for=\"password\">Password</label>"
            + "<input id=\"password\" name=\"password\" type=\"password\" class=\"form-input\" required autocomplete=\"current-password\" placeholder=\"••••••••\"></div>"
            + "<button type=\"submit\" class=\"submit-btn\">Sign In to Classroom &rarr;</button>"
            + "</form>"
            + "<p class=\"auth-switch\">New student? <a href=\"/register\">Register an account</a></p>"
            + "<p class=\"auth-switch\" style=\"margin-top:0.4rem;\"><a href=\"/\" class=\"auth-back-link\">&larr; Return to Welcome Gateway</a></p>"
            + "<footer class=\"auth-footer\">Demo Credentials: <strong>student</strong> / <strong>password123</strong>"
            + "<div class=\"auth-legal\"><a href=\"/privacy\">Privacy Policy</a> &middot; <a href=\"/terms\">Terms &amp; Conditions</a></div></footer>"
            + "</div></main>";

        return layout("Enter Portal", "login", null, content);
    }

    private static String registerPage(String error) {
        String errorHtml = error != null && !error.isEmpty()
            ? "<div class=\"auth-error\">" + escape(error) + "</div>"
            : "";

        String content = "<main class=\"auth-wrapper\"><div class=\"auth-card\"><header class=\"auth-header\">"
            + "<div class=\"auth-logo\"><img src=\"/logo.svg\" alt=\"NanoScholar\" class=\"brand-logo-img\"><span>NanoScholar</span></div>"
            + "<p class=\"eyebrow\">SDG 4 · Universal Access</p>"
            + "<h1>Student Sign Up</h1><p>Create your lightweight profile to track your progress.</p></header>"
            + errorHtml
            + "<form action=\"/register\" method=\"POST\">"
            + "<div class=\"form-group\"><label class=\"form-label\" for=\"reg-username\">Choose a Username</label>"
            + "<input id=\"reg-username\" name=\"username\" type=\"text\" class=\"form-input\" required autocomplete=\"username\" placeholder=\"e.g. rahul_stem\" autofocus></div>"
            + "<div class=\"form-group\"><label class=\"form-label\" for=\"reg-password\">Create Password</label>"
            + "<input id=\"reg-password\" name=\"password\" type=\"password\" class=\"form-input\" required autocomplete=\"new-password\" placeholder=\"••••••••\"></div>"
            + "<div class=\"form-group\"><label class=\"form-label\" for=\"gradeLevel\">Select Your Grade</label>"
            + "<select id=\"gradeLevel\" name=\"gradeLevel\" class=\"form-select\">"
            + "<option value=\"11\">Class 11 (Higher Secondary: Year 1)</option>"
            + "<option value=\"12\">Class 12 (Higher Secondary: Year 2)</option>"
            + "</select></div>"
            + "<button type=\"submit\" class=\"submit-btn\">Create Free Account &rarr;</button>"
            + "</form>"
            + "<p class=\"auth-switch\">Already registered? <a href=\"/login\">Enter Portal (Sign in)</a></p>"
            + "<p class=\"auth-switch\" style=\"margin-top:0.4rem;\"><a href=\"/\" class=\"auth-back-link\">&larr; Return to Welcome Gateway</a></p>"
            + "<footer class=\"auth-footer\">Optimized for 2G / 3G mobile connections &middot; Under 50KB total payload"
            + "<div class=\"auth-legal\"><a href=\"/privacy\">Privacy Policy</a> &middot; <a href=\"/terms\">Terms &amp; Conditions</a></div></footer>"
            + "</div></main>";

        return layout("Register Student", "register", null, content);
    }

    private static String offlinePage() {
        String content = "<div class=\"content-shell\" style=\"text-align:center; padding-top:5rem;\">"
            + "<div style=\"display:flex; justify-content:center; margin-bottom:1rem; color:var(--primary);\">" + SVG_SIGNAL + "</div>"
            + "<p class=\"eyebrow\">Offline Mode Active &middot; Service Worker</p>"
            + "<h1>Connection <em>Interrupted.</em></h1>"
            + "<p style=\"color:var(--muted); max-width:30rem; margin:1rem auto 2rem;\">You are currently offline or on a weak 2G link. Your cached formula cards, notes, and downloaded past papers remain fully accessible!</p>"
            + "<div style=\"display:flex; justify-content:center; gap:1rem; flex-wrap:wrap;\">"
            + "<a href=\"/formulas\" class=\"card-btn card-btn-primary\">Open Cached Formula Cards " + SVG_ARROW + "</a>"
            + "<a href=\"/dashboard\" class=\"card-btn card-btn-quiz\">Return to Cached Home</a>"
            + "</div>"
            + "</div>";
        return layout("Offline Mode", "", null, content);
    }

    private static String notFoundPage(User user) {
        String content = "<div class=\"content-shell\" style=\"text-align:center; padding-top:5rem;\">"
            + "<div style=\"display:flex; justify-content:center; margin-bottom:1rem; color:var(--muted);\">" + SVG_SEARCH + "</div>"
            + "<p class=\"eyebrow\">404 &middot; Not Found</p>"
            + "<h1>Page <em>Not Found</em></h1>"
            + "<p style=\"color:var(--muted); margin:1rem 0 2rem;\">The requested learning module or endpoint is unavailable.</p>"
            + "<a href=\"/dashboard\" class=\"card-btn card-btn-primary\">Return to Dashboard " + SVG_ARROW + "</a>"
            + "</div>";
        return layout("Page Not Found", "", user, content);
    }

    private static String privacyPage(User user) {
        String backLink = user != null
            ? "<nav class=\"breadcrumbs\"><a href=\"/dashboard\">Dashboard</a> <span>&rsaquo;</span> <span>Privacy Policy</span></nav>"
            : "<nav class=\"breadcrumbs\"><a href=\"/login\">&larr; Back to Sign In</a> <span>&rsaquo;</span> <span>Privacy Policy</span></nav>";

        String content = "<div class=\"legal-container\"><header class=\"page-header\">"
            + backLink
            + "<p class=\"eyebrow\">Student Privacy &middot; Zero-Tracking Guarantee</p>"
            + "<h1>Privacy <em>Policy</em></h1>"
            + "<p class=\"page-intro-text\">NanoScholar is committed to strict data minimization under UN Sustainable Development Goal 4 (Quality Education). No tracking cookies, no advertisements, and no data sales.</p>"
            + "</header>"
            + "<article class=\"legal-card scroll-reveal\">"
            + "<section class=\"legal-section\">"
            + "<h2>1. Data Minimization Standard</h2>"
            + "<p>NanoScholar collects only the minimal data required to deliver core educational functionality. Student accounts consist strictly of a chosen username, an encrypted password credential, and grade level enrollment (Class 11 or 12). No telephone numbers, government IDs, physical addresses, or biometric data are ever requested or stored.</p>"
            + "</section>"
            + "<section class=\"legal-section\">"
            + "<h2>2. Zero Third-Party Trackers &amp; No Commercial Analytics</h2>"
            + "<p>We do not use Google Analytics, advertising SDKs, social media tracking pixels, or cross-site tracking beacons. Our HTTP responses are completely self-contained with no external DNS requests or external script injections.</p>"
            + "</section>"
            + "<section class=\"legal-section\">"
            + "<h2>3. Local Device Storage &amp; Offline Cache</h2>"
            + "<p>Your preferences (such as light or dark theme and sidebar layout) are stored strictly within your browser's local storage (`localStorage`). Revision formula cards, notes, and past examination papers are cached via the Service Worker CacheStorage API on your device so you can study uninterrupted during network outages.</p>"
            + "</section>"
            + "<section class=\"legal-section\">"
            + "<h2>4. Session Lifecycle &amp; Bookmarks</h2>"
            + "<p>Session authentication uses a standard secure HTTP-only session cookie. Bookmarked questions in your personal Notebook remain linked to your student session. You may clear your session and local storage at any time by selecting Log Out.</p>"
            + "</section>"
            + "<section class=\"legal-section\">"
            + "<h2>5. Inquiries &amp; SDG 4 Governance</h2>"
            + "<p>NanoScholar is published as an open educational resource adhering strictly to student privacy and open-access pedagogy.</p>"
            + "</section>"
            + "</article>"
            + "</div>";

        return layout("Privacy Policy", "privacy", user, content);
    }

    private static String termsPage(User user) {
        String backLink = user != null
            ? "<nav class=\"breadcrumbs\"><a href=\"/dashboard\">Dashboard</a> <span>&rsaquo;</span> <span>Terms &amp; Conditions</span></nav>"
            : "<nav class=\"breadcrumbs\"><a href=\"/login\">&larr; Back to Sign In</a> <span>&rsaquo;</span> <span>Terms &amp; Conditions</span></nav>";

        String content = "<div class=\"legal-container\"><header class=\"page-header\">"
            + backLink
            + "<p class=\"eyebrow\">Service Governance &middot; Educational Use Only</p>"
            + "<h1>Terms and <em>Conditions</em></h1>"
            + "<p class=\"page-intro-text\">Fair-use guidelines and academic standards governing access to the NanoScholar CBSE study portal.</p>"
            + "</header>"
            + "<article class=\"legal-card scroll-reveal\">"
            + "<section class=\"legal-section\">"
            + "<h2>1. Educational Purpose &amp; Academic License</h2>"
            + "<p>NanoScholar is provided free of charge to students, teachers, and secondary schools preparing for CBSE Class 11 and Class 12 board examinations. The portal is designed specifically for low-bandwidth 2G and 3G environments.</p>"
            + "</section>"
            + "<section class=\"legal-section\">"
            + "<h2>2. Permitted Academic Use</h2>"
            + "<p>Students are granted a personal, non-exclusive, non-transferable license to read lesson notes, generate mock tests, bookmark PYQs, and inspect step-by-step marking schemes for individual study and non-commercial classroom instruction.</p>"
            + "</section>"
            + "<section class=\"legal-section\">"
            + "<h2>3. CBSE Curriculum Alignment</h2>"
            + "<p>All syllabus structures, chapter notes, formulas, and previous year questions (PYQs) for Mathematics, Physics, and Chemistry adhere to standard Central Board of Secondary Education (CBSE) senior school curriculum blueprints.</p>"
            + "</section>"
            + "<section class=\"legal-section\">"
            + "<h2>4. Offline Availability &amp; PWA Reliability</h2>"
            + "<p>While every engineering measure has been taken to make cached offline content persistent across network disruptions via Progressive Web App technology, users should maintain backups of critical notes on their local devices.</p>"
            + "</section>"
            + "<section class=\"legal-section\">"
            + "<h2>5. Integrity &amp; Responsible Access</h2>"
            + "<p>Users agree not to execute automated denial-of-service scripts or scrapers that compromise access for students in bandwidth-constrained rural or remote regions.</p>"
            + "</section>"
            + "</article>"
            + "</div>";

        return layout("Terms and Conditions", "terms", user, content);
    }

    // ---------------- BASE LAYOUT WITH DARK THEME, SIDEBAR & PWA ----------------

    private static String layout(String title, String activeNav, User user, String content) {
        // Immediate dark theme and sidebar state initializer to prevent layout flash (< 300 B)
        String themeInitScript = "<script>(function(){var t=localStorage.getItem('theme'),d=document.documentElement;if(t)d.setAttribute('data-theme',t);else if(window.matchMedia&&window.matchMedia('(prefers-color-scheme: dark)').matches)d.setAttribute('data-theme','dark');if(localStorage.getItem('ns_sidebar_min')==='1'&&innerWidth>768)document.addEventListener('DOMContentLoaded',function(){var p=document.querySelector('.portal-layout');p&&p.classList.add('sidebar-minimized');});})();</script>";

        // PWA Service Worker Registration for unauthenticated pages
        String pwaScript = "<script>if('serviceWorker' in navigator)window.onload=function(){navigator.serviceWorker.register('/sw.js');};</script>";

        // Consolidated Zero-Bloat Client Script (< 1.1 KB minified)
        String clientScripts = "<script>"
            + "function getEl(i){return document.getElementById(i);}"
            + "function toggleTheme(){var d=document.documentElement,c=d.getAttribute('data-theme')==='dark'?'light':'dark';d.setAttribute('data-theme',c);localStorage.setItem('theme',c);}"
            + "function toggleBookmark(b,q){fetch('/api/bookmark?id='+q,{method:'POST'}).then(function(r){return r.json();}).then(function(d){b.classList.toggle('bookmarked',d.bookmarked);var s=b.querySelector('span');if(s)s.textContent=d.bookmarked?'Bookmarked':'Bookmark';});}"
            + "(function(){"
            + "var pl=document.querySelector('.portal-layout'),tb=getEl('sidebar-toggle-btn'),sb=getEl('sidebar'),ob=getEl('overlay'),mb=getEl('menu-btn'),cb=getEl('close-btn');"
            + "function c(o){if(sb&&ob)sb.classList.toggle('open',o),ob.classList.toggle('open',o);}"
            + "if(tb)tb.onclick=function(){innerWidth<=768?c(!sb.classList.contains('open')):pl&&localStorage.setItem('ns_sidebar_min',pl.classList.toggle('sidebar-minimized')?'1':'0');};"
            + "if(mb)mb.onclick=function(){c(true);};"
            + "if(cb)cb.onclick=function(){c(false);};"
            + "if(ob)ob.onclick=function(){c(false);};"
            + "if('IntersectionObserver' in window){var obs=new IntersectionObserver(function(es){es.forEach(function(e){if(e.isIntersecting){e.target.classList.add('is-visible');obs.unobserve(e.target);}});},{threshold:0.08});document.querySelectorAll('.scroll-reveal').forEach(function(e){obs.observe(e);});}else{document.querySelectorAll('.scroll-reveal').forEach(function(e){e.classList.add('is-visible');});}"
            + "if('serviceWorker' in navigator)window.onload=function(){navigator.serviceWorker.register('/sw.js');};"
            + "})();"
            + "</script>";

        // Unauthenticated layout
        if (user == null) {
            return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<meta name=\"theme-color\" content=\"#0b0f33\">"
                + "<meta name=\"description\" content=\"NanoScholar: Ultra-low bandwidth study portal for CBSE Class 11 &amp; 12 students.\">"
                + "<link rel=\"manifest\" href=\"/manifest.json\">"
                + "<link rel=\"canonical\" href=\"https://nanoscholar.edu\">"
                + "<link rel=\"icon\" type=\"image/svg+xml\" href=\"/favicon.svg\">"
                + "<link rel=\"alternate icon\" href=\"/favicon.ico\">"
                + "<title>" + escape(title) + " &middot; NanoScholar</title>"
                + themeInitScript
                + "<link rel=\"stylesheet\" href=\"/style.css\">"
                + "</head><body>"
                + content
                + clientScripts
                + "</body></html>";
        }

        // Active nav markers
        String activeHome = "home".equals(activeNav) ? "active" : "";
        String activeTest = "test-builder".equals(activeNav) ? "active" : "";
        String activeFormulas = "formulas".equals(activeNav) ? "active" : "";
        String activeNotes = "notes".equals(activeNav) ? "active" : "";
        String activeNotebook = "notebook".equals(activeNav) ? "active" : "";
        String activeProfile = "profile".equals(activeNav) ? "active" : "";
        String activeClass11 = "class11".equals(activeNav) ? "active" : "";
        String activeClass12 = "class12".equals(activeNav) ? "active" : "";

        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
            + "<meta name=\"theme-color\" content=\"#0b0f33\">"
            + "<meta name=\"description\" content=\"NanoScholar: Premium low-bandwidth study portal for CBSE Class 11 and 12 STEM education.\">"
            + "<link rel=\"manifest\" href=\"/manifest.json\">"
            + "<link rel=\"canonical\" href=\"https://nanoscholar.edu\">"
            + "<link rel=\"icon\" type=\"image/svg+xml\" href=\"/favicon.svg\">"
            + "<link rel=\"alternate icon\" href=\"/favicon.ico\">"
            + "<title>" + escape(title) + " &middot; NanoScholar</title>"
            + themeInitScript
            + "<link rel=\"stylesheet\" href=\"/style.css\">"
            + "</head><body>"
            + "<div class=\"portal-layout\">"
            // Mobile Topbar
            + "<header class=\"mobile-topbar\">"
            + "<button id=\"menu-btn\" class=\"hamburger-btn\" aria-label=\"Open Navigation Menu\">" + SVG_HAMBURGER + "</button>"
            + "<div class=\"mobile-brand\"><img src=\"/logo.svg\" alt=\"NanoScholar\" class=\"brand-logo-img\" style=\"width:1.85rem;height:1.85rem;\"><span>NanoScholar</span></div>"
            + "<div class=\"mobile-actions\">"
            + "<button class=\"theme-toggle-btn\" onclick=\"toggleTheme()\" aria-label=\"Toggle Dark/Light Mode\">" + SVG_THEME + "</button>"
            + "<span class=\"mobile-user-tag\">Class " + user.getGradeLevel() + "</span>"
            + "</div>"
            + "</header>"
            // Mobile Overlay
            + "<div id=\"overlay\" class=\"sidebar-overlay\"></div>"
            // The Responsive Animated Sidebar
            + "<aside id=\"sidebar\" class=\"sidebar\">"
            + "<div class=\"sidebar-header\">"
            + "<a href=\"/dashboard\" class=\"sidebar-brand\"><img src=\"/logo.svg\" alt=\"NanoScholar\" class=\"brand-logo-img\"><span>NanoScholar</span></a>"
            + "<div class=\"header-actions\">"
            + "<button id=\"sidebar-toggle-btn\" class=\"sidebar-toggle-btn\" title=\"Toggle Sidebar (Collapse / Expand)\" aria-label=\"Toggle Sidebar Navigation\">" + SVG_SIDEBAR_TOGGLE + "</button>"
            + "<button class=\"theme-toggle-btn\" onclick=\"toggleTheme()\" title=\"Toggle Dark/Light Mode\" aria-label=\"Toggle Dark/Light Mode\">" + SVG_THEME + "</button>"
            + "<button id=\"close-btn\" class=\"close-btn\" aria-label=\"Close Menu\">" + SVG_CLOSE + "</button>"
            + "</div>"
            + "</div>"
            // User Card in Sidebar
            + "<div class=\"sidebar-user\">"
            + "<div class=\"user-avatar-circle\">" + SVG_USER + "</div>"
            + "<div class=\"user-info\"><div class=\"user-name\">" + escape(user.getUsername()) + "</div>"
            + "<span class=\"user-grade-badge\">Class " + user.getGradeLevel() + " PCM</span></div>"
            + "</div>"
            // Sidebar Navigation
            + "<nav class=\"sidebar-nav\" aria-label=\"Sidebar Navigation\">"
            + "<span class=\"nav-label\">Main Menu</span>"
            + "<a href=\"/dashboard\" class=\"nav-item " + activeHome + "\" title=\"Home\">" + SVG_HOME + "<span>Home</span></a>"
            + "<a href=\"/profile\" class=\"nav-item " + activeProfile + "\" title=\"Preparation Tracker\">" + SVG_USER + "<span>Preparation Tracker</span></a>"
            + "<a href=\"/notebook\" class=\"nav-item " + activeNotebook + "\" title=\"My Notebook\">" + SVG_NOTEBOOK + "<span>My Notebook</span><span class=\"nav-pill-badge\">" + user.getBookmarkedQuestions().size() + "</span></a>"
            + "<span class=\"nav-label\">CBSE Prep Engine</span>"
            + "<a href=\"/test-builder\" class=\"nav-item " + activeTest + "\" title=\"Test Builder\">" + SVG_TEST + "<span>Test Builder</span></a>"
            + "<a href=\"/formulas\" class=\"nav-item " + activeFormulas + "\" title=\"Formula Cards\">" + SVG_FORMULA + "<span>Formula Cards</span></a>"
            + "<a href=\"/notes\" class=\"nav-item " + activeNotes + "\" title=\"Chapter Notes\">" + SVG_NOTES + "<span>Chapter Notes</span></a>"
            + "<span class=\"nav-label\">Curriculum Tracks</span>"
            + "<a href=\"/class?grade=11\" class=\"nav-item " + activeClass11 + "\" title=\"Class 11 Track\">" + SVG_BOOK + "<span>Class 11</span></a>"
            + "<a href=\"/class?grade=12\" class=\"nav-item " + activeClass12 + "\" title=\"Class 12 Track\">" + SVG_BOOK + "<span>Class 12</span></a>"
            + "<span class=\"nav-label\">Session</span>"
            + "<a href=\"/logout\" class=\"nav-item\" title=\"Log Out\">" + SVG_LOGOUT + "<span>Log Out</span></a>"
            + "</nav>"
            // Sidebar Footer
            + "<div class=\"sidebar-footer\">"
            + "<div class=\"sdg-badge\"><span class=\"sdg-dot\"></span><span>SDG 4: Quality Education</span></div>"
            + "<p class=\"bandwidth-badge\">Optimized for <strong>2G &amp; 3G</strong> connections.<br>Zero external fonts or scripts.</p>"
            + "<div class=\"sidebar-legal\"><a href=\"/privacy\">Privacy</a> &middot; <a href=\"/terms\">Terms</a></div>"
            + "</div>"
            + "</aside>"
            // Dynamic Right Content Area with overflow-y: auto
            + "<main class=\"main-content\">"
            + content
            + "</main>"
            + "</div>"
            // Zero-Bloat Consolidated Client Script (< 1.5 KB total)
            + clientScripts
            + "</body></html>";
    }

    // ---------------- UTILITIES & HELPERS ----------------

    private static String getSubjectIconSvg(String subjectName) {
        if (subjectName == null) return SVG_BOOK;
        if (subjectName.contains("Math")) return SVG_MATH;
        if (subjectName.contains("Phys")) return SVG_PHYSICS;
        if (subjectName.contains("Chem")) return SVG_CHEMISTRY;
        return SVG_BOOK;
    }

    private static void serveStaticFile(HttpExchange exchange, String filename, String contentType, int maxAge) throws IOException {
        byte[] bytes = null;
        Path[] searchPaths = new Path[]{
            Path.of("src", "main", "resources", "static", filename),
            Path.of("static", filename),
            Path.of("resources", "static", filename),
            Path.of("out", "static", filename),
            Path.of("out", filename)
        };
        for (Path p : searchPaths) {
            if (Files.exists(p)) {
                bytes = Files.readAllBytes(p);
                break;
            }
        }
        if (bytes == null) {
            try (java.io.InputStream is = StudyPortal.class.getResourceAsStream("/static/" + filename)) {
                if (is != null) bytes = is.readAllBytes();
            }
        }
        if (bytes == null) {
            try (java.io.InputStream is = StudyPortal.class.getResourceAsStream("/" + filename)) {
                if (is != null) bytes = is.readAllBytes();
            }
        }
        if (bytes == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        if (maxAge > 0) {
            exchange.getResponseHeaders().set("Cache-Control", "public, max-age=" + maxAge);
        } else {
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        }
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static void serveSvgIcon(HttpExchange exchange) throws IOException {
        serveStaticFile(exchange, "logo.svg", "image/svg+xml", 86400);
    }

    private static void sendHtml(HttpExchange exchange, String html) throws IOException {
        sendHtml(exchange, 200, html);
    }

    private static void sendHtml(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private static Map<String, String> queryParams(HttpExchange exchange) {
        return parseForm(exchange.getRequestURI().getRawQuery() == null ? "" : exchange.getRequestURI().getRawQuery());
    }

    private static Map<String, String> parseForm(String raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyMap();
        Map<String, String> values = new HashMap<>();
        for (String pair : raw.split("&")) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String val = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            values.put(key, val);
        }
        return values;
    }

    private static String urlEncode(String text) {
        return java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private static int parseInt(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}

package home.sounds;

public class Note {

    // Waveform types for different sound characteristics
    public enum WaveType {
        SINE, // Smooth, pure tone
        SQUARE, // Digital, harsh tone
        SAWTOOTH, // Buzzy, rich harmonics
        TRIANGLE, // Soft, mellow tone
        NOISE // White noise for percussion effects
    }

    // ADSR Envelope parameters for realistic sound shaping
    public static class ADSREnvelope {
        public final int attackMs; // Time to reach peak volume
        public final int decayMs; // Time to drop to sustain level
        public final double sustainLevel; // Volume level during sustain (0.0-1.0)
        public final int releaseMs; // Time to fade to silence

        public ADSREnvelope(int attackMs, int decayMs, double sustainLevel, int releaseMs) {
            this.attackMs = attackMs;
            this.decayMs = decayMs;
            this.sustainLevel = Math.max(0.0, Math.min(1.0, sustainLevel));
            this.releaseMs = releaseMs;
        }

        // Default envelope for musical notes
        public static ADSREnvelope musical() {
            return new ADSREnvelope(10, 100, 0.7, 200);
        }

        // Sharp attack for percussive sounds
        public static ADSREnvelope percussive() {
            return new ADSREnvelope(5, 50, 0.3, 300);
        }

        // Smooth pad-like envelope
        public static ADSREnvelope pad() {
            return new ADSREnvelope(200, 300, 0.8, 500);
        }

        // Quick laser-like envelope
        public static ADSREnvelope laser() {
            return new ADSREnvelope(2, 20, 0.1, 100);
        }
    }

    // Predefined note frequencies
    private static final Note A = new Note(440.0);
    private static final Note B = new Note(493.88);
    private static final Note C = new Note(523.25);
    private static final Note D = new Note(587.33);
    private static final Note E = new Note(659.25);
    private static final Note F = new Note(698.46);
    private static final Note G = new Note(783.99);
    private static final Note ASHARP = new Note(466.16);
    private static final Note CSHARP = new Note(554.37);
    private static final Note DSHARP = new Note(622.25);
    private static final Note FSHARP = new Note(739.99);
    private static final Note GSHARP = new Note(830.61);

    private final double baseFrequency;
    private double volume = 1.0;
    private int durationMs = 500;
    private WaveType waveType = WaveType.SINE;
    private ADSREnvelope envelope = ADSREnvelope.musical();
    private double[] harmonics = { 1.0 }; // Base frequency only by default
    private double pitchBendStart = 1.0; // Multiplier for frequency at start
    private double pitchBendEnd = 1.0; // Multiplier for frequency at end

    private Note(double frequency) {
        this.baseFrequency = frequency;
    }

    public Note(Note base, int durationMs, double volume) {
        this.baseFrequency = base.baseFrequency;
        this.durationMs = durationMs;
        this.volume = volume;
    }

    // Advanced constructor for complex sounds
    public Note(Note base, int durationMs, double volume, WaveType waveType, ADSREnvelope envelope) {
        this.baseFrequency = base.baseFrequency;
        this.durationMs = durationMs;
        this.volume = volume;
        this.waveType = waveType;
        this.envelope = envelope;
    }

    // Constructor with harmonics for rich timbres
    public Note(Note base, int durationMs, double volume, WaveType waveType, ADSREnvelope envelope,
            double[] harmonics) {
        this.baseFrequency = base.baseFrequency;
        this.durationMs = durationMs;
        this.volume = volume;
        this.waveType = waveType;
        this.envelope = envelope;
        this.harmonics = harmonics.clone();
    }

    // Constructor with pitch bending for effects
    public Note(Note base, int durationMs, double volume, WaveType waveType, ADSREnvelope envelope,
            double pitchBendStart, double pitchBendEnd) {
        this.baseFrequency = base.baseFrequency;
        this.durationMs = durationMs;
        this.volume = volume;
        this.waveType = waveType;
        this.envelope = envelope;
        this.pitchBendStart = pitchBendStart;
        this.pitchBendEnd = pitchBendEnd;
    }

    public double getFrequency() {
        return baseFrequency;
    }

    public double getFrequencyAtTime(double progressRatio) {
        double pitchMultiplier = pitchBendStart + (pitchBendEnd - pitchBendStart) * progressRatio;
        return baseFrequency * pitchMultiplier;
    }

    public double getVolume() {
        return volume;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public WaveType getWaveType() {
        return waveType;
    }

    public ADSREnvelope getEnvelope() {
        return envelope;
    }

    public double[] getHarmonics() {
        return harmonics.clone();
    }

    public double getPitchBendStart() {
        return pitchBendStart;
    }

    public double getPitchBendEnd() {
        return pitchBendEnd;
    }

    // Calculate amplitude at a specific time using ADSR envelope
    public double getAmplitudeAtTime(int timeMs) {
        ADSREnvelope env = this.envelope;

        if (timeMs < 0)
            return 0.0;

        // Attack phase
        if (timeMs < env.attackMs) {
            return (double) timeMs / env.attackMs;
        }

        // Decay phase
        if (timeMs < env.attackMs + env.decayMs) {
            double decayProgress = (double) (timeMs - env.attackMs) / env.decayMs;
            return 1.0 - (1.0 - env.sustainLevel) * decayProgress;
        }

        // Sustain phase
        int sustainEnd = durationMs - env.releaseMs;
        if (timeMs < sustainEnd) {
            return env.sustainLevel;
        }

        // Release phase
        if (timeMs < durationMs) {
            double releaseProgress = (double) (timeMs - sustainEnd) / env.releaseMs;
            return env.sustainLevel * (1.0 - releaseProgress);
        }

        return 0.0;
    }

    // Generate waveform sample at given angle
    public double getWaveformSample(double angle) {
        switch (waveType) {
        case SINE:
            return Math.sin(angle);
        case SQUARE:
            return Math.sin(angle) >= 0 ? 1.0 : -1.0;
        case SAWTOOTH:
            return 2.0 * (angle / (2 * Math.PI) - Math.floor(angle / (2 * Math.PI) + 0.5));
        case TRIANGLE:
            double sawValue = 2.0 * (angle / (2 * Math.PI) - Math.floor(angle / (2 * Math.PI) + 0.5));
            return 2.0 * Math.abs(sawValue) - 1.0;
        case NOISE:
            return Math.random() * 2.0 - 1.0;
        default:
            return Math.sin(angle);
        }
    }

    // Generate complex waveform with harmonics
    public double getComplexSample(double angle) {
        double sample = 0.0;
        for (int i = 0; i < harmonics.length; i++) {
            if (harmonics[i] > 0) {
                double harmonicAngle = angle * (i + 1);
                sample += getWaveformSample(harmonicAngle) * harmonics[i];
            }
        }
        return sample / harmonics.length; // Normalize
    }

    public static Note getNoteByName(String name) {
        switch (name.toUpperCase()) {
        case "A":
            return A;
        case "B":
            return B;
        case "C":
            return C;
        case "D":
            return D;
        case "E":
            return E;
        case "F":
            return F;
        case "G":
            return G;
        case "ASHARP":
        case "A#":
            return ASHARP;
        case "CSHARP":
        case "C#":
            return CSHARP;
        case "DSHARP":
        case "D#":
            return DSHARP;
        case "FSHARP":
        case "F#":
            return FSHARP;
        case "GSHARP":
        case "G#":
            return GSHARP;
        default:
            throw new IllegalArgumentException("Invalid note name: " + name);
        }
    }

}

package home.sounds;

import javax.sound.sampled.*;
import java.util.concurrent.*;

public class SoundManager {

    private double masterVolume = 1.0;
    private boolean enabled = true;

    // Audio configuration
    private static final float SAMPLE_RATE = 44100;
    private static final int BIT_DEPTH = 16;
    private static final int CHANNELS = 1; // Mono for simplicity

    // Thread pool for concurrent sound playback
    private final ExecutorService soundPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("SoundPlayer-" + System.currentTimeMillis());
        return t;
    });

    // Echo/Reverb settings
    private boolean reverbEnabled = false;
    private double reverbDecay = 0.3;
    private int reverbDelayMs = 100;

    public void play(Sound sound) {
        play(sound, 1.0);
    }

    public void play(Sound sound, double volumeMultiplier) {
        if (!enabled || masterVolume <= 0) {
            return;
        }

        soundPool.submit(() -> {
            try {
                playSound(sound, volumeMultiplier);
            } catch (Exception e) {
                System.err.println("Error playing sound: " + e.getMessage());
            }
        });
    }

    private void playSound(Sound sound, double volumeMultiplier) throws Exception {
        AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, BIT_DEPTH, CHANNELS, true, false);
        SourceDataLine dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open(audioFormat);
        dataLine.start();

        // Calculate total samples needed
        int totalSamples = 0;
        for (Note note : sound.notes) {
            totalSamples += note.getDurationMs() * (int) (SAMPLE_RATE / 1000);
        }

        // Buffer for audio data
        byte[] audioBuffer = new byte[totalSamples * (BIT_DEPTH / 8)];
        int bufferIndex = 0;

        // Generate audio for each note
        for (Note note : sound.notes) {
            int noteSamples = note.getDurationMs() * (int) (SAMPLE_RATE / 1000);

            for (int i = 0; i < noteSamples; i++) {
                double timeMs = (double) i / SAMPLE_RATE * 1000;
                double progress = (double) i / noteSamples;

                // Get frequency with pitch bending
                double frequency = note.getFrequencyAtTime(progress);
                double angle = i / (SAMPLE_RATE / frequency) * 2.0 * Math.PI;

                // Get complex waveform sample
                double sample = note.getComplexSample(angle);

                // Apply ADSR envelope
                double amplitude = note.getAmplitudeAtTime((int) timeMs);

                // Apply volume and master volume
                sample *= amplitude * note.getVolume() * volumeMultiplier * masterVolume;

                // Add reverb if enabled
                if (reverbEnabled && i > reverbDelayMs * (SAMPLE_RATE / 1000)) {
                    int delayIndex = bufferIndex - (int) (reverbDelayMs * (SAMPLE_RATE / 1000)) * (BIT_DEPTH / 8);
                    if (delayIndex >= 0 && delayIndex < audioBuffer.length - 1) {
                        // Read previous sample for reverb
                        short prevSample = (short) ((audioBuffer[delayIndex] & 0xFF)
                                | (audioBuffer[delayIndex + 1] << 8));
                        sample += (prevSample / 32767.0) * reverbDecay;
                    }
                }

                // Clamp and convert to 16-bit
                sample = Math.max(-1.0, Math.min(1.0, sample));
                short shortSample = (short) (sample * 32767);

                // Store in buffer (little-endian)
                if (bufferIndex < audioBuffer.length - 1) {
                    audioBuffer[bufferIndex++] = (byte) (shortSample & 0xFF);
                    audioBuffer[bufferIndex++] = (byte) ((shortSample >> 8) & 0xFF);
                }
            }
        }

        // Play the buffer
        dataLine.write(audioBuffer, 0, bufferIndex);
        dataLine.drain();
        dataLine.stop();
        dataLine.close();
    }

    public void playLayered(Sound[] sounds) {
        if (!enabled)
            return;

        for (int i = 0; i < sounds.length; i++) {
            final int index = i;
            final int delay = index * 50; // Slight delay between layers
            soundPool.submit(() -> {
                try {
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                    playSound(sounds[index], 1.0);
                } catch (Exception e) {
                    System.err.println("Error playing layered sound: " + e.getMessage());
                }
            });
        }
    }

    public void setVolume(double volume) {
        this.masterVolume = Math.max(0.0, Math.min(1.0, volume));
    }

    public double getVolume() {
        return masterVolume;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setReverbEnabled(boolean enabled) {
        this.reverbEnabled = enabled;
    }

    public void setReverbSettings(double decay, int delayMs) {
        this.reverbDecay = Math.max(0.0, Math.min(1.0, decay));
        this.reverbDelayMs = Math.max(0, delayMs);
    }

    // Utility method to create harmonically rich sounds
    public static double[] createHarmonics(double... amplitudes) {
        return amplitudes;
    }

    // Cleanup method
    public void shutdown() {
        soundPool.shutdown();
        try {
            if (!soundPool.awaitTermination(1, TimeUnit.SECONDS)) {
                soundPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            soundPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
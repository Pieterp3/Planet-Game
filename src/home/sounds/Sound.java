package home.sounds;

import home.sounds.Note.WaveType;
import home.sounds.Note.ADSREnvelope;

public enum Sound {

    // ============ UI SOUNDS ============
    HOVER_MENU_BUTTON(new Note[] { new Note(Note.getNoteByName("C"), 80, 0.3, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("E"), 60, 0.2, WaveType.SINE, ADSREnvelope.musical()) }),

    CLICK_MENU_BUTTON(
            new Note[] { new Note(Note.getNoteByName("C"), 120, 0.4, WaveType.TRIANGLE, ADSREnvelope.percussive()),
                    new Note(Note.getNoteByName("G"), 100, 0.3, WaveType.SINE, ADSREnvelope.percussive()) }),

    MENU_TRANSITION(new Note[] { new Note(Note.getNoteByName("C"), 200, 0.3, WaveType.SINE, ADSREnvelope.pad()),
            new Note(Note.getNoteByName("E"), 180, 0.25, WaveType.SINE, ADSREnvelope.pad()),
            new Note(Note.getNoteByName("G"), 160, 0.2, WaveType.SINE, ADSREnvelope.pad()) }),

    GOLD_DONATION(new Note[] {
            new Note(Note.getNoteByName("G"), 200, 0.6, WaveType.SINE, ADSREnvelope.musical(),
                    SoundManager.createHarmonics(1.0, 0.5, 0.25)),
            new Note(Note.getNoteByName("C"), 180, 0.5, WaveType.TRIANGLE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("E"), 160, 0.4, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("G"), 300, 0.7, WaveType.TRIANGLE, new ADSREnvelope(50, 100, 0.8, 150)) }),

    // ============ COMBAT SOUNDS ============
    LASER_FIRE(new Note[] {
            new Note(Note.getNoteByName("A"), 150, 0.6, WaveType.SAWTOOTH, ADSREnvelope.laser(),
                    SoundManager.createHarmonics(1.0, 0.3, 0.15, 0.1)),
            new Note(Note.getNoteByName("E"), 120, 0.4, WaveType.SQUARE, ADSREnvelope.laser()) }),

    LASER_HIT(new Note[] { new Note(Note.getNoteByName("D"), 80, 0.8, WaveType.NOISE, ADSREnvelope.percussive()),
            new Note(Note.getNoteByName("A"), 60, 0.6, WaveType.SAWTOOTH, ADSREnvelope.percussive()) }),

    SHIP_EXPLOSION(new Note[] { new Note(Note.getNoteByName("C"), 300, 0.9, WaveType.NOISE, ADSREnvelope.percussive()),
            new Note(Note.getNoteByName("F"), 250, 0.7, WaveType.SAWTOOTH, ADSREnvelope.percussive()),
            new Note(Note.getNoteByName("ASHARP"), 200, 0.5, WaveType.SQUARE, ADSREnvelope.percussive()) }),

    PLANET_DAMAGE(new Note[] {
            new Note(Note.getNoteByName("F"), 150, 0.9, WaveType.NOISE, ADSREnvelope.percussive(), 2.0, 0.6),
            new Note(Note.getNoteByName("D"), 120, 0.8, WaveType.SAWTOOTH, ADSREnvelope.percussive(), 1.8, 0.7),
            new Note(Note.getNoteByName("ASHARP"), 100, 0.6, WaveType.SQUARE, ADSREnvelope.percussive(), 1.5, 0.8),
            new Note(Note.getNoteByName("G"), 80, 0.4, WaveType.TRIANGLE, ADSREnvelope.percussive(), 1.2, 0.9) }),

    // ============ ABILITY SOUNDS ============
    // Each ability now has its own unique sound effect

    ABILITY_ACTIVATE_FREEZE(
            new Note[] { new Note(Note.getNoteByName("C"), 400, 0.5, WaveType.SINE, ADSREnvelope.pad(), 1.0, 0.7),
                    new Note(Note.getNoteByName("G"), 350, 0.4, WaveType.TRIANGLE, ADSREnvelope.pad(), 1.0, 0.6),
                    new Note(Note.getNoteByName("E"), 300, 0.3, WaveType.SINE, ADSREnvelope.pad(), 1.0, 0.5) }),

    ABILITY_ACTIVATE_MISSILE_BARRAGE(
            new Note[] { new Note(Note.getNoteByName("D"), 150, 0.8, WaveType.NOISE, ADSREnvelope.percussive()),
                    new Note(Note.getNoteByName("F"), 120, 0.7, WaveType.SAWTOOTH, ADSREnvelope.percussive(), 1.2, 0.9),
                    new Note(Note.getNoteByName("A"), 100, 0.6, WaveType.SQUARE, ADSREnvelope.percussive(), 1.5, 0.8),
                    new Note(Note.getNoteByName("D"), 80, 0.9, WaveType.NOISE, ADSREnvelope.percussive(), 2.0, 0.7) }),

    ABILITY_ACTIVATE_SHIELD(new Note[] {
            new Note(Note.getNoteByName("E"), 300, 0.6, WaveType.TRIANGLE, new ADSREnvelope(50, 50, 0.8, 200),
                    SoundManager.createHarmonics(1.0, 0.5, 0.25, 0.125)),
            new Note(Note.getNoteByName("A"), 250, 0.4, WaveType.SINE, ADSREnvelope.pad()) }),

    ABILITY_ACTIVATE_FACTORY_HYPE(new Note[] {
            new Note(Note.getNoteByName("G"), 200, 0.7, WaveType.SQUARE, new ADSREnvelope(30, 20, 0.9, 150)),
            new Note(Note.getNoteByName("B"), 180, 0.6, WaveType.SAWTOOTH, ADSREnvelope.musical(), 1.1, 0.8),
            new Note(Note.getNoteByName("D"), 160, 0.5, WaveType.TRIANGLE, ADSREnvelope.musical(), 1.2, 0.7) }),

    ABILITY_ACTIVATE_IMPROVED_FACTORIES(new Note[] {
            new Note(Note.getNoteByName("C"), 350, 0.6, WaveType.TRIANGLE, new ADSREnvelope(40, 60, 0.8, 250),
                    SoundManager.createHarmonics(1.0, 0.3, 0.15)),
            new Note(Note.getNoteByName("E"), 320, 0.5, WaveType.SINE, ADSREnvelope.pad()),
            new Note(Note.getNoteByName("G"), 300, 0.4, WaveType.TRIANGLE, ADSREnvelope.pad()) }),

    ABILITY_ACTIVATE_ANSWERED_PRAYERS(new Note[] {
            new Note(Note.getNoteByName("F"), 400, 0.7, WaveType.SINE, new ADSREnvelope(100, 150, 0.7, 150),
                    SoundManager.createHarmonics(1.0, 0.5, 0.25, 0.125, 0.0625)),
            new Note(Note.getNoteByName("A"), 380, 0.6, WaveType.TRIANGLE, ADSREnvelope.musical(), 0.9, 0.8),
            new Note(Note.getNoteByName("C"), 360, 0.5, WaveType.SINE, ADSREnvelope.musical(), 0.8, 0.7) }),

    ABILITY_ACTIVATE_CURSE(new Note[] {
            new Note(Note.getNoteByName("FSHARP"), 600, 0.8, WaveType.SAWTOOTH, new ADSREnvelope(80, 120, 0.5, 400),
                    1.8, 0.6),
            new Note(Note.getNoteByName("ASHARP"), 550, 0.6, WaveType.SQUARE, ADSREnvelope.pad(), 1.6, 0.5),
            new Note(Note.getNoteByName("DSHARP"), 500, 0.4, WaveType.TRIANGLE, ADSREnvelope.pad(), 1.4, 0.4) }),

    ABILITY_ACTIVATE_BLACK_HOLE(new Note[] {
            new Note(Note.getNoteByName("C"), 800, 0.7, WaveType.SAWTOOTH, new ADSREnvelope(200, 100, 0.6, 500), 2.0,
                    0.5),
            new Note(Note.getNoteByName("FSHARP"), 600, 0.5, WaveType.TRIANGLE, ADSREnvelope.pad(), 1.8, 0.4),
            new Note(Note.getNoteByName("ASHARP"), 400, 0.3, WaveType.SINE, ADSREnvelope.pad(), 1.5, 0.3) }),

    ABILITY_ACTIVATE_PLANETARY_FLAME(new Note[] {
            new Note(Note.getNoteByName("D"), 500, 0.8, WaveType.NOISE, new ADSREnvelope(20, 80, 0.4, 400)),
            new Note(Note.getNoteByName("A"), 450, 0.6, WaveType.SAWTOOTH, ADSREnvelope.pad(), 1.2, 0.8),
            new Note(Note.getNoteByName("F"), 400, 0.4, WaveType.TRIANGLE, ADSREnvelope.pad()) }),

    ABILITY_ACTIVATE_PLANETARY_INFECTION(new Note[] {
            new Note(Note.getNoteByName("E"), 450, 0.7, WaveType.SAWTOOTH, new ADSREnvelope(60, 90, 0.6, 300), 1.3,
                    0.7),
            new Note(Note.getNoteByName("GSHARP"), 420, 0.6, WaveType.SQUARE, ADSREnvelope.pad(), 1.1, 0.6),
            new Note(Note.getNoteByName("B"), 400, 0.5, WaveType.TRIANGLE, ADSREnvelope.pad(), 0.9, 0.5) }),

    ABILITY_ACTIVATE_UNSTOPPABLE_SHIPS(new Note[] {
            new Note(Note.getNoteByName("A"), 300, 0.8, WaveType.SQUARE, new ADSREnvelope(40, 30, 0.9, 230)),
            new Note(Note.getNoteByName("C"), 280, 0.7, WaveType.SAWTOOTH, ADSREnvelope.musical(), 1.1, 0.8),
            new Note(Note.getNoteByName("E"), 260, 0.6, WaveType.TRIANGLE, ADSREnvelope.musical(), 1.2, 0.7),
            new Note(Note.getNoteByName("A"), 240, 0.9, WaveType.SQUARE, ADSREnvelope.musical(), 1.3, 0.9) }),

    ABILITY_ACTIVATE_ORBITAL_FREEZE(new Note[] {
            new Note(Note.getNoteByName("B"), 500, 0.6, WaveType.SINE, new ADSREnvelope(80, 100, 0.7, 320),
                    SoundManager.createHarmonics(1.0, 0.4, 0.2, 0.1)),
            new Note(Note.getNoteByName("D"), 480, 0.5, WaveType.TRIANGLE, ADSREnvelope.pad(), 0.9, 0.6),
            new Note(Note.getNoteByName("FSHARP"), 460, 0.4, WaveType.SINE, ADSREnvelope.pad(), 0.8, 0.5) }),

    // ============ ACHIEVEMENT & SUCCESS SOUNDS ============
    SUCCESS_CHIME(new Note[] { new Note(Note.getNoteByName("C"), 200, 0.5, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("E"), 200, 0.4, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("G"), 200, 0.3, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("C"), 300, 0.6, WaveType.TRIANGLE, ADSREnvelope.musical()) }),

    ACHIEVEMENT_UNLOCK(new Note[] { new Note(Note.getNoteByName("G"), 150, 0.4, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("C"), 150, 0.5, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("E"), 150, 0.4, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("G"), 300, 0.6, WaveType.TRIANGLE, new ADSREnvelope(50, 100, 0.8, 150),
                    SoundManager.createHarmonics(1.0, 0.5, 0.25)) }),

    GAME_WIN(new Note[] { new Note(Note.getNoteByName("C"), 200, 0.6, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("D"), 200, 0.5, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("E"), 200, 0.5, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("F"), 200, 0.5, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("G"), 200, 0.6, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("A"), 200, 0.5, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("B"), 200, 0.5, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("C"), 400, 0.8, WaveType.TRIANGLE, new ADSREnvelope(50, 100, 0.8, 250)) }),

    // ============ ERROR & WARNING SOUNDS ============
    ERROR_BEEP(new Note[] { new Note(Note.getNoteByName("F"), 150, 0.7, WaveType.SQUARE, ADSREnvelope.percussive()),
            new Note(Note.getNoteByName("D"), 150, 0.6, WaveType.SQUARE, ADSREnvelope.percussive()),
            new Note(Note.getNoteByName("ASHARP"), 200, 0.5, WaveType.TRIANGLE, ADSREnvelope.percussive()) }),

    WARNING_ALERT(new Note[] { new Note(Note.getNoteByName("A"), 100, 0.8, WaveType.SQUARE, ADSREnvelope.percussive()),
            new Note(Note.getNoteByName("A"), 100, 0.8, WaveType.SQUARE, ADSREnvelope.percussive()),
            new Note(Note.getNoteByName("A"), 200, 0.6, WaveType.TRIANGLE, ADSREnvelope.percussive()) }),

    // ============ AMBIENT & NOTIFICATION SOUNDS ============
    NOTIFICATION_SOFT(new Note[] { new Note(Note.getNoteByName("E"), 120, 0.3, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("A"), 100, 0.25, WaveType.SINE, ADSREnvelope.musical()) }),

    PLANET_SELECT(new Note[] { new Note(Note.getNoteByName("C"), 80, 0.4, WaveType.TRIANGLE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("E"), 60, 0.3, WaveType.SINE, ADSREnvelope.musical()) }),

    SHIP_DEPLOY(new Note[] { new Note(Note.getNoteByName("G"), 120, 0.5, WaveType.TRIANGLE, ADSREnvelope.percussive()),
            new Note(Note.getNoteByName("D"), 100, 0.4, WaveType.SINE, ADSREnvelope.percussive(), 1.0, 1.2) }),

    // ============ PURCHASE & UPGRADE SOUNDS ============
    PURCHASE_SUCCESS(new Note[] { new Note(Note.getNoteByName("C"), 150, 0.5, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("F"), 150, 0.4, WaveType.SINE, ADSREnvelope.musical()),
            new Note(Note.getNoteByName("A"), 200, 0.6, WaveType.TRIANGLE, ADSREnvelope.musical()) }),

    PURCHASE_FAIL(new Note[] { new Note(Note.getNoteByName("E"), 200, 0.6, WaveType.SQUARE, ADSREnvelope.percussive()),
            new Note(Note.getNoteByName("C"), 300, 0.4, WaveType.TRIANGLE, ADSREnvelope.percussive()) });

    public final Note[] notes;

    Sound(Note[] notes) {
        this.notes = notes;
    }
}

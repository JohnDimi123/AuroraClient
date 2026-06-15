package dev.aurora.client.util;

/**
 * Tracks combat session statistics (kills, deaths, wins, combos). Updated by
 * combat events and BedWars chat parsing; read by HUD modules.
 */
public final class SessionStats {

    private int kills;
    private int deaths;
    private int wins;
    private int finalKills;
    private int bedsBroken;

    private int currentCombo;
    private int bestCombo;
    private final long sessionStart = System.currentTimeMillis();

    public void addKill() { kills++; }
    public void addDeath() { deaths++; currentCombo = 0; }
    public void addWin() { wins++; }
    public void addFinalKill() { finalKills++; }
    public void addBedBroken() { bedsBroken++; }

    /** Registers a hit for combo tracking. */
    public void onHit() {
        currentCombo++;
        if (currentCombo > bestCombo) bestCombo = currentCombo;
    }

    /** Resets the active combo (e.g. on taking damage). */
    public void breakCombo() { currentCombo = 0; }

    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getWins() { return wins; }
    public int getFinalKills() { return finalKills; }
    public int getBedsBroken() { return bedsBroken; }
    public int getCurrentCombo() { return currentCombo; }
    public int getBestCombo() { return bestCombo; }

    /** Kill/death ratio; deaths==0 returns kills as the ratio. */
    public double getKdr() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public long getSessionDurationMs() {
        return System.currentTimeMillis() - sessionStart;
    }

    public String getFormattedDuration() {
        long s = getSessionDurationMs() / 1000;
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }
}

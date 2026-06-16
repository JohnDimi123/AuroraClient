package dev.aurora.client.anticheat;

import dev.aurora.client.anticheat.check.AttackTimingCheck;
import dev.aurora.client.anticheat.check.Check;
import dev.aurora.client.anticheat.check.MovementCheck;
import dev.aurora.client.anticheat.check.ReachCheck;
import dev.aurora.client.anticheat.check.RotationCheck;
import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinates the informational anti-cheat system.
 * <p>
 * <b>Scope and ethics.</b> This engine is a passive observer. It collects only
 * data every player can already see (positions, rotations, attack timing) and
 * runs statistical heuristics to flag <em>possible</em> suspicious behaviour. It
 * never claims certainty, never takes action against anyone, and never sends
 * data to a server automatically — it simply surfaces a confidence-scored note
 * to the local user, who decides what to do. Per-player alert cooldowns prevent
 * spam, and a confidence floor reduces false positives.
 */
public final class AntiCheatEngine {

    private final Map<String, PlayerObservation> observations = new HashMap<>();
    private final Map<String, Long> lastAlert = new HashMap<>();
    private final List<Check> checks = new ArrayList<>();
    private final List<DetectionResult> recentAlerts = new ArrayList<>();

    private final ReachCheck reachCheck = new ReachCheck();

    /** Minimum confidence before an alert is surfaced. */
    private double confidenceThreshold = 0.70;
    /** Cooldown between alerts for the same player (ms). */
    private static final long ALERT_COOLDOWN = 8000L;

    private boolean enabled = false;

    public AntiCheatEngine() {
        checks.add(new RotationCheck());
        checks.add(new AttackTimingCheck());
        checks.add(new MovementCheck());
        checks.add(reachCheck);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }
    public void setConfidenceThreshold(double t) { this.confidenceThreshold = t; }
    public List<DetectionResult> getRecentAlerts() { return recentAlerts; }

    /** Feeds a measured reach value into the reach check. */
    public void reportReach(double reach) { reachCheck.setLastReach(reach); }

    /** Registers an observed attack timestamp for a named player. */
    public void reportAttack(String player) {
        observations.computeIfAbsent(player, PlayerObservation::new)
                .addAttack(System.currentTimeMillis());
    }

    @Listen
    public void onTick(TickEvent event) {
        if (!enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        // Sample publicly visible player state.
        @SuppressWarnings("unchecked")
        java.util.List<PlayerEntity> worldPlayers =
                (java.util.List<PlayerEntity>) mc.world.playerEntities;
        for (PlayerEntity player : worldPlayers) {
            if (player == mc.player) continue;
            String name = player.getGameProfile().getName();
            PlayerObservation obs = observations.computeIfAbsent(name, PlayerObservation::new);
            obs.addRotation(player.yaw, player.pitch);
            obs.addPosition(player.x, player.y, player.z);
        }

        // Run checks on players with enough data.
        for (PlayerObservation obs : observations.values()) {
            if (!obs.hasEnoughData()) continue;
            for (Check check : checks) {
                DetectionResult result = check.analyse(obs);
                if (result != null && result.confidence >= confidenceThreshold) {
                    maybeAlert(obs.name, result);
                }
            }
        }
    }

    private void maybeAlert(String player, DetectionResult result) {
        long now = System.currentTimeMillis();
        Long last = lastAlert.get(player);
        if (last != null && now - last < ALERT_COOLDOWN) return;
        lastAlert.put(player, now);

        recentAlerts.add(0, result);
        while (recentAlerts.size() > 10) recentAlerts.remove(recentAlerts.size() - 1);

        String report = formatReport(player, result);

        // Chat alert (client-side only — visible to local user, not sent anywhere).
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.inGameHud.getChatHud().addMessage(
                    new net.minecraft.text.LiteralText(report));
        }
        // HUD toast.
        dev.aurora.client.Aurora.INSTANCE.getNotificationManager().push(
                "AntiCheat: " + player,
                result.checkName + " " + result.getPercent() + "%",
                dev.aurora.client.render.notification.NotificationManager.Type.WARNING, 6000);
    }

    /**
     * Builds the formatted, deliberately non-definitive report line.
     * <pre>
     * [Aurora AntiCheat] Suspicious Player: Steve
     * Detection: Possible KillAura | Confidence: 89% | Reason: ...
     * </pre>
     */
    public String formatReport(String player, DetectionResult result) {
        return "\u00a7b[Aurora AntiCheat] \u00a7fSuspicious: \u00a7e" + player
                + " \u00a77| \u00a7fPossible " + result.checkName
                + " \u00a77| \u00a7fConfidence: \u00a7c" + result.getPercent() + "%"
                + " \u00a77| \u00a7f" + result.reason;
    }
}

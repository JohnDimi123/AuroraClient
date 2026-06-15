package dev.aurora.client.module.impl.bedwars;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.ChatReceiveEvent;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.module.impl.hud.HudModule;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.setting.BooleanSetting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BedWars information overlay.
 * <p>
 * Parses public game chat (bed-break, final-kill, game-start messages) to track
 * team bed status, final kills and a match timer. All information is derived
 * from messages every player already sees — it provides no hidden information
 * and performs no automation, so it is server-rules-safe.
 */
public final class BedwarsOverlay extends HudModule {

    /** Per-team state inferred from chat. */
    private static final class Team {
        boolean bedAlive = true;
        int finalKills;
    }

    private static final Pattern BED_BROKEN =
            Pattern.compile("BED DESTRUCTION.*?(Red|Blue|Green|Yellow|Aqua|White|Pink|Gray) Bed",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern FINAL_KILL =
            Pattern.compile("(\\w+) .*FINAL KILL.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern GAME_START =
            Pattern.compile("Protect your bed and destroy the enemy beds", Pattern.CASE_INSENSITIVE);

    private final Map<String, Team> teams = new LinkedHashMap<>();
    private long matchStart;
    private boolean inGame;
    private int sessionFinalKills;

    private final BooleanSetting showTimer =
            new BooleanSetting("Show Timer", "Display match timer", true);
    private final BooleanSetting showBeds =
            new BooleanSetting("Show Beds", "Display team bed status", true);
    private final BooleanSetting showFinals =
            new BooleanSetting("Show Final Kills", "Display final-kill counter", true);

    public BedwarsOverlay() {
        super("BedWars Overlay", "Bed status, final kills and match timer", 280, 4);
        addSettings(showTimer, showBeds, showFinals);
    }

    @Listen
    public void onChat(ChatReceiveEvent event) {
        String msg = event.message;

        if (GAME_START.matcher(msg).find()) {
            startMatch();
            return;
        }

        Matcher bed = BED_BROKEN.matcher(msg);
        if (bed.find()) {
            String color = bed.group(1);
            teams.computeIfAbsent(color, k -> new Team()).bedAlive = false;
            Aurora.INSTANCE.getNotificationManager().push("BedWars",
                    color + " bed destroyed",
                    dev.aurora.client.render.notification.NotificationManager.Type.WARNING);
            return;
        }

        if (FINAL_KILL.matcher(msg).find()) {
            sessionFinalKills++;
            Aurora.INSTANCE.getSessionStats().addFinalKill();
        }
    }

    private void startMatch() {
        teams.clear();
        matchStart = System.currentTimeMillis();
        inGame = true;
        for (String c : new String[]{"Red", "Blue", "Green", "Yellow"}) {
            teams.put(c, new Team());
        }
    }

    private String matchTime() {
        if (!inGame) return "00:00";
        long s = (System.currentTimeMillis() - matchStart) / 1000;
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    @Override protected void draw(float partialTicks) {
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("BedWars");
        if (showTimer.get()) lines.add("Time: " + matchTime());
        if (showFinals.get()) lines.add("Final Kills: " + sessionFinalKills);
        if (showBeds.get()) {
            for (Map.Entry<String, Team> e : teams.entrySet()) {
                lines.add(e.getKey() + ": " + (e.getValue().bedAlive ? "Bed OK" : "Broken"));
            }
        }

        int lineH = font().getHeight() + 1;
        double maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, font().getWidth(l) + 12);
        double h = lineH * lines.size() + 6;
        setSize(maxW, h);

        Render2D.roundedRect(0, 0, maxW, h, 4, theme.surface());
        Render2D.rect(0, 0, 2, h, theme.accent());
        for (int i = 0; i < lines.size(); i++) {
            int color = i == 0 ? theme.accent() : theme.text();
            font().drawWithShadow(lines.get(i), 6, 4 + i * lineH, color);
        }
    }
}

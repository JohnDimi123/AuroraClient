package dev.aurora.client.render.notification;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.render.animation.Animation;
import dev.aurora.client.render.font.FontRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Toast-style notification system. Notifications slide in from the right, hold,
 * then slide out. Rendering is purely additive over the HUD.
 */
public final class NotificationManager {

    /** Severity controls the accent bar colour. */
    public enum Type { INFO, SUCCESS, WARNING, ERROR }

    private static final class Notification {
        final String title;
        final String message;
        final Type type;
        final long created = System.currentTimeMillis();
        final long lifetimeMs;
        final Animation slide = new Animation(0, 250, Animation.Easing.EASE_OUT_EXPO);

        Notification(String title, String message, Type type, long lifetimeMs) {
            this.title = title; this.message = message; this.type = type; this.lifetimeMs = lifetimeMs;
            slide.setTarget(1);
        }
        boolean expired() { return System.currentTimeMillis() - created > lifetimeMs; }
    }

    private final List<Notification> active = new ArrayList<>();

    public void push(String title, String message, Type type) {
        push(title, message, type, 4000);
    }

    public void push(String title, String message, Type type, long lifetimeMs) {
        synchronized (active) { active.add(new Notification(title, message, type, lifetimeMs)); }
    }

    /** Renders all active notifications; call from the HUD render event. */
    public void render(int screenWidth, int screenHeight) {
        synchronized (active) {
            FontRenderer font = Aurora.INSTANCE.getFontManager().regular();
            FontRenderer small = Aurora.INSTANCE.getFontManager().regular();
            Theme theme = Aurora.INSTANCE.getThemeManager().getActive();

            double y = screenHeight - 16;
            Iterator<Notification> it = active.iterator();
            List<Notification> snapshot = new ArrayList<>(active);
            for (int i = snapshot.size() - 1; i >= 0; i--) {
                Notification n = snapshot.get(i);
                if (n.expired() && n.slide.getValue() <= 0.01) continue;
                if (n.expired()) n.slide.setTarget(0);

                double width = Math.max(160, font.getWidth(n.title) + 24);
                double height = 36;
                double progress = n.slide.update();
                double x = screenWidth - (width + 12) * progress + (1 - progress) * 8;
                y -= height + 6;

                Render2D.roundedRect(x, y, width, height, 4, theme.surface());
                Render2D.rect(x, y, 3, height, accent(theme, n.type));
                font.drawWithShadow(n.title, (float) x + 10, (float) y + 6, theme.text());
                small.drawWithShadow(n.message, (float) x + 10, (float) y + 20, theme.textMuted());
            }
            // Purge fully-faded expired notifications.
            active.removeIf(n -> n.expired() && n.slide.getValue() <= 0.01);
        }
    }

    private static int accent(Theme theme, Type type) {
        switch (type) {
            case SUCCESS: return theme.positive();
            case WARNING: return 0xFFFFB020;
            case ERROR:   return theme.negative();
            default:      return theme.accent();
        }
    }
}

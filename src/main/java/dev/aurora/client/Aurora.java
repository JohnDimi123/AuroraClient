package dev.aurora.client;

import dev.aurora.client.anticheat.AntiCheatEngine;
import dev.aurora.client.config.ConfigManager;
import dev.aurora.client.event.EventBus;
import dev.aurora.client.gui.hudeditor.HudManager;
import dev.aurora.client.gui.theme.ThemeManager;
import dev.aurora.client.module.ModuleManager;
import dev.aurora.client.network.NetworkManager;
import dev.aurora.client.render.font.FontManager;
import dev.aurora.client.render.notification.NotificationManager;
import dev.aurora.client.util.ClickTracker;
import dev.aurora.client.util.PerformanceMonitor;
import dev.aurora.client.util.SessionStats;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aurora Client — main entrypoint and service locator.
 * <p>
 * A single {@code INSTANCE} owns every manager. Initialisation order matters:
 * the {@link EventBus}, {@link ThemeManager} and {@link FontManager} must exist
 * before modules (which subscribe and render), and the {@link ConfigManager} is
 * loaded last so it can restore saved state.
 *
 * <h2>Design notes</h2>
 * Aurora is a <em>legitimate</em> enhancement client. It contains no movement,
 * combat, or packet modifications that would grant an unfair advantage. Its
 * "AntiCheat" feature is a purely informational, client-side observer.
 */
public final class Aurora implements ClientModInitializer {

    public static final String NAME = "Aurora";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    /** Global singleton; assigned in {@link #onInitializeClient()}. */
    public static Aurora INSTANCE;

    private EventBus eventBus;
    private ThemeManager themeManager;
    private FontManager fontManager;
    private NotificationManager notificationManager;
    private ModuleManager moduleManager;
    private HudManager hudManager;
    private ConfigManager configManager;
    private AntiCheatEngine antiCheatEngine;
    private NetworkManager networkManager;

    private PerformanceMonitor performanceMonitor;
    private SessionStats sessionStats;
    private ClickTracker clickTracker;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        LOGGER.info("Initialising {} v{}", NAME, VERSION);

        // Core services
        eventBus = new EventBus();
        themeManager = new ThemeManager();
        fontManager = new FontManager();
        notificationManager = new NotificationManager();

        // State trackers
        performanceMonitor = new PerformanceMonitor();
        sessionStats = new SessionStats();
        clickTracker = new ClickTracker();

        // Subsystems
        hudManager = new HudManager();
        moduleManager = new ModuleManager();
        antiCheatEngine = new AntiCheatEngine();
        networkManager = new NetworkManager();

        // Wire up event subscribers for always-on managers.
        eventBus.subscribe(moduleManager);
        eventBus.subscribe(antiCheatEngine);
        eventBus.subscribe(networkManager);

        // Persisted configuration restored last.
        configManager = new ConfigManager();
        configManager.load();

        // Save on JVM shutdown so progress is never lost.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { configManager.save(); } catch (Exception ignored) {}
        }, "Aurora-Config-Save"));

        notificationManager.push(NAME + " v" + VERSION, "Client loaded successfully",
                NotificationManager.Type.SUCCESS);
        LOGGER.info("{} initialised with {} modules", NAME, moduleManager.getModules().size());
    }

    // ---- service accessors --------------------------------------------------

    public EventBus getEventBus() { return eventBus; }
    public ThemeManager getThemeManager() { return themeManager; }
    public FontManager getFontManager() { return fontManager; }
    public NotificationManager getNotificationManager() { return notificationManager; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public HudManager getHudManager() { return hudManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public AntiCheatEngine getAntiCheatEngine() { return antiCheatEngine; }
    public NetworkManager getNetworkManager() { return networkManager; }
    public PerformanceMonitor getPerformanceMonitor() { return performanceMonitor; }
    public SessionStats getSessionStats() { return sessionStats; }
    public ClickTracker getClickTracker() { return clickTracker; }
}

package com.github.manevolent.jbot.plugin.java;

import com.github.manevolent.jbot.artifact.Artifact;
import com.github.manevolent.jbot.Bot;
import com.github.manevolent.jbot.artifact.ArtifactIdentifier;
import com.github.manevolent.jbot.command.CommandManager;
import com.github.manevolent.jbot.command.executor.CommandExecutor;
import com.github.manevolent.jbot.database.Database;
import com.github.manevolent.jbot.database.DatabaseInitializer;
import com.github.manevolent.jbot.database.DatabaseManager;
import com.github.manevolent.jbot.database.SystemDatabaseManager;
import com.github.manevolent.jbot.event.EventListener;
import com.github.manevolent.jbot.event.EventManager;
import com.github.manevolent.jbot.platform.Platform;
import com.github.manevolent.jbot.platform.PlatformManager;
import com.github.manevolent.jbot.plugin.Plugin;
import com.github.manevolent.jbot.plugin.PluginException;

import java.util.*;
import java.util.stream.Collectors;

public abstract class JavaPlugin implements Plugin {
    private boolean initialized = false;

    private PluginEventManager eventManager;
    private PluginCommandManager commandManager;
    private PluginPlatformManager platformManager;
    private PluginDatabaseManager databaseManager;

    private Bot bot;

    private final Artifact artifact;

    private final Object enableLock = new Object();
    private boolean enabled;

    protected JavaPlugin(Artifact artifact) {
        this.artifact = artifact;
    }

    /**
     * Gets the <b>Bot</b> instance associated with this plugin.
     * @return Bot instance.
     */
    protected final Bot getBot() {
        return bot;
    }

    protected final EventManager getEventManager() {
        return eventManager;
    }

    protected final CommandManager getCommandManager() {
        return commandManager;
    }

    protected final PlatformManager getPlatformManager() {
        return platformManager;
    }

    protected final DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Sets the <b>Bot</b> instance associated with this plugin.
     * @param bot Bot instance.
     */
    public final void initialize(Bot bot,
                                 CommandManager commandManager,
                                 EventManager eventManager,
                                 PlatformManager platformManager,
                                 SystemDatabaseManager databaseManager)
            throws IllegalStateException {
        synchronized (this) {
            if (initialized) throw new IllegalStateException();

            this.bot = bot;
            this.commandManager = new PluginCommandManager(commandManager);
            this.eventManager = new PluginEventManager(eventManager);
            this.platformManager = new PluginPlatformManager(platformManager);
            this.databaseManager = new PluginDatabaseManager(databaseManager);

            this.initialized = true;
        }
    }

    @Override
    public final String getName() {
        return artifact.getManifest().getArtifactId().toLowerCase();
    }

    @Override
    public final Artifact getArtifact() {
        return artifact;
    }

    @Override
    public final boolean setEnabled(boolean enabled) throws PluginException {
        synchronized (enableLock) {
            if (this.enabled != enabled) {
                if (enabled) {
                    onEnable();
                    this.enabled = true;
                    onEnabled();
                } else {
                    onDisable();
                    commandManager.destroy();
                    eventManager.destroy();
                    platformManager.destroy();
                    databaseManager.destroy();
                    this.enabled = false;
                    onDisabled();
                }
                return true;
            }
            return false;
        }
    }

    protected void onEnable() throws PluginException {}
    protected void onEnabled() throws PluginException {}
    protected void onDisable() throws PluginException {}
    protected void onDisabled() throws PluginException {}

    private class PluginEventManager implements EventManager {
        private final Object registrationLock = new Object();
        private final EventManager eventManager;
        private final List<EventListener> registeredListeners = new LinkedList<>();

        private PluginEventManager(EventManager eventManager) {
            this.eventManager = eventManager;
        }

        @Override
        public void registerListener(EventListener listener) {
            synchronized (registrationLock) {
                eventManager.registerListener(listener);
                registeredListeners.add(listener);
            }
        }

        @Override
        public void unregisterListener(EventListener listener) {
            synchronized (registrationLock) {
                if (!registeredListeners.contains(listener)) return;

                eventManager.unregisterListener(listener);
                registeredListeners.remove(listener);
            }
        }
        private void destroy() {
            synchronized (registrationLock) {
                registeredListeners.forEach(this::unregisterListener);
            }
        }
    }

    private class PluginCommandManager extends CommandManager {
        private final Object registrationLock = new Object();
        private final CommandManager commandManager;
        private final List<String> registeredLabels = new LinkedList<>();

        private PluginCommandManager(CommandManager commandManager) {
            this.commandManager = commandManager;
        }

        @Override
        public Registration registerExecutor(String label, CommandExecutor executor) {
            synchronized (registrationLock) {
                if (registeredLabels.contains(label))
                    throw new IllegalArgumentException(label);

                Registration registration = commandManager.registerExecutor(label, executor);
                registeredLabels.add(label);
                return new PluginRegistration(executor, registration, label);
            }
        }

        @Override
        public void unregisterExecutor(String label) {
            synchronized (registrationLock) {
                if (!registeredLabels.contains(label))
                    throw new IllegalArgumentException(label);

                commandManager.unregisterExecutor(label);
                registeredLabels.remove(label);
            }
        }

        @Override
        public CommandExecutor getExecutor(String label) {
            if (registeredLabels.contains(label))
                return commandManager.getExecutor(label);
            else
                return null;
        }

        private void destroy() {
            synchronized (registrationLock) {
                registeredLabels.forEach(this::unregisterExecutor);
            }
        }

        private class PluginRegistration extends Registration {
            private final Registration registration;

            public PluginRegistration(CommandExecutor executor, Registration registration, String label) {
                super(executor, label);
                this.registration = registration;
            }

            @Override
            public Registration alias(String alias) {
                synchronized (registrationLock) {
                    registration.alias(alias);
                    registeredLabels.add(alias);
                }

                return this;
            }
        }
    }

    private class PluginPlatformManager implements PlatformManager {
        private final Object registrationLock = new Object();
        private final PlatformManager platformManager;
        private final List<Platform> platforms = new LinkedList<>();

        private PluginPlatformManager(PlatformManager platformManager) {
            this.platformManager = platformManager;
        }

        @Override
        public void registerPlatform(Platform listener) {
            synchronized (registrationLock) {
                platformManager.registerPlatform(listener);
                platforms.add(listener);
            }
        }

        @Override
        public void unregisterPlatform(Platform listener) {
            synchronized (registrationLock) {
                if (!platforms.contains(listener)) return;

                platformManager.unregisterPlatform(listener);
                platforms.remove(listener);
            }
        }
        private void destroy() {
            synchronized (registrationLock) {
                platforms.forEach(this::unregisterPlatform);
            }
        }
    }

    private class PluginDatabaseManager implements DatabaseManager {
        private final SystemDatabaseManager databaseManager;

        private PluginDatabaseManager(SystemDatabaseManager databaseManager) {
            this.databaseManager = databaseManager;
        }

        private void destroy() {
            // Do nothing
        }

        @Override
        public Collection<Database> getDatabases() {
            return Collections.unmodifiableCollection(
                    databaseManager.getDatabases(artifact.getIdentifier().withoutVersion())
            );
        }

        @Override
        public boolean hasDatabase(String name) {
            return hasDatabase(getDbNamePrefix() + ":" + name);
        }

        @Override
        public Database getDatabase(String name) {
            return getDatabase(getDbNamePrefix() + ":" + name);
        }

        @Override
        public Database createDatabase(String name) {
            return databaseManager.createDatabase(
                    artifact.getIdentifier().withoutVersion(),
                    getDbNamePrefix() + ":" + name
            );
        }

        @Override
        public Database createDatabase(String name, DatabaseInitializer initializer) {
            return databaseManager.createDatabase(
                    artifact.getIdentifier().withoutVersion(),
                    getDbNamePrefix() + ":" + name,
                    initializer
            );
        }

        private String getDbNamePrefix() {
            ArtifactIdentifier identifier = getArtifact().getIdentifier();
            return identifier.getPackageId() + ":" + identifier.getArtifactId();
        }
    }
}
package com.github.manevolent.jbot.event.plugin;

import com.github.manevolent.jbot.plugin.Plugin;

public class PluginEnabledEvent extends PluginEvent {
    public PluginEnabledEvent(Object sender, Plugin plugin) {
        super(sender, plugin);
    }
}

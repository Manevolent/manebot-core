package com.github.manevolent.jbot.event.command;

import com.github.manevolent.jbot.command.executor.CommandExecutor;

public class CommandUnregisteredEvent extends CommandEvent {
    private final String label;

    public CommandUnregisteredEvent(Object sender,
                                    CommandExecutor executor,
                                    String label) {
        super(sender, executor);

        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

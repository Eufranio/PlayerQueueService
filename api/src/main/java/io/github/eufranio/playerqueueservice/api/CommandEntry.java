package io.github.eufranio.playerqueueservice.api;

public class CommandEntry {

    String command;
    boolean shouldSendAsPlayer;
    public CommandEntry(String command, boolean shouldSendAsPlayer) {
        this.command = command;
        this.shouldSendAsPlayer = shouldSendAsPlayer;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean shouldSendAsPlayer() {
        return this.shouldSendAsPlayer;
    }

}

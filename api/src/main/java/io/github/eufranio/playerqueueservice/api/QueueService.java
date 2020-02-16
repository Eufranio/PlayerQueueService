package io.github.eufranio.playerqueueservice.api;

import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.UUID;

public interface QueueService {

    List<Text> getQueuedMessages(UUID player);

    List<String> getRawQueuedMessages(UUID player);

    List<CommandEntry> getQueuedCommands(UUID player);

    void queueMessage(UUID player, Text message);

    void queueCommand(UUID player, String command, boolean asPlayer);

    void deleteMessage(UUID player, Text message);

    void deleteCommand(UUID player, CommandEntry command);

}

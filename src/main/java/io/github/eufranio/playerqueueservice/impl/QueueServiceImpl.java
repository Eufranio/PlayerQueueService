package io.github.eufranio.playerqueueservice.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import io.github.eufranio.playerqueueservice.api.CommandEntry;
import io.github.eufranio.playerqueueservice.api.QueueService;
import io.github.eufranio.storage.Persistable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class QueueServiceImpl implements QueueService {

    Persistable<PlayerQueue, UUID> players;

    public QueueServiceImpl(String databaseUrl) {
        this.players = Persistable.create(PlayerQueue.class, databaseUrl);
    }

    @Override
    public List<Text> getQueuedMessages(UUID player) {
        return this.getRawQueuedMessages(player).stream()
                .map(TextSerializers.JSON::deserialize)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRawQueuedMessages(UUID player) {
        PlayerQueue queue = this.players.get(player);
        if (queue == null)
            return Lists.newArrayList();
        return Lists.newArrayList(queue.messages);
    }

    @Override
    public List<CommandEntry> getQueuedCommands(UUID player) {
        PlayerQueue queue = this.players.get(player);
        if (queue == null)
            return Lists.newArrayList();
        return queue.commands.entrySet().stream()
                .map(e -> new CommandEntry(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void queueMessage(UUID player, Text message) {
        Player p = Sponge.getServer().getPlayer(player).orElse(null);
        if (p != null) {
            p.sendMessage(message);
            return;
        }

        PlayerQueue queue = this.players.getOrCreate(player);
        queue.messages.add(TextSerializers.JSON.serialize(message));
        this.players.save(queue);
    }

    @Override
    public void queueCommand(UUID player, String command, boolean asPlayer) {
        Player p = Sponge.getServer().getPlayer(player).orElse(null);
        if (p != null) {
            Sponge.getCommandManager().process(asPlayer ? p : Sponge.getServer().getConsole(), command);
            return;
        }

        PlayerQueue queue = this.players.getOrCreate(player);
        queue.commands.put(command, asPlayer);
        this.players.save(queue);
    }

    @Override
    public void deleteMessage(UUID player, Text message) {
        PlayerQueue queue = this.players.get(player);
        if (queue != null) {
            queue.messages.remove(TextSerializers.JSON.serialize(message));
        }
    }

    @Override
    public void deleteCommand(UUID player, CommandEntry command) {
        PlayerQueue queue = this.players.get(player);
        if (queue != null) {
            queue.commands.remove(command.getCommand());
        }
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player p) {
        PlayerQueue queue = this.players.get(p.getUniqueId());
        if (queue != null) {
            queue.commands.forEach((cmd, asPlayer) ->
                    Sponge.getCommandManager().process(asPlayer ? p : Sponge.getServer().getConsole(), cmd)
            );
            queue.messages.forEach(msg -> p.sendMessage(TextSerializers.JSON.deserialize(msg)));
            this.players.delete(queue);
        }
    }

    @DatabaseTable(tableName = "queues")
    public static class PlayerQueue extends BaseDaoEnabled<PlayerQueue, UUID> {

        @DatabaseField(id = true)
        private UUID id;

        @DatabaseField(dataType = DataType.SERIALIZABLE)
        private ArrayList<String> messages = new ArrayList<>();

        @DatabaseField(dataType = DataType.SERIALIZABLE)
        private HashMap<String, Boolean> commands = Maps.newHashMap(); // command -> boolean asPlayer

    }

}

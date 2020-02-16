package io.github.eufranio.playerqueueservice;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.eufranio.config.Config;
import io.github.eufranio.playerqueueservice.api.QueueService;
import io.github.eufranio.playerqueueservice.impl.QueueServiceImpl;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.util.List;

@Plugin(
        id = "playerqueueservice",
        name = "PlayerQueueService",
        description = "Adds a service to queue messages/commands to players",
        authors = {
                "Eufranio"
        }
)
public class PlayerQueueService {

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        Config<MainConfig> config = new Config<>(MainConfig.class, "PlayerQueueService.conf", configDir);

        QueueServiceImpl queueService = new QueueServiceImpl(config.get().databaseUrl);
        Sponge.getEventManager().registerListeners(this, queueService);
        Sponge.getServiceManager().setProvider(this, QueueService.class, queueService);

        CommandSpec queueMessage = CommandSpec.builder()
                .permission("playerqueueservice.queue.message")
                .arguments(
                        GenericArguments.user(Text.of("player")),
                        GenericArguments.text(Text.of("text"), TextSerializers.FORMATTING_CODE, true)
                )
                .executor((src, args) -> {
                    User user = args.requireOne("player");
                    queueService.queueMessage(user.getUniqueId(), args.requireOne("text"));
                    src.sendMessage(Text.of(TextColors.GREEN, "Successfully queued message!"));
                    return CommandResult.success();
                })
                .build();

        CommandSpec queueCommand = CommandSpec.builder()
                .permission("playerqueueservice.queue.command")
                .arguments(
                        GenericArguments.user(Text.of("player")),
                        GenericArguments.bool(Text.of("as player")),
                        GenericArguments.remainingJoinedStrings(Text.of("command"))
                )
                .executor((src, args) -> {
                    User user = args.requireOne("player");
                    queueService.queueCommand(user.getUniqueId(), args.requireOne("text"), args.requireOne("as player"));
                    src.sendMessage(Text.of(TextColors.GREEN, "Successfully queued command!"));
                    return CommandResult.success();
                })
                .build();

        CommandSpec queueInfo = CommandSpec.builder()
                .permission("playerqueueservice.info")
                .arguments(GenericArguments.userOrSource(Text.of("player")))
                .executor((src, args) -> {
                    User user = args.requireOne("player");

                    List<Text> info = Lists.newArrayList(Text.of(TextColors.YELLOW, "-- Queued Messages"));
                    queueService.getQueuedMessages(user.getUniqueId())
                            .forEach(s -> info.add(Text.of("    ", s)));
                    info.add(Text.EMPTY);
                    info.add(Text.of(TextColors.YELLOW, "-- Queued Commands"));

                    Text p = Text.of(TextColors.RED, TextActions.showText(Text.of("As player")), "[p]");

                    queueService.getQueuedCommands(user.getUniqueId()).forEach(cmd -> info.add(
                            Text.of(cmd.shouldSendAsPlayer() ? p : Text.of("   "), TextColors.GRAY, cmd.getCommand())
                    ));

                    PaginationList.builder()
                            .title(Text.of(TextColors.GREEN, user.getName(), "'s Queue"))
                            .contents(info)
                            .sendTo(src);
                    return CommandResult.success();
                })
                .build();

        CommandSpec queue = CommandSpec.builder()
                .permission("playerqueueservice.base")
                .executor((src, args) -> {
                    Sponge.getCommandManager().process(src, "queue info");
                    return CommandResult.success();
                })
                .child(queueInfo, "info")
                .child(queueMessage, "message")
                .child(queueCommand, "command")
                .build();

        Sponge.getCommandManager().register(this, queue, "queue");
    }

    @ConfigSerializable
    public static class MainConfig {

        @Setting
        public String databaseUrl = "jdbc:sqlite:PlayerQueueService.db";

    }


}

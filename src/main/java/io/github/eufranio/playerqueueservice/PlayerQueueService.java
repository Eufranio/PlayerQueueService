package io.github.eufranio.playerqueueservice;

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
import org.spongepowered.api.text.Text;

import java.io.File;

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

        CommandSpec queueServiceCommand = CommandSpec.builder()
                .permission("playerqueueservice.info")
                .arguments(GenericArguments.userOrSource(Text.of("player")))
                .executor((src, args) -> {
                    User user = args.requireOne("player");
                    Sponge.getServiceManager().provideUnchecked(QueueService.class)
                            .getInfo(user.getUniqueId())
                            .sendTo(src);
                    return CommandResult.success();
                })
                .build();
        Sponge.getCommandManager().register(this, queueServiceCommand, "queueserviceinfo");
    }

    @ConfigSerializable
    public static class MainConfig {

        @Setting
        public String databaseUrl = "jdbc:sqlite:PlayerQueueService.db";

    }


}

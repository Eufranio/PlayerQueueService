# PlayerQueueService
PlayerQueueService is a simple plugin that adds a QueueService to the game, so other plugins and server owners can queue commands/messages to players that run when the player joins the server. By default, this plugin adds commands to manually queue via the console/chat.

## Commands, permissions and descriptions
* /queue info <player> : playerqueueservice.info : shows the pending commands/messages from the specified player
* /queue message <player> <message> : playerqueueservice.queue.message : queues the specified message to the user, accepts colors/styles
* /queue command <player> <run as player (true/false)> <command> : playerqueueservice.queue.command : queues the specified command to the user, and depending of `run as user`, will run the command as the user or as the server console
        
## Configuration
You can configure the database URL that the plugin should use in `config/playerqueueservice/PlayerQueueService.conf`, by default it uses a db called `PlayerQueueService.db` in the root server directory

## Download
You can download this plugin from Ore: https://ore.spongepowered.org/Eufranio/PlayerQueueService/versions

## Plugin Developers
First, you have to add PlayerQueueService to your classpath, you can use Jitpack for that:
```groovy
dependencies {
    compileOnly 'com.github.Eufranio.PlayerQueueService:api:2.2'
}

repositories {
    ...
    maven { url = 'https://jitpack.io' }
}
```
Then you can get the [QueueService](https://github.com/Eufranio/PlayerQueueService/blob/master/api/src/main/java/io/github/eufranio/playerqueueservice/api/QueueService.java) instance, which holds all the queue management methods, via the Service Manager like this:
```java
    Sponge.getServiceManager().provide(QueueService.class).ifPresent(service -> {
            ... your code
    });
```

If you find any issues, report them to the [plugin's issue tracker](https://github.com/Eufranio/PlayerQueueService/issues). If you want to support my work, you can donate for me trough PayPal: **eufraniow@gmail.com**.
    

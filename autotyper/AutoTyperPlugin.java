package net.runelite.client.plugins.autotyper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Extension
@PluginDescriptor(
        name = "[W] Auto typer",
        enabledByDefault = false,
        description = "Auto types configured messages",
        tags = {"autotyper", "auto", "typer", "wilks"},
        type = PluginType.MISCELLANEOUS
)
public class AutoTyperPlugin extends Plugin {
    @Inject private Client client;
    @Inject private AutoTyperConfig config;

    boolean isStarted = false;
    int messageDelayInSeconds = 0;
    String message = "";
    boolean sendInClanChat = false;
    Instant lastSentMessageTimestamp = null;


    @Override
    protected void startUp() {
        logMessage("starting up");
    }

    @Override
    protected void shutDown() {
        logMessage("shutting down");
    }

    @Provides
    AutoTyperConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoTyperConfig.class);
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked event) {
        if (!event.getGroup().equalsIgnoreCase("wAutoTyper")) {
            return;
        }
        switch (event.getKey()) {
            case "startButton":
                if (isStarted) {
                    isStarted = false;
                } else {
                    isStarted = true;
                    messageDelayInSeconds = config.delay();
                    message = config.messageToSend();
                    sendInClanChat = config.sendToClanChat();
                }
                break;
            default:
                break;
        }
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equalsIgnoreCase("wAutoTyper")) {
            return;
        }
        messageDelayInSeconds = config.delay();
        sendInClanChat = config.sendToClanChat();
        message = config.messageToSend();
    }

    @Subscribe
    private void onGameTick(GameTick tick) {
        if (!isStarted) {
            return;
        }
        if (client != null && client.getGameState() == GameState.LOGGED_IN) {
            handleMessage(Instant.now());
        }
    }

    private void handleMessage(Instant now) {
        if (lastSentMessageTimestamp == null) {
            sendMessage();
            lastSentMessageTimestamp = Instant.now();
            return;
        }
        Duration duration = Duration.between(lastSentMessageTimestamp, now);
        logMessage("duration between them is " + duration.getSeconds());
        if (duration.getSeconds() > messageDelayInSeconds) {
            sendMessage();
            lastSentMessageTimestamp = now;
        }
    }

    private void sendMessage() {
        if (message.isEmpty()) {
            logMessage("message is empty");
            return;
        }
        String sanitizedMessage = new ChatMessageBuilder().append(message).build();
        if (sendInClanChat) {
            //hacky empty string sanitization
            client.runScript(ScriptID.CHATBOX_INPUT, 2, " " + sanitizedMessage);
        } else {
            client.runScript(ScriptID.PUBLICMSG, sanitizedMessage);
        }
    }

    private void logMessage(String message) {
        log.info("[Wilks Autotyper: " + message);
    }
}

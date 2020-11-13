package net.runelite.client.plugins.autotyper;

import net.runelite.client.config.*;

@ConfigGroup("wAutoTyper")
public interface AutoTyperConfig extends Config {
    @ConfigItem(
            keyName = "sendToClanChat",
            name = "Send messages in clan chat",
            description = "Sends autotyped messages in cc",
            position = 0
    )
    default boolean sendToClanChat() {
        return false;
    }

    @Range(min=0, max = 300)
    @ConfigItem(
            keyName = "delay",
            name = "Delay(s) between messages",
            description = "Delay in seconds between typed messages",
            position = 1
    )
    default int delay() {
        return 5;
    }

    @ConfigItem(
            keyName = "messageToSend",
            name = "Message to auto type",
            description = "Enter autotyped message here",
            position = 3
    )
    default String messageToSend() {
        return "Meow talk sux";
    }

    @ConfigItem(
            keyName = "startButton",
            name = "Start/Stop",
            description = "Start or stop the auto type",
            position = 4
    )
    default Button startButton() {
        return new Button();
    }
}
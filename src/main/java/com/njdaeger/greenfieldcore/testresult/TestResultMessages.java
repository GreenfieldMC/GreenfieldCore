package com.njdaeger.greenfieldcore.testresult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.function.Function;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class TestResultMessages {

    public static final TextComponent MODULE = moduleMessage("TestResult");

    public static final TextComponent ERROR_INVALID_PAGE = Component.text("That page does not exist.", NamedTextColor.RED);
    public static final String ERROR_PLAYER_OFFLINE = "This command requires the player to be online.";

    public static final TextComponent ATTEMPT_START = MODULE.append(Component.text("You may now begin your test, good luck!", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> ATTEMPT_START_SENDER = userName -> MODULE.append(Component.text("Test build for " + userName + " has started.", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> ATTEMPT_PASS = userName -> MODULE.append(Component.text("Welcome to the team, " + userName + "!", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> ATTEMPT_PASS_SENDER = userName -> MODULE.append(Component.text("Latest test build for " + userName + " was successful.", NamedTextColor.GRAY));
    public static final TextComponent ATTEMPT_FAIL = MODULE.append(Component.text("Unfortunately, your latest test build attempt was unsuccessful. Please follow guidance from our staff.", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> ATTEMPT_FAIL_SENDER = userName -> MODULE.append(Component.text("Latest test build for " + userName + " was unsuccessful.", NamedTextColor.GRAY));
    public static final TextComponent ATTEMPT_FAIL_FINAL = MODULE.append(Component.text("Unfortunately, your test builds were not successful. Thank you for applying, we hope to see you apply to the server again in the future!", NamedTextColor.GRAY).appendNewline().appendNewline().append(Component.text("[You will be automatically kicked in 10 seconds]", NamedTextColor.GRAY, TextDecoration.BOLD)));
    public static final Function<String, TextComponent> ATTEMPT_FAIL_FINAL_SENDER = userName -> MODULE.append(Component.text("Latest test build for " + userName + " was unsuccessful. Kicking in 10 seconds.", NamedTextColor.GRAY));

    public static final TextComponent KICK_FAIL_MESSAGE = Component.text("You can re-apply again to get another chance at joining! We're glad we could meet you.");
}

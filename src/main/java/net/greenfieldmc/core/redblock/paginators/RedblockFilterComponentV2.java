package net.greenfieldmc.core.redblock.paginators;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.components.IComponent;
import net.greenfieldmc.core.CommonMessages;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.redblock.Redblock;
import net.greenfieldmc.core.redblock.RedblockConstants;
import net.greenfieldmc.core.redblock.arguments.StatusListArgument;
import net.greenfieldmc.core.redblock.services.IRedblockConfigService;
import net.greenfieldmc.core.shared.services.IUserService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class RedblockFilterComponentV2 implements IComponent<GfRedblock, Pair<ICommandContext, IUserService>> {

    private final IRedblockConfigService configService;
    private final IUserService userService;

    public RedblockFilterComponentV2(IRedblockConfigService configService, IUserService userService) {
        this.configService = configService;
        this.userService = userService;
    }

    @Override
    public TextComponent getText(Pair<ICommandContext, IUserService> context, ChatPaginator<GfRedblock, Pair<ICommandContext, IUserService>> paginator, List<GfRedblock> results, int currentPage, int totalResults) {

        try {
            var projectKey = context.getFirst().getTyped("project", World.class, context.getFirst().getLocation().getWorld());

            var statuses = context.getFirst().<List<String>>getFlag("status", List.of());

            var unassigned = context.getFirst().hasFlag("unassigned");
            var mine = context.getFirst().hasFlag("mine");

            var assignedToAny = context.getFirst().<List<GfUser>>getFlag("assignedTo");
            var assignedToExactly = context.getFirst().<List<GfUser>>getFlag("assignedToExact");
            var assignedToAll = context.getFirst().<List<GfUser>>getFlag("assignedToAll");

            List<GfUser> userList = mine ? List.of(resolveUser(context.getFirst()))
                    : assignedToAny != null ? assignedToAny
                    : assignedToExactly != null ? assignedToExactly
                    : assignedToAll != null ? assignedToAll
                    : List.of();

            var roleAny = context.getFirst().<List<String>>getFlag("role");
            var roleExactly = context.getFirst().<List<String>>getFlag("roleExact");
            var roleAll = context.getFirst().<List<String>>getFlag("roleAll");

            var roleList = roleAny != null ? roleAny
                    : roleExactly != null ? roleExactly
                    : roleAll != null ? roleAll
                    : List.of();

            @SuppressWarnings("DataFlowIssue")
            int radius = context.getFirst().getFlag("radius", -1);

            Supplier<TextComponent> hoverText = () -> {
                var base = Component.text();
                base.append(Component.text("Project: ", NamedTextColor.GRAY)).append(Component.text(configService.getProjectKey(projectKey), NamedTextColor.BLUE));
                if (!statuses.isEmpty()) {
                    base.appendNewline().append(Component.text("Status Shown: ", NamedTextColor.GRAY));
                    for (var status : statuses) {
                        base.appendNewline().append(Component.text("- " + status.toUpperCase(), NamedTextColor.BLUE));
                    }
                }

                if (unassigned) base.appendNewline().append(Component.text("Assigned to: ", NamedTextColor.GRAY)).append(Component.text("No one", NamedTextColor.BLUE, TextDecoration.ITALIC));
                else if (mine) base.appendNewline().append(Component.text("Assigned to: ", NamedTextColor.GRAY)).append(Component.text("You", NamedTextColor.BLUE));
                else if (!userList.isEmpty()) {
                    base.appendNewline().append(Component.text("Assigned to: ", NamedTextColor.GRAY));
                    for (var user : userList) {
                        base.appendNewline().append(Component.text("- " + user.getUsername(), NamedTextColor.BLUE));
                    }
                }

                if (!roleList.isEmpty()) {
                    base.appendNewline().append(Component.text("Roles: ", NamedTextColor.GRAY));
                    for (var role : roleList) {
                        base.appendNewline().append(Component.text("- " + role, NamedTextColor.BLUE));
                    }
                }

                if (radius != -1) base.appendNewline().append(Component.text("Radius: ", NamedTextColor.GRAY)).append(Component.text(String.valueOf(radius), NamedTextColor.BLUE));
                return base.build();
            };
            return Component.text("Filter", NamedTextColor.LIGHT_PURPLE).hoverEvent(HoverEvent.showText(hoverText.get()));

        } catch (PDKCommandException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private GfUser resolveUser(ICommandContext ctx) throws PDKCommandException {
        var user = !ctx.isPlayer()
                ? userService.getSystemUser()
                : userService.getUserByUuid(ctx.asPlayer().getUniqueId());

        if (user == null) ctx.error(CommonMessages.ERROR_USER_NOT_FOUND.apply(!ctx.isPlayer() ? "System" : ctx.asPlayer().getName()));
        return user;
    }
}

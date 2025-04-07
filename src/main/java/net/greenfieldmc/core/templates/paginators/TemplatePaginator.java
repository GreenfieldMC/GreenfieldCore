package net.greenfieldmc.core.templates.paginators;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.greenfieldmc.core.Triple;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

public class TemplatePaginator extends ChatPaginatorBuilder<Template, Triple<TemplatePaginator.TemplatePaginatorMode, ICommandContext, TemplateBrush>> {

    public TemplatePaginator(TemplatePaginatorMode mode) {
        super();
        // Set the paginator title
        addComponent(Component.text("Template List", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        
        // Add the result count component in the top left
        addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT);
        
        // Add a filter component in the top right to show filtering information
        addComponent(this::createFilterComponent, ComponentPosition.TOP_RIGHT);
        
        // Add page navigation
        addComponent(new PageNavigationComponent<>(
                (ctx, res, pg) -> "/tlist" + ctx.getFirst() + " -page 1",
                (ctx, res, pg) -> "/tlist" + ctx.getFirst() + " -page " + (pg - 1),
                (ctx, res, pg) -> "/tlist" + ctx.getFirst() + " -page " + (pg + 1),
                (ctx, res, pg) -> "/tlist" + ctx.getFirst() + " -page " + ((int) Math.ceil(res.size() / (ctx.getFirst() == TemplatePaginatorMode.BRUSH_MODIFY ? 4.0 : 8.0)))
        ), ComponentPosition.BOTTOM_CENTER);
        
        // Add a find button at the bottom right
        addComponent((ctx, paginator, results, pg) -> Component.text("[☀]", paginator.getHighlightColor())
                .hoverEvent(HoverEvent.showText(Component.text("Search for a template", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.suggestCommand("/tlist \""))
        , ComponentPosition.BOTTOM_RIGHT);
        
        // Line wrapping mode
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
        setResultsPerPage(mode == TemplatePaginatorMode.BRUSH_MODIFY ? 4 : 8);
    }

    private TextComponent createFilterComponent(Triple<TemplatePaginatorMode, ICommandContext, TemplateBrush> templatePaginatorModeICommandContextTemplateBrushTriple, ChatPaginator<Template, Triple<TemplatePaginatorMode, ICommandContext, TemplateBrush>> templateTripleChatPaginator, List<Template> templates, int i) {
        var mode = templatePaginatorModeICommandContextTemplateBrushTriple.getFirst();
        var filterText = mode == TemplatePaginatorMode.LIST ? "No Filter" : "Brush Mode";

        return Component.text("Query", NamedTextColor.LIGHT_PURPLE, TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(Component.text(filterText, NamedTextColor.GRAY)));
    }

    public enum TemplatePaginatorMode {
        LIST,
        BRUSH_MODIFY
    }
}

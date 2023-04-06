package com.njdaeger.greenfieldcore.codes;

import com.google.common.base.Strings;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickEvent;
import com.njdaeger.pdk.utils.text.click.ClickString;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import java.util.List;

import static org.bukkit.ChatColor.*;

public class Codes {

    private String codesString;
    private int size;

    public Codes(CodesConfig config) {
        reload(config.getCodes());
    }

    public void reload(List<String> codes) {
        StringBuilder codesBuilder = new StringBuilder();
        int start = 1;
        for (String code : codes) {
            codesBuilder.append(GRAY).append(start).append(") ").append(WHITE).append(code).append("\n");
            start++;
        }
        this.codesString = codesBuilder.toString();
        this.size = ChatPaginator.paginate(this.codesString, 1, 55, 8).getTotalPages();
    }

    public void sendTo(CommandSender sender, int page) {
        if (page <= 0 || page > size) {
            sender.sendMessage(RED + "No more pages to display.");
            return;
        }
        ChatPaginator.ChatPage chatPage = ChatPaginator.paginate(codesString, page, 55, 8);
        sender.sendMessage(GRAY + "=== " + LIGHT_PURPLE + "[Codes]" + GRAY + " ==================== Page: " + LIGHT_PURPLE + page + "/" + chatPage.getTotalPages() + GRAY + " ===");
        int padding = 1;
        for (String line : chatPage.getLines()) {
            if (line.startsWith(COLOR_CHAR + "7")) {
                padding = line.substring(2, line.indexOf(")")).length();
                sender.sendMessage(" " + line);
            }
            else sender.sendMessage("   " + Strings.repeat(" ", padding) + line);
        }
        if (sender instanceof Player) {
            var outOfRange = new ClickEvent<>(ClickAction.RUN_COMMAND, ClickString.of("/codes -1"));
            Text.of("===== ").setColor(GRAY)
                .appendRoot(page <= 1 ? "|X|" : "<<").setColor(LIGHT_PURPLE).setBold(true)
                .setClickEvent(page <= 1 ? outOfRange : new ClickEvent<>(ClickAction.RUN_COMMAND, ClickString.of("/codes " + (page - 1))))
                .appendRoot(" ========================== ").setColor(GRAY)
                .appendRoot(page >= chatPage.getTotalPages() ? "|X|" : ">>").setColor(LIGHT_PURPLE).setBold(true)
                .setClickEvent(page >= chatPage.getTotalPages() ? outOfRange : new ClickEvent<>(ClickAction.RUN_COMMAND, ClickString.of("/codes " + (page + 1))))
                .appendRoot(" =====").setColor(GRAY).sendTo(sender);
        }
    }

    public int getTotalPages() {
        return size;
    }

}

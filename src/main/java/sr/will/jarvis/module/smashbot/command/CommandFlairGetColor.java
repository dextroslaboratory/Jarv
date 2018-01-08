package sr.will.jarvis.module.smashbot.command;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import sr.will.jarvis.command.Command;
import sr.will.jarvis.module.smashbot.ModuleSmashBot;

import java.awt.*;

public class CommandFlairGetColor extends Command {
    private ModuleSmashBot module;

    public CommandFlairGetColor(ModuleSmashBot module) {
        super("flairgetcolor", "flairgetcolor [user mention]", "Get the color of a user's flair", module);
        this.module = module;
    }

    @Override
    public void execute(Message message, String... args) {
        checkModuleEnabled(message, module);
        checkBotPermission(message, Permission.MANAGE_ROLES);

        User user = message.getAuthor();
        if (getMentionedUser(message, args) != null) {
            user = getMentionedUser(message, args);
        }

        Member member = message.getGuild().getMember(user);
        Color color = module.getMemberFlair(member).color;

        sendSuccessMessage(message, module.getHexFromColor(color), false);
    }
}
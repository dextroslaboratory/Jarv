package sr.will.jarvis.module.smashbot.command;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import sr.will.jarvis.Jarvis;
import sr.will.jarvis.command.Command;
import sr.will.jarvis.module.smashbot.ModuleSmashBot;

import java.awt.*;
import java.util.HashMap;

public class CommandFlairList extends Command {
    private ModuleSmashBot module;

    public CommandFlairList(ModuleSmashBot module) {
        super("commandflairlist", "commandflairlist", "List all flairs and owners", module);
        this.module = module;
    }

    @Override
    public void execute(Message message, String... args) {
        checkModuleEnabled(message, module);

        long guildId = message.getGuild().getIdLong();
        HashMap<Long, String> memberFlairs = module.getMemberFlairs(guildId);

        if (memberFlairs.size() == 0) {
            message.getChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("No flairs registered").build()).queue();
            return;
        }

        int maxLen = 0;

        for (long memberId : memberFlairs.keySet()) {
            String name = Jarvis.getJda().getUserById(memberId).getName();
            maxLen = Math.max(name.length(), maxLen);
        }

        StringBuilder stringBuilder = new StringBuilder();

        int pos = 0;
        for (long memberId : memberFlairs.keySet()) {
            String name = Jarvis.getJda().getUserById(memberId).getName();
            stringBuilder.append('`').append(name).append(getFiller(maxLen - name.length())).append('`');
            stringBuilder.append(' ').append(memberFlairs.get(memberId));
            stringBuilder.append('\n');

            if (pos % 10 == 0 || pos == memberFlairs.size()) {
                EmbedBuilder embed = new EmbedBuilder().setColor(Color.GREEN);
                embed.setDescription(stringBuilder.toString());
                message.getChannel().sendMessage(embed.build()).queue();
                stringBuilder = new StringBuilder();
            }

            pos += 1;
        }
    }
}

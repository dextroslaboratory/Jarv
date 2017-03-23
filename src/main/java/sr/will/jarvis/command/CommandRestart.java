package sr.will.jarvis.command;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import sr.will.jarvis.Jarvis;

import java.awt.*;

public class CommandRestart extends Command {
    private Jarvis jarvis;

    public CommandRestart(Jarvis jarvis) {
        this.jarvis = jarvis;
    }

    @Override
    public void execute(Message message, String... args) {
        // Only allow the bot owner to restart the bot
        if (!jarvis.config.discord.owners.contains(message.getAuthor().getId())) {
            message.getChannel().sendMessage(new EmbedBuilder().setTitle("Error", "https://jarvis.will.sr").setColor(Color.RED).setDescription("You don't have permission for that").build()).queue();
            return;
        }

        for (User user : message.getMentionedUsers()) {
            if (user.getId().equals(message.getJDA().getSelfUser().getId())) {
                message.getChannel().sendMessage(new EmbedBuilder().setTitle("Success", "https://jarvis.will.sr").setColor(Color.GREEN).setDescription("Restarting...").build()).queue();
                jarvis.stop();
                return;
            }
        }
    }
}

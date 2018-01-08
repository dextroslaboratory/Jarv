package sr.will.jarvis.module.smashbot.command;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import sr.will.jarvis.command.Command;
import sr.will.jarvis.module.smashbot.ModuleSmashBot;

public class CommandFlair extends Command {
    ModuleSmashBot module;


    public CommandFlair(ModuleSmashBot module) {
        super("flair", "flair", "Flair help command", module);
        this.module = module;
    }

    @Override
    public void execute(Message message, String... args) {
        checkModuleEnabled(message, module);

        message.getChannel().sendMessage(new EmbedBuilder().setTitle("Flair Help", null)
                .addField("flairsetname <name>", "Rename your flair to this name", false)
                .addField("flairsetcolor <color|hex code>", "Change your flair color", false)
                .addField("flairgetcolor [user mention]", "Get the color of a user's flair", false)
                .addField("flairlist", "List all flairs and their owners", false)
                .build()).queue();
    }
}

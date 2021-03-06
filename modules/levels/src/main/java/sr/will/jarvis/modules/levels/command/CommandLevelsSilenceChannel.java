package sr.will.jarvis.modules.levels.command;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import sr.will.jarvis.command.Command;
import sr.will.jarvis.modules.levels.ModuleLevels;

public class CommandLevelsSilenceChannel extends Command {
    private ModuleLevels module;

    public CommandLevelsSilenceChannel(ModuleLevels module) {
        super("levelssilencechannel", "levelssilencechannel [channelId]", "Silences or unsilences channels. Silencing prevents level up messages from appearing in specified channels", module);
        this.module = module;
    }

    @Override
    public void execute(Message message, String... args) {
        checkModuleEnabled(message, module);
        checkUserPermission(message, Permission.MANAGE_CHANNEL);

        MessageChannel channel = message.getChannel();
        if (getMentionedChannel(message, args) != null) {
            channel = getMentionedChannel(message, args);
        }

        if (module.channelSilenced(channel.getIdLong())) {
            module.unsilenceChannel(channel.getIdLong());
            sendSuccessMessage(message, "Channel " + channel.getName() + " has been unsilenced");
        } else {
            module.silenceChannel(channel.getIdLong());
            sendSuccessMessage(message, "Channel " + channel.getName() + " has been silenced");
        }
    }
}

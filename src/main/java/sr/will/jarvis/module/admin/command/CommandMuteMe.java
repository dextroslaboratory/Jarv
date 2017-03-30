package sr.will.jarvis.module.admin.command;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.noxal.common.util.DateUtils;
import sr.will.jarvis.command.Command;
import sr.will.jarvis.module.admin.ModuleAdmin;
import sr.will.jarvis.util.CommandUtils;

public class CommandMuteMe extends Command {
    private ModuleAdmin module;

    public CommandMuteMe(ModuleAdmin module) {
        this.module = module;
    }

    @Override
    public void execute(Message message, String... args) {
        User user = message.getAuthor();

        if (args.length == 0) {
            CommandUtils.sendSuccessEmote(message);
            module.muteManager.mute(message.getGuild().getId(), user.getId(), message.getAuthor().getId());
            return;
        }

        long duration = 0;

        try {
            duration = DateUtils.parseDateDiff(args[0], true);
        } catch (Exception e) {
            CommandUtils.sendFailureMessage(message, "Invalid time");
            return;
        }

        CommandUtils.sendSuccessMessage(message, user.getAsMention() + " has been muted for " + DateUtils.formatDateDiff(duration));
        module.muteManager.mute(message.getGuild().getId(), user.getId(), message.getAuthor().getId(), duration);
    }
}
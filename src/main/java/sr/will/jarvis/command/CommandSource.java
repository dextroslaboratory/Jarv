package sr.will.jarvis.command;

import net.dv8tion.jda.core.entities.Message;

public class CommandSource extends Command {
    @Override
    public void execute(Message message, String... args) {
        sendSuccessMessage(message, "https://github.com/Willsr71/Jarvis", false);
    }
}

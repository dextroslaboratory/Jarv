package sr.will.jarvis.modules.levels;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.noxal.common.util.DateUtils;
import sr.will.jarvis.Jarvis;
import sr.will.jarvis.module.Module;
import sr.will.jarvis.modules.levels.cache.CachedLevelsIgnoredChannels;
import sr.will.jarvis.modules.levels.cache.CachedUserXp;
import sr.will.jarvis.modules.levels.command.*;
import sr.will.jarvis.modules.levels.event.EventHandlerLevels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ModuleLevels extends Module {
    private HashMap<Integer, Long> levels = new HashMap<>();
    private ArrayList<RecentMessage> recentMessages = new ArrayList<>();

    public void initialize() {
        setNeededPermissions(
                Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE
        );
        setDefaultEnabled(false);

        registerEventHandler(new EventHandlerLevels(this));

        registerCommand("importmee6", new CommandImportMee6(this));
        registerCommand("levels", new CommandLevels(this));
        registerCommand("levelsignorechannel", new CommandLevelsIgnoreChannel(this));
        registerCommand("levelsoptin", new CommandLevelsOptIn(this));
        registerCommand("levelsoptout", new CommandLevelsOptOut(this));
        registerCommand("levelssilencechannel", new CommandLevelsSilenceChannel(this));
        registerCommand("rank", new CommandRank(this));

        generateLevels(100);
    }

    public void finishStart() {
        Jarvis.getDatabase().execute("CREATE TABLE IF NOT EXISTS levels(" +
                "id int NOT NULL AUTO_INCREMENT," +
                "guild bigint(20) NOT NULL," +
                "user bigint(20) NOT NULL," +
                "xp bigint(20) NOT NULL DEFAULT 0," +
                "PRIMARY KEY (id));");
        Jarvis.getDatabase().execute("CREATE TABLE IF NOT EXISTS levels_ignored_channels(" +
                "id int NOT NULL AUTO_INCREMENT," +
                "channel bigint(20) NOT NULL," +
                "PRIMARY KEY (id));");
        Jarvis.getDatabase().execute("CREATE TABLE IF NOT EXISTS levels_silenced_channels(" +
                "id int NOT NULL AUTO_INCREMENT," +
                "channel bigint(20) NOT NULL," +
                "PRIMARY KEY (id));");
    }

    public void stop() {

    }

    public void reload() {

    }

    public void addUser(long guildId, long userId, long xp) {
        Jarvis.getDatabase().execute("INSERT INTO levels (guild, user, xp) VALUES (?, ?, ?);", guildId, userId, xp);
        new CachedUserXp(guildId, userId, xp);
    }

    public void setUserXp(long guildId, long userId, long xp) {
        Jarvis.getDatabase().execute("UPDATE levels SET xp = ? WHERE (guild = ? AND user = ?);", xp, guildId, userId);
        new CachedUserXp(guildId, userId, xp);
    }

    public XPUser getXPUser(long guildId, long userId) {
        HashMap<Integer, XPUser> leaderboard = getLeaderboard(guildId);

        for (int pos : leaderboard.keySet()) {
            XPUser user = leaderboard.get(pos);

            if (user.userId == userId) {
                return user;
            }
        }

        return null;
    }

    public long getUserXp(long guildId, long userId) {
        CachedUserXp c = CachedUserXp.getEntry(guildId, userId);
        if (c != null) {
            return c.getXp();
        }

        try {
            ResultSet result = Jarvis.getDatabase().executeQuery("SELECT xp FROM levels WHERE (guild = ? AND user = ?);", guildId, userId);
            if (result.first()) {
                new CachedUserXp(guildId, userId, result.getLong("xp"));
                return result.getLong("xp");
            } else {
                new CachedUserXp(guildId, userId, 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean userExists(long guildId, long userId) {
        CachedUserXp c = CachedUserXp.getEntry(guildId, userId);
        if (c != null) {
            return true;
        }

        try {
            ResultSet result = Jarvis.getDatabase().executeQuery("SELECT 1 FROM levels WHERE (guild = ? AND user = ?);", guildId, userId);
            return result.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public HashMap<Integer, XPUser> getLeaderboard(long guildId) {
        HashMap<Integer, XPUser> leaderboard = new HashMap<>();

        try {
            ResultSet result = Jarvis.getDatabase().executeQuery("SELECT user, xp, (SELECT COUNT(id) FROM levels WHERE (guild = ? AND xp >= 0)) AS pos_total FROM levels WHERE (guild = ? AND xp >= 0) ORDER BY xp DESC;", guildId, guildId);

            int pos = 1;
            while (result.next()) {
                leaderboard.put(pos, new XPUser(this, guildId, result.getLong("user"), result.getLong("xp"), pos, result.getInt("pos_total")));
                pos += 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return leaderboard;
    }

    public void ignoreChannel(long channelId) {
        Jarvis.getDatabase().execute("INSERT INTO levels_ignored_channels (channel) VALUES (?);", channelId);
        new CachedLevelsIgnoredChannels(channelId, true);
    }

    public void unignoreChannel(long channelId) {
        Jarvis.getDatabase().execute("DELETE FROM levels_ignored_channels WHERE (channel = ?);", channelId);
        new CachedLevelsIgnoredChannels(channelId, false);
    }

    public boolean channelIgnored(long channelId) {
        CachedLevelsIgnoredChannels c = CachedLevelsIgnoredChannels.getEntry(channelId);
        if (c != null) {
            return c.isIgnored();
        }

        try {
            ResultSet result = Jarvis.getDatabase().executeQuery("SELECT 1 FROM levels_ignored_channels WHERE (channel = ?);", channelId);
            new CachedLevelsIgnoredChannels(channelId, result.first());
            return result.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void silenceChannel(long channelId) {
        Jarvis.getDatabase().execute("INSERT INTO levels_silenced_channels (channel) VALUES (?);", channelId);
    }

    public void unsilenceChannel(long channelId) {
        Jarvis.getDatabase().execute("DELETE FROM levels_silenced_channels WHERE (channel = ?);", channelId);
    }

    public boolean channelSilenced(long channelId) {
        try {
            ResultSet result = Jarvis.getDatabase().executeQuery("SELECT 1 FROM levels_silenced_channels WHERE (channel = ?);", channelId);
            return result.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void increase(Message message) {
        long guildId = message.getGuild().getIdLong();
        long channelId = message.getChannel().getIdLong();
        long userId = message.getAuthor().getIdLong();

        if (!isEnabled(guildId)) {
            return;
        }

        if (channelIgnored(channelId)) {
            return;
        }

        if (!userExists(guildId, userId)) {
            addUser(guildId, userId, 0);
        }

        long xp = getUserXp(guildId, userId);

        // User is opted out
        if (xp < 0) {
            return;
        }

        long timestamp = message.getCreationTime().toInstant().toEpochMilli();
        int messageLength = message.getContentDisplay().length();

        recentMessages.add(new RecentMessage(guildId, channelId, userId, timestamp, messageLength));
        int messageLengthAverage = getRecentMessageAverage();
        long timeFromLast = getTimeFromLastMessage(guildId, userId, timestamp);

        double baseAmount = 20.0;
        double lengthMultiplier = (double) messageLength / (double) messageLengthAverage;
        double timeMultiplier = ((double) timeFromLast / 1000.0) / 60.0;
        double correctedTimeMultiplier = Math.min(1.0, timeMultiplier);

        //System.out.println("length: " + lengthMultiplier);
        //System.out.println("time:   " + timeMultiplier);
        //System.out.println("timeco: " + correctedTimeMultiplier);

        long xpGained = Math.round(baseAmount * lengthMultiplier * correctedTimeMultiplier);

        //System.out.println("gained: " + xpGained);

        Jarvis.getDatabase().execute("UPDATE levels SET xp = xp + ? WHERE (guild = ? AND user = ?);", xpGained, guildId, userId);

        if (getLevelFromXp(xp + xpGained) > getLevelFromXp(xp) && !channelSilenced(message.getChannel().getIdLong())) {
            message.getChannel().sendMessage("Congratulations! " + message.getJDA().getUserById(userId).getAsMention() + " has reached level " + getLevelFromXp(xp + xpGained)).queue();
        }
    }

    public void removeExpiredRecentMessages() {
        for (int x = 0; x < recentMessages.size(); x += 1) {
            RecentMessage message = recentMessages.get(x);
            long expires = message.timestamp + (60 * 60 * 1000);
            if (!DateUtils.timestampApplies(expires)) {
                recentMessages.remove(x);
                x -= 1;
            }
        }
    }

    public int getRecentMessageAverage() {
        removeExpiredRecentMessages();

        int total = 0;
        for (RecentMessage message : recentMessages) {
            total += message.messageLength;
        }

        return Math.floorDiv(total, recentMessages.size());
    }

    public long getTimeFromLastMessage(long guildId, long userId, long timestamp) {
        long timeFromLast = (60 * 60 * 1000);
        for (RecentMessage message : recentMessages) {
            if (message.guildId != guildId || message.userId != userId) {
                continue;
            }

            if (message.timestamp == timestamp) {
                continue;
            }

            timeFromLast = timestamp - message.timestamp;
        }

        return timeFromLast;
    }

    private void generateLevels(int size) {
        int start = 0;
        while (levels.containsKey(start)) {
            start += 1;
        }

        generateLevels(start, size);
    }

    private void generateLevels(int start, int end) {
        if (start == 0) {
            levels.put(0, 0L);
            start = 1;
        }

        for (int x = start; x <= end; x += 1) {
            //long xp = (x * 100) + levels.get(x - 1);
            long xp = ((5 * (x ^ 2)) + (50 * x) + 35) + levels.get(x - 1);
            //double level = (double) x;
            //long xp = Math.round((5.0 / 6.0) * level * (2.0 * level * level + (27.0 * level) + 91.0));
            levels.put(x, xp);
            //System.out.println(x + " = " + levels.get(x));
        }
    }

    public long getLevelXp(int level) {
        if (!levels.containsKey(level)) {
            generateLevels(level);
        }

        return levels.get(level);
    }

    public int getLevelFromXp(long xp) {
        int level = 0;
        while (xp >= getLevelXp(level + 1)) {
            level += 1;
        }

        return level;
    }
}

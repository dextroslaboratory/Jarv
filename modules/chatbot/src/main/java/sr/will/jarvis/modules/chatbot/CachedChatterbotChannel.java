package sr.will.jarvis.modules.chatbot;

import sr.will.jarvis.Jarvis;
import sr.will.jarvis.cache.Cache;
import sr.will.jarvis.cache.CacheEntry;

public class CachedChatterbotChannel extends CacheEntry {
    public long channelId;
    private boolean chatterbotChannel;

    public CachedChatterbotChannel(long channelId, boolean chatterbotChannel) {
        this.channelId = channelId;
        this.chatterbotChannel = chatterbotChannel;
        initialize(Jarvis.getInstance().config.cache.timeouts.chatterbotChannels);
    }

    public boolean isChatterbotChannel() {
        updateLastUsed();
        return chatterbotChannel;
    }

    public static CachedChatterbotChannel getEntry(long channelId) {
        for (CachedChatterbotChannel cachedChatterbotChannel : Cache.getByType(CachedChatterbotChannel.class)) {
            if (cachedChatterbotChannel.channelId == channelId) {
                return cachedChatterbotChannel;
            }
        }

        return null;
    }

    public boolean fieldsMatch(CacheEntry entry) {
        CachedChatterbotChannel c = (CachedChatterbotChannel) entry;
        return channelId == c.channelId;
    }
}

package sr.will.jarvis.modules.admin;

import sr.will.jarvis.Jarvis;
import sr.will.jarvis.cache.Cache;
import sr.will.jarvis.cache.CacheEntry;

public class CachedMute extends CacheEntry {
    public long guildId;
    public long userId;
    private long duration;

    public CachedMute(long guildId, long userId, long duration) {
        this.guildId = guildId;
        this.userId = userId;
        this.duration = duration;
        initialize(Jarvis.getInstance().config.cache.timeouts.mute);
    }

    public long getDuration() {
        updateLastUsed();
        return duration;
    }

    public static CachedMute getEntry(long guildId, long userId) {
        for (CachedMute cachedMute : Cache.getByType(CachedMute.class)) {
            if (cachedMute.guildId == guildId && cachedMute.userId == userId) {
                return cachedMute;
            }
        }

        return null;
    }

    public boolean fieldsMatch(CacheEntry entry) {
        CachedMute c = (CachedMute) entry;
        return guildId == c.guildId && userId == c.userId;
    }
}

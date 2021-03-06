package sr.will.jarvis.module;

import sr.will.jarvis.Jarvis;
import sr.will.jarvis.cache.Cache;
import sr.will.jarvis.cache.CacheEntry;

public class CachedModule extends CacheEntry {
    public long guildId;
    public String module;
    private boolean enabled;

    public CachedModule(long guildId, String module, boolean enabled) {
        this.guildId = guildId;
        this.module = module;
        this.enabled = enabled;
        initialize(Jarvis.getInstance().config.cache.timeouts.module);
    }

    public boolean moduleEnabled() {
        updateLastUsed();
        return enabled;
    }

    public static CachedModule getEntry(long guildId, String module) {
        for (CachedModule cachedModule : Cache.getByType(CachedModule.class)) {
            if (cachedModule.guildId == guildId && cachedModule.module.equals(module)) {
                return cachedModule;
            }
        }

        return null;
    }

    public boolean fieldsMatch(CacheEntry entry) {
        CachedModule c = (CachedModule) entry;
        return guildId == c.guildId && module.equals(c.module);
    }
}
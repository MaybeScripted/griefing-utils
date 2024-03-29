package griefingutils.utils;

import java.util.Collection;

public enum WhitelistEnum {
    Whitelist,
    Blacklist;

    public <T> boolean isBlacklisted(Collection<T> collection, T entry) {
        return !isWhitelisted(collection, entry);
    }

    public <T> boolean isWhitelisted(Collection<T> collection, T entry) {
        return (this == Whitelist) == collection.contains(entry);
    }
}

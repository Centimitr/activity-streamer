import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

// 879849: here MessageCache is not automatically cleaned, it should be cleaned base on timeout and the scale the application should be

class MessageCacheComparator implements Comparator<MessageCacheItem> {
    @Override
    public int compare(MessageCacheItem a, MessageCacheItem b) {
        return Integer.compare(a.index, b.index);
    }
}

class MessageCacheItem {
    String username;
    int index;
    String message;

    MessageCacheItem(String username, int index, String message) {
        this.username = username;
        this.index = index;
        this.message = message;
    }
}

class MessageCache {
    private ConcurrentHashMap<String, ArrayList<MessageCacheItem>> map = new ConcurrentHashMap<>();

    void put(String sender, int index, String message) {
        if (!map.containsKey(sender)) {
            map.put(sender, new ArrayList<>());
        }
        ArrayList<MessageCacheItem> list = map.get(sender);
        list.add(new MessageCacheItem(sender, index, message));
    }

    ArrayList<String> pop(String sender) {
        ArrayList<String> messages = new ArrayList<>();
        ArrayList<MessageCacheItem> list = map.get(sender);
        list.sort(new MessageCacheComparator());
        for (MessageCacheItem item : list) {
            messages.add(item.message);
        }
        map.remove(sender);
        return messages;
    }
}

class MessageCounter {
    private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    int getIndex(String username) {
        if (!map.containsKey(username)) {
            map.put(username, 0);
        }
        int index = map.get(username);
        map.put(username, index++);
        return index;
    }
}
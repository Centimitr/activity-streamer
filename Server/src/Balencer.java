import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class BalencerRecord {
    String id;
    String hostname;
    Integer port;
    Integer load;

    Record(String id, String hostname, Integer port, Integer load) {
        this.id = id;
        update(hostname, port, load);
    }

    void update(String hostname, Integer port, Integer load) {
        this.hostname = hostname;
        this.port = port;
        this.load = load;
    }
}

public class Balencer {
    static Integer calcAvailableLoad(Integer load) {
        return load - 2;
    }

    public Integer rule = 0;

    private Map<String, BalencerRecord> records = new HashMap<>();
    int a;

    boolean has(String id) {
        return records.containsKey(id);
    }

    BalencerRecord get(String id) {
        return records.get(id);
    }

    void put(String id, String hostname, Integer port, Integer load) {
        if (has(id)) {
            BalencerRecord r = get(id);
            r.update(hostname, port, load);
        } else {
            BalencerRecord r = new BalencerRecord(id, hostname, port, load);
            records.put(id, r);
        }
    }

    ArrayList<BalencerRecord> recordsWithLoadLowerThan(Integer num) {

    }

    BalencerRecord recordWithLoadLowerThan(Integer num) {

    }

    BalencerRecord recordWithLeastLoad(Integer num) {

    }

    BalencerRecord available(Integer load) {
        Integer targetLoad = calcAvailableLoad(load);
        BalencerRecord record;
        switch (rule) {
            case 0:
                record = recordWithLoadLowerThan(targetLoad);
                break;
            case 1:
            default:
                record = recordWithLeastLoad(targetLoad);
        }
        return record;
    }
}

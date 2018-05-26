import com.google.gson.Gson;

class RecoveryData {
    static final Gson g = new Gson();
    String[] users;
    String[] nodes;

    RecoveryData() {

    }

    static RecoveryData fromJson(String json) {
        return g.fromJson(json, RecoveryData.class);
    }

    String toJson() {
        return g.toJson(this);
    }
}

package timecrypt.server.utils;

public class FailureJson {
    private boolean failure = true;
    private String msg;

    public FailureJson(String msg) {
        this.msg = msg;
    }
}
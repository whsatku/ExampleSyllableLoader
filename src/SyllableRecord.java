public class SyllableRecord {
    private long id;
    private int lang;
    private int length;
    private boolean hasTailSpace;
    private boolean isUnused;
    private boolean isNumeric;
    private long mapFilePos;
    private long timestamp;
    private String text;

    private SyllableRecord() {
    }

    public SyllableRecord(long id, int lang, int length, boolean hasTailSpace, boolean isUnused, boolean isNumeric, long mapFilePos, long timestamp, String text) {
        this.id = id;
        this.lang = lang;
        this.length = length;
        this.hasTailSpace = hasTailSpace;
        this.isUnused = isUnused;
        this.isNumeric = isNumeric;
        this.mapFilePos = mapFilePos;
        this.timestamp = timestamp;
        this.text = text;
    }

    @Override
    public String toString() {
        return "SyllableRecord{" +
                "id=" + id +
                ", lang=" + lang +
                ", length=" + length +
                ", hasTailSpace=" + hasTailSpace +
                ", isUnused=" + isUnused +
                ", isNumeric=" + isNumeric +
                ", mapFilePos=" + mapFilePos +
                ", timestamp=" + timestamp +
                ", text='" + text + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    public int getLang() {
        return lang;
    }

    public int getLength() {
        return length;
    }

    public boolean isHasTailSpace() {
        return hasTailSpace;
    }

    public boolean isUnused() {
        return isUnused;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    public long getMapFilePos() {
        return mapFilePos;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }
}

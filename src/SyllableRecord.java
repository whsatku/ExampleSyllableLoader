public class SyllableRecord {
    private long id;
    private String text;

    private SyllableRecord() {
    }

    public SyllableRecord(long id, String text) {
        this.id = id;
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "SyllableRecord{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }
}

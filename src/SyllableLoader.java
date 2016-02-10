import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class SyllableLoader {
    private final int HEADER_SIZE = 256;
    private final int RECORD_TEXT_SIZE = 1023;
    private final String CHARSET = "MS874";

    private DataInput reader;
    private boolean headerRead = false;

    private SyllableLoader() {
    }

    public SyllableLoader(File input) throws FileNotFoundException {
        reader = new LittleEndianDataInputStream(new BufferedInputStream(new FileInputStream(input)));
    }

    public void readHeader() throws IOException {
        reader.skipBytes(HEADER_SIZE);
        headerRead = true;
    }

    public SyllableRecord readRecord() throws IOException {
        if (!headerRead) {
            throw new HeaderNotReadException();
        }

        long id = readUnsignedInt();
        int lang = reader.readUnsignedShort();
        int length = reader.readUnsignedShort();
        boolean hasTailSpace = readBoolean();
        boolean isUnused = reader.readBoolean();
        boolean isNumeric = reader.readBoolean();
        long mapFilePos = readUnsignedInt();
        long timestamp = reader.readLong();
        reader.skipBytes(1);

        String text = readText(hasTailSpace);

        return new SyllableRecord(
                id, lang, length,
                hasTailSpace, isUnused, isNumeric,
                mapFilePos, timestamp, text
        );
    }

    private boolean readBoolean() throws IOException {
        return (reader.readByte() & 0x1) == 1;
    }

    private long readUnsignedInt() throws IOException {
        // http://www.darksleep.com/player/JavaAndUnsignedTypes.html
        byte[] buf = new byte[4];
        reader.readFully(buf); // this does not swap the order

        int firstByte = (0x000000FF & ((int) buf[3]));
        int secondByte = (0x000000FF & ((int) buf[2]));
        int thirdByte = (0x000000FF & ((int) buf[1]));
        int fourthByte = (0x000000FF & ((int) buf[0]));

        return ((long) (firstByte << 24
                | secondByte << 16
                | thirdByte << 8
                | fourthByte))
                & 0xFFFFFFFFL;
    }

    private String readText(boolean hasTailSpace) throws IOException {
        if (hasTailSpace) {
            byte[] buffer = new byte[RECORD_TEXT_SIZE];
            reader.readFully(buffer);

            return new String(buffer, CHARSET);
        } else {
            List<Byte> buffer = new LinkedList<>();
            byte item;

            while ((item = reader.readByte()) != 0) {
                buffer.add(item);
            }

            byte[] rawBuffer = new byte[buffer.size()];

            // data is boxed, need manual unboxing
            int i = 0;
            for (Byte node : buffer) {
                rawBuffer[i++] = node;
            }

            return new String(rawBuffer, CHARSET);
        }
    }

    public static class HeaderNotReadException extends RuntimeException {
        public HeaderNotReadException() {
            super("File header wasn't read. Did you call readHeader()?");
        }
    }
}
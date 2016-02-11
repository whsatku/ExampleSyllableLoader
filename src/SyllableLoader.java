import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SyllableLoader {
    private final int HEADER_SIZE = 256;
    private final int RECORD_TEXT_SIZE = 1023;
    private final String CHARSET = "MS874";

    private enum TimeFormat {
        TBD,
        BIT_32,
        BIT_64,
    };

    private RandomAccessFile reader;
    private boolean headerRead = false;
    private TimeFormat timeFormat = TimeFormat.TBD;

    private SyllableLoader() {
    }

    public SyllableLoader(File input) throws FileNotFoundException {
        reader = new RandomAccessFile(input, "r");
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
        reader.skipBytes(4 + 4);

        String text = readText();

        return new SyllableRecord(id, text);
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

    private String readText() throws IOException {
        // attempt 32/64 bit detection by finding padding
        // 64 bit will have 8 bytes time_t + 4 bytes padding [0,0,0,0]
        // 32 bit will have 4 bytes time_t without padding
        switch(timeFormat){
            case BIT_64:
                reader.skipBytes(8+4);
                break;
            case BIT_32:
                reader.skipBytes(4);
                break;
            case TBD: default:
                doBitDetect();
        }

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

    private void doBitDetect() throws IOException {
        // detect for trailing padding
        byte[] buffer = new byte[8+4];
        reader.readFully(buffer);
        boolean hasPadding = Arrays.equals(Arrays.copyOfRange(buffer, 8, 12), new byte[]{0, 0, 0, 0});
        if(hasPadding){
            timeFormat = TimeFormat.BIT_64;
        }else{
            timeFormat = TimeFormat.BIT_32;
            // we overread data, feed back the last 4 bytes
            reader.seek(reader.getFilePointer() - 4);
        }
    }

    public static class HeaderNotReadException extends RuntimeException {
        public HeaderNotReadException() {
            super("File header wasn't read. Did you call readHeader()?");
        }
    }
}
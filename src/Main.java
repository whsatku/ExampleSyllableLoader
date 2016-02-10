import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        SyllableLoader loader;
        String filename = args.length > 0 ? args[0] : "SyllableDB-V1.dat";
        try {
            loader = new SyllableLoader(new File(filename));
            loader.readHeader();
        } catch (IOException e) {
            System.out.println("Cannot read file " + filename);
            return;
        }

        long lastId = 0;
        while (true) {
            try {
                SyllableRecord record = loader.readRecord();
                System.out.println(record);
                // debug check, you don't have to use this one
                if (record.getId() != lastId + 1) {
                    System.out.println("Record error: ID not continuous");
                    return;
                }
                lastId = record.getId();
            } catch (IOException e) {
                break;
            }
        }
    }
}

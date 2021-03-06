package Sacramento;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FilePermission;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Matteo on 21/08/2016.
 */
public class MainSacramento {

    private static List<Home> data = new LinkedList<Home>();
    private static List<Home> huizenBoven200K = new LinkedList<Home>();
    private static List<Home> huizenMeerAls4Slaapkamers = new LinkedList<Home>();

    public static void main(String[] args) {
        try {
            Thread reader = new Thread(() -> readData());
            reader.start();
            reader.join();
            data.forEach(System.out::println);

            huizenBoven200K = data.stream().filter(h -> h.getPrice() > 200000).collect(Collectors.toList());

            huizenMeerAls4Slaapkamers = data.stream().filter(h -> h.getBeds() > 4).collect(Collectors.toList());

            Map<String, Long> aantalVerkopenPerZip = data.stream().collect(Collectors.groupingBy(Home::getZip, Collectors.counting()));

            for (Map.Entry<String, Long> home : aantalVerkopenPerZip.entrySet()) {
                System.out.println(home.getKey() + " = " + home.getValue());
            }

            Thread writer200K = new Thread(() -> {
                try {
                    WriteListThread(huizenBoven200K, Paths.get("C:\\Users\\Matteo\\Downloads\\200KHuizen.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread writer4Bed = new Thread(() -> {
                try {
                    WriteListThread(huizenMeerAls4Slaapkamers, Paths.get("C:\\Users\\Matteo\\Downloads\\4BedHuizen.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            writer200K.start();
            writer4Bed.start();


        } catch (InterruptedException ex) {
            System.out.println("ex = " + ex);
        }
    }

    private static void WriteListThread(List<? extends Home> lijst, Path p) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(p);
        try {

            lijst.forEach(h -> {
                try {
                    writer.write(h.toString());
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception ex) {
            System.out.println("ex = " + ex);
        } finally {
            writer.close();
        }
    }

    private static Date parseDate(String date, String format) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
        return formatter.parse(date);
    }

    private static void readData() {
        try {
            Path path = Paths.get("C:\\Users\\Matteo\\Downloads\\Sacramentorealestatetransactions.csv");
            BufferedReader reader = Files.newBufferedReader(path);
            String line = null;
            Home h;
            boolean skipFirst = true;
            String[] arr;
            while ((line = reader.readLine()) != null) {
                if (!skipFirst) {
                    arr = line.split(",");
                    data.add(new Home(
                            arr[0],
                            arr[1],
                            arr[2],
                            arr[3],
                            Integer.parseInt(arr[4]),
                            Integer.parseInt(arr[5]),
                            Integer.parseInt(arr[6]),
                            HomeType.fromString(arr[7]),
                            parseDate(arr[8], "EEE MMM dd HH:mm:ss z yyyy"),
                            Double.parseDouble(arr[9]),
                            Double.parseDouble(arr[10]),
                            Double.parseDouble(arr[11])
                    ));
                } else {
                    skipFirst = false;
                }
            }
        } catch (IOException ex) {
            System.out.println("ex = " + ex);
        } catch (ParseException ecx) {
            System.out.println("ecx = " + ecx);
        }
    }


}

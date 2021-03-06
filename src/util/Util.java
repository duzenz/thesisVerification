package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Util {

    public Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order) {

        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public void printMap(Map<String, Integer> map, int limit) {
        int value = 0;
        for (Entry<String, Integer> entry : map.entrySet()) {
            value += 1;
            System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
            if (value == limit) {
                break;
            }
        }
    }

    public void printResultsToFile(String text, String filename) {
        File inputFile = new File(filename);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, true));
            writer.append(text + System.getProperty("line.separator"));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyFileToNewLocation(String baseFile, String targetFile) {
        File source = new File(baseFile);
        File destination = new File(targetFile);
        try {
            Files.copy(source.toPath(), destination.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String path) {
        File file = new File(path);
        try {
            Files.delete(file.toPath());
            System.out.println("File in path : " + path + " deleted");
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", path);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", path);
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}

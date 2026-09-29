package SnakeYAMLExample;

import java.io.FileReader;
import java.io.IOException;

public class ImportYAMLExample {

	private SnakeYAMLExample converter;

    public ImportYAMLExample(SnakeYAMLExample converter) {
        this.converter = converter;
    }

    public Person load(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Person loadedPerson = converter.convertToPerson(reader);
            System.out.println("Success: Person data imported from " + filePath);
            return loadedPerson;
        } catch (IOException e) {
            System.err.println("Failed to read import file: " + e.getMessage());
            return null;
        }
    }
}

package SnakeYAMLExample;

import java.io.FileWriter;
import java.io.IOException;

class ExportYAMLExample {
	private SnakeYAMLExample converter;

    public ExportYAMLExample(SnakeYAMLExample converter) {
        this.converter = converter;
    }

    public void export(Person person, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            converter.convertToYaml(person, writer);
            System.out.println("Success: Person data exported to " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to write export file: " + e.getMessage());
        }
    }
}

package SnakeYAMLExample;

import java.util.Arrays;

public class MainTest {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\10695534920\\PersonTest.yml";
        
        // 1. Inicializar projeto
        SnakeYAMLExample converter = new SnakeYAMLExample();
        ExportYAMLExample exporter = new ExportYAMLExample(converter);
        ImportYAMLExample importer = new ImportYAMLExample(converter);

        // 2. Criar pessoa
        Person person = new Person();
        person.setName("Bernardo");
        person.setAge(19);
        person.setHobbies(Arrays.asList("Research", "Architecture", "Java"));

        // 3. Exportar
        exporter.export(person, filePath);

        // 4. Importar
        Person importedPerson = importer.load(filePath);
        
        if (importedPerson != null) {
            System.out.println("Verified Name: " + importedPerson.getName());
            System.out.println("Verified Age: " + importedPerson.getAge());
            System.out.println("Verified Hobbies: " + importedPerson.getHobbies());
        }
    }
}
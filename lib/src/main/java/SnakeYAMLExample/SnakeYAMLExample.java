package SnakeYAMLExample;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.Reader;
import java.io.Writer;

public class SnakeYAMLExample {

	private Yaml yaml;

    public SnakeYAMLExample() {
    	LoaderOptions options = new LoaderOptions();
        options.setTagInspector(new TagInspector() {
            public boolean isGlobalTagAllowed(Tag tag) {
                return tag.getValue().equals("tag:yaml.org,2002:SnakeYAMLExample.Person");
            }
        });

        this.yaml = new Yaml(new Constructor(Person.class, options));
    }

    public void convertToYaml(Person person, Writer writer) {
        yaml.dump(person, writer);
    }

    public Person convertToPerson(Reader reader) {
        return yaml.load(reader);
    }
}
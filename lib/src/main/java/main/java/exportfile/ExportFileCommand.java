package main.java.exportfile;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.nlogo.api.Argument;
import org.nlogo.api.Command;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import burlap.statehashing.HashableState;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class ExportFileCommand implements Command {

    // The Q-Table to export. 
    private Map<HashableState, Object> qTable; 

    // Constructor: Pass the Q-Table reference here from your Extension's ClassManager
    public ExportFileCommand(Map<HashableState, Object> qTable) {
        this.qTable = qTable;
    }

    @Override
    public Syntax getSyntax() {
        // Defines the command syntax. Expects exactly 1 argument of type String (the file path).
    	System.out.println("Até aqui 1 está sendo processado.");
        return SyntaxJ.commandSyntax(new int[] { Syntax.StringType() });
    }

    @Override
    public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
        // Retrieve the string argument passed from NetLogo
        String path = args[0].getString();
        
        try {
            writeQTable(path);
            System.out.println("Até aqui 2 está sendo processado.");
        } catch (Exception e) {
            // Wrap underlying exceptions in a NetLogo ExtensionException so it displays in the UI properly
            throw new ExtensionException("Failed to write YAML: " + e.getMessage(), e);
        }
    }

    public void writeQTable(String path) {
        // 1. Configura a saída YAML para ser de fácil leitura
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setExplicitStart(true); // Adiciona '---' no começo do arquivo
        
        // 2. Inicializa a YAML
        Yaml yaml = new Yaml(options);

        // 3. Escreve a Q-Table no arquivo
        try (FileWriter writer = new FileWriter(path)) {
            yaml.dump(this.qTable, writer);
            System.out.println("Até aqui 3 está sendo processado.");
        } catch (IOException e) {
            throw new RuntimeException("Error writing Q-Table to file: " + e.getMessage(), e);
        }
    }
}
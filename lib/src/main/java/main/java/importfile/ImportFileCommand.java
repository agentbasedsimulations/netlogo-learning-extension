package main.java.importfile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nlogo.api.Argument;
import org.nlogo.api.Command;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.state.State;
import main.java.burlap.QLearningAlgorithm;

public class ImportFileCommand implements Command {

    private static final String DELIMITER = ";";

    @Override
    public Syntax getSyntax() {
        return SyntaxJ.commandSyntax(new int[] { Syntax.StringType() });
    }

    @Override
    public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
        String filename = args[0].getString();
        
        QLearningAlgorithm learning = QLearningAlgorithm.getInstance(args, context);
        Map<State, List<QValue>> qtable = learning.getState();

        // É crucial que a Q-Table já exista no BURLAP antes de importarmos os valores
        if (qtable == null || qtable.isEmpty()) {
            throw new ExtensionException("A Q-Table está vazia! Certifique-se de inicializar o agente/ambiente no NetLogo antes de importar.");
        }

        // 1. Ler o CSV e armazenar em uma estrutura rápida: Map<Estado, Map<Acao, QValue>>
        Map<String, Map<String, Double>> importedData = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                // Pula a primeira linha (cabeçalho)
                if (isFirstLine && line.startsWith("State")) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(DELIMITER);
                if (values.length >= 3) {
                    String stateStr = values[0];
                    String actionStr = values[1];
                    double q = Double.parseDouble(values[2]);

                    // Insere os dados lidos do CSV no nosso Map temporário
                    importedData.putIfAbsent(stateStr, new HashMap<>());
                    importedData.get(stateStr).put(actionStr, q);
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new ExtensionException("Erro ao ler o arquivo CSV: " + e.getMessage());
        }

        // 2. Injetar os valores do CSV na Q-Table oficial do agente
        for (Map.Entry<State, List<QValue>> entry : qtable.entrySet()) {
            State state = entry.getKey();

            // Constrói a string do estado exatamente igual fizemos na exportação
            StringBuilder stateStringBuilder = new StringBuilder();
            List<Object> keys = state.variableKeys();

            for (int i = 0; i < keys.size(); i++) {
                Object key = keys.get(i);
                int value = (int) ((double) state.get(key));
                stateStringBuilder.append(value);
                
                if (i < keys.size() - 1) {
                    stateStringBuilder.append("-");
                }
            }
            String stateString = stateStringBuilder.toString();

            // Verifica se o estado atual do agente existe no arquivo que importamos
            if (importedData.containsKey(stateString)) {
                Map<String, Double> importedActions = importedData.get(stateString);
                List<QValue> qValues = entry.getValue();

                // Itera sobre as ações disponíveis para esse estado
                for (QValue qValue : qValues) {
                    String cleanAction = qValue.a.actionName()
                            .replace("(anonymous command: [", "")
                            .replace("])", "")
                            .trim();

                    // Se a ação atual do agente foi encontrada no arquivo CSV para este estado, atualizamos o peso
                    if (importedActions.containsKey(cleanAction)) {
                        qValue.q = importedActions.get(cleanAction);
                    }
                }
            }
        }
    }
}
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

    @Override
    public Syntax getSyntax() {
        return SyntaxJ.commandSyntax(new int[] { Syntax.StringType() });
    }

    @Override
    public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
        String filename = args[0].getString();
        
        QLearningAlgorithm learning = QLearningAlgorithm.getInstance(args, context);
        Map<State, List<QValue>> qtable = learning.getState();
        
        if (qtable == null || qtable.isEmpty()) {
        	// setup()
         }
     // Estrutura temporária para guardar os dados lidos
        Map<String, Map<String, Double>> importedData = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // Separa o estado das ações
                String[] parts = line.split("\\}; actions values: ");
                if (parts.length < 2) continue;
                // Reconstroi o estado: "state: {YCOR=0.0...}"
                String stateStr = parts[0].trim() + "}"; 
                String actionsStr = parts[1].trim();

                Map<String, Double> actionMap = new HashMap<>();
                // Separa as ações pelo ;
                String[] actionPairs = actionsStr.split(";");
                for (String pair : actionPairs) {
                    pair = pair.trim();
                    if (pair.isEmpty()) continue;
                    // Caso tenha sinal especial, quebra no sinal de igual
                    int lastEqualsIdx = pair.lastIndexOf('=');
                    if (lastEqualsIdx != -1) {
                        String actionName = pair.substring(0, lastEqualsIdx).trim();
                        try {
                            double qValue = Double.parseDouble(pair.substring(lastEqualsIdx + 1).trim());
                            actionMap.put(actionName, qValue);
                        } catch (NumberFormatException e) {
                        	// Valores mal formatados
                        }
                    }
                }
                // Salva o estado e ações
                importedData.put(stateStr, actionMap);
            }
        } catch (IOException e) {
            throw new ExtensionException("Erro ao ler arquivo: " + e.getMessage());
        }
        // Entrar na Q-Table do agente para injetar os valores
        for (Map.Entry<State, List<QValue>> entry : qtable.entrySet()) {
            State state = entry.getKey();
            // Constroi a string do estado
            StringBuilder stateStringBuilder = new StringBuilder();
            stateStringBuilder.append("state: {");
            List<Object> keys = state.variableKeys();

            for (int i = 0; i < keys.size(); i++) {
                Object key = keys.get(i);
                double value = (double) state.get(key);
                stateStringBuilder.append(key.toString()).append("=").append(value);
                
                if (i < keys.size() - 1) {
                    stateStringBuilder.append(", ");
                }
            }
            stateStringBuilder.append("}");
            String stateString = stateStringBuilder.toString();
            // Se o estado existe no arquivo importado
            if (importedData.containsKey(stateString)) {
                Map<String, Double> importedActions = importedData.get(stateString);
                List<QValue> qValues = entry.getValue();
                // Itera pelas ações de cada estado na memória do agente
                for (QValue qValue : qValues) {
                    String rawActionName = qValue.a.actionName();
                    // Se a ação estiver no arquivo, injetamos o peso!
                    if (importedActions.containsKey(rawActionName)) {
                        qValue.q = importedActions.get(rawActionName);
                    }
                }
            }
        }
    }
}
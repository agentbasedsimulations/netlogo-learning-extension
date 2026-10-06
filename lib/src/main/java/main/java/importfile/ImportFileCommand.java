package main.java.importfile; 

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Command;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
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
        	try {
                learning.setup();
                qtable = learning.getState();
            } catch (AgentException e) {
                throw new ExtensionException("Erro ao inicializar a Q-Table vazia: " + e.getMessage());
            }
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
        
        
        
        
        Map<HashableState, QLearningStateNode> newQTable = new HashMap<>();
        burlap.statehashing.simple.SimpleHashableStateFactory factory = new burlap.statehashing.simple.SimpleHashableStateFactory();

        // Itera sobre os dados
        for (Map.Entry<String, Map<String, Double>> entry : importedData.entrySet()) {
            String rawStateString = entry.getKey(); 
            Map<String, Double> actionsMap = entry.getValue();

            // 1. Limpa o texto
            String cleanStateStr = rawStateString.replace("state: {", "").replace("}", "").trim();

            // 2. Instancia um novo AgentState
            main.java.burlap.AgentState agentState = new main.java.burlap.AgentState();
            Map<String, Object> stateVariables = new HashMap<>();

            // 3. Quebra o texto "YCOR=0.0, XCOR=1.0"
            String[] variables = cleanStateStr.split(",");
            for (String var : variables) {
                String[] keyValue = var.split("=");
                if (keyValue.length == 2) {
                    stateVariables.put(keyValue[0].trim(), Double.parseDouble(keyValue[1].trim()));
                }
            }
            
            // 4. Popula o AgentState
            agentState.setState(stateVariables); 
            HashableState hashedState = factory.hashState(agentState);

            // 5. Vai guardar este estado e suas ações
            burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode node = 
                new burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode(hashedState);
            node.qEntry = new ArrayList<>();

            // 6. Adiciona todas as ações
            for (Map.Entry<String, Double> actionEntry : actionsMap.entrySet()) {
                burlap.behavior.valuefunction.QValue qv = new burlap.behavior.valuefunction.QValue(
                    agentState, 
                    new burlap.mdp.core.action.SimpleAction(actionEntry.getKey()), 
                    actionEntry.getValue()
                );
                node.qEntry.add(qv);
            }

            // Salva na tabela
            newQTable.put(hashedState, node);
        }

        // 7. INJEÇÃO VIA REFLECTION
        try {
            java.lang.reflect.Field qFunctionField = burlap.behavior.singleagent.learning.tdmethods.QLearning.class.getDeclaredField("qFunction");
            qFunctionField.setAccessible(true);
            qFunctionField.set(learning.getQLearningAdapter(), newQTable);
            
            System.out.println("Sucesso: Foram injetados " + newQTable.size() + " estados zerados com valores do arquivo!");
        } catch (Exception e) {
            throw new ExtensionException("Erro ao injetar a tabela reconstruída: " + e.getMessage());
        }
    }
}
package main.java.exportfile;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

public class ExportFileCommand implements Command {
	
    private static final String DELIMITER = ";";

    @Override
    public Syntax getSyntax() {
        // Recebe o argumento chamando a classe "C:file\file"
        return SyntaxJ.commandSyntax(new int[] { Syntax.StringType() });
    }

    @Override
    public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
        String filename = args[0].getString();
        
        // Recupera o QLearningAlgorithm
        QLearningAlgorithm learning = QLearningAlgorithm.getInstance(args, context);
        
        //Cria um mapa com os Estados e Qvalues
        Map<State, List<QValue>> qtable = learning.getState();
        
        // Verifica se o modelo foi rodado e a Qtable recebeu valores
        if (qtable == null || qtable.isEmpty()) {
            throw new ExtensionException("The model was not yet initialized or the Q-table is empty!");
        }

        // Testa criar uma pasta com o nome do arquivo
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            
            // Cria um cabeçalho para o arquivo
            writer.println("State" + DELIMITER + "Action" + DELIMITER + "MaxQValue");

            // Passa pelos estados do mapa
            for (Map.Entry<State, List<QValue>> entry : qtable.entrySet()) {
                State state = entry.getKey();
                
                // De acordo com o número de estados cria os estados com valores
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
                // Cria de fato uma string com os estados e valores
                String stateString = stateStringBuilder.toString();
                
                // Acessa os dados dos valores
                List<QValue> qValues = entry.getValue();
                QValue bestAction = null;

                // Salva o melhor valor(ação)
                for (QValue qValue : qValues) {
                    // Keep the best value
                    if (bestAction == null || qValue.q > bestAction.q) {
                        bestAction = qValue;
                    }
                }

                // Adiciona os valores com a string da ação
                if (bestAction != null) {
                    String cleanAction = bestAction.a.actionName()
                            .replace("(anonymous command: [", "")
                            .replace("])", "")
                            .trim();
                    // Arredonda o valor para duas casas decimais
                    double maxQ = Math.round(bestAction.q * 100.0) / 100.0;

                    // Escreve no arquivo os dados encontrados
                    writer.println(stateString + DELIMITER + cleanAction + DELIMITER + maxQ);
                }
            }
        } catch (IOException e) {
            throw new ExtensionException("Error writing Q-Table to file: " + e.getMessage());
        }
    }
}
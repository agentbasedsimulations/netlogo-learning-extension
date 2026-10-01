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
            throw new ExtensionException("The model was not yet initialized or the Q-table is empty!");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
        	// Itera cada estado
            for (Map.Entry<State, List<QValue>> entry : qtable.entrySet()) {
                State state = entry.getKey();
                List<QValue> qValues = entry.getValue();
                
                StringBuilder lineBuilder = new StringBuilder();
                // Formata o Estado: {CHAVE=VALOR, CHAVE=VALOR, ...}
                lineBuilder.append("state: {");
                List<Object> keys = state.variableKeys();
                
                for (int i = 0; i < keys.size(); i++) {
                    Object key = keys.get(i);
                    // Pega o valor do estado
                    double value = (double) state.get(key);
                    
                    lineBuilder.append(key.toString()).append("=").append(value);
                    
                    if (i < keys.size() - 1) {
                        lineBuilder.append(", ");
                    }
                }
                lineBuilder.append("}; actions values: ");
                // Formata as Ações: (anonymous command: [ acao ])=valor
                for (int i = 0; i < qValues.size(); i++) {
                    QValue qValue = qValues.get(i);
                    // Mantem o nome da ação
                    String rawActionName = qValue.a.actionName();
                    double q = qValue.q;
                    
                    lineBuilder.append(rawActionName).append("=").append(q);
                    // Adiciona o ; no final
                    lineBuilder.append("; ");
                }
                // escreve a linha no arquivo
                writer.println(lineBuilder.toString().trim());
            }
        } catch (IOException e) {
            throw new ExtensionException("Error writing Q-Table to file: " + e.getMessage());
        }
    }
}
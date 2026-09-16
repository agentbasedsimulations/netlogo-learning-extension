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
        // Expects one string argument: the destination file path/name
        return SyntaxJ.commandSyntax(new int[] { Syntax.StringType() });
    }

    @Override
    public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
        String filename = args[0].getString();
        
        // Retrieve the QLearningAlgorithm instance
        QLearningAlgorithm learning = QLearningAlgorithm.getInstance(args, context);
        
        Map<State, List<QValue>> qtable = learning.getState();
        
        if (qtable == null || qtable.isEmpty()) {
            throw new ExtensionException("The model was not yet initialized or the Q-table is empty!");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            
            // Dynamically create an header for the file
            writer.println("State" + DELIMITER + "Action" + DELIMITER + "MaxQValue");

            // Iterate through states
            for (Map.Entry<State, List<QValue>> entry : qtable.entrySet()) {
                State state = entry.getKey();
                
                // Dynamically construct the state string
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

                List<QValue> qValues = entry.getValue();
                QValue bestAction = null;

                for (QValue qValue : qValues) {
                    // Keep the best value
                    if (bestAction == null || qValue.q > bestAction.q) {
                        bestAction = qValue;
                    }
                }

                // Print the best value found in each state
                if (bestAction != null) {
                    String cleanAction = bestAction.a.actionName()
                            .replace("(anonymous command: [", "")
                            .replace("])", "")
                            .trim();
                    
                    double maxQ = Math.round(bestAction.q * 100.0) / 100.0;

                    writer.println(stateString + DELIMITER + cleanAction + DELIMITER + maxQ);
                }
            }
        } catch (IOException e) {
            throw new ExtensionException("Error writing Q-Table to file: " + e.getMessage());
        }
    }
}
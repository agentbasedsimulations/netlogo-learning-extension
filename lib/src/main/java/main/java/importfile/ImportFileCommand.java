package main.java.importfile;

import org.nlogo.api.Argument;
import org.nlogo.api.Command;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import main.java.burlap.QLearningAlgorithm;
import main.java.model.AgentLearning;
import main.java.model.Session;

public class ImportFileCommand implements Command {

    public Syntax getSyntax() {
        // Defines the command syntax. Expects exactly 1 argument of type String (the file path).
        return SyntaxJ.commandSyntax(new int[] { Syntax.StringType() });
    }

    @Override
    public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {

        String path = args[0].getString();
        AgentLearning agent = Session.getInstance().getAgent(context.getAgent());
        
        if(agent.algorithm.equals("qlearning")) {
            QLearningAlgorithm learning = QLearningAlgorithm.getInstance(args, context);
            learning.loadQTable(path);;
        }
    }
}
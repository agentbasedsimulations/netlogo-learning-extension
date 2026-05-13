package main.java;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.PrimitiveManager;

import main.java.primitives.go.DecayEpsilonCommand;
import main.java.primitives.go.GetEpisodeCommand;
import main.java.primitives.go.GetQTableCommand;
import main.java.primitives.go.LearningCommand;
import main.java.primitives.setup.ActionSelectionCommand;
import main.java.primitives.setup.ActionSelectionEGreedyCommand;
import main.java.primitives.setup.ActionSelectionRandomCommand;
import main.java.primitives.setup.ActionsCommand;
import main.java.primitives.setup.DefineAlgorithmCommand;
import main.java.primitives.setup.DiscountFactorCommand;
import main.java.primitives.setup.IsEndEpisodeCommand;
import main.java.primitives.setup.LambdaCommand;
import main.java.primitives.setup.LearningRateCommand;
import main.java.primitives.setup.RewardCommand;
import main.java.primitives.setup.SetupCommand;
import main.java.primitives.setup.StateDefinitionCommand;
import main.java.primitives.setup.StateDefinitionExtraCommand;
import main.java.tests.FirstTezts;

/**
 * Extension Commands Class
 * @author Eloísa Bazzanella
 * @since  march, 2022
 */
public class ExtensionCommands extends DefaultClassManager {
    public void load(PrimitiveManager primitiveManager) {
        
        //COMANDOS SETUP
        primitiveManager.addPrimitive("learning-rate", new LearningRateCommand());
        primitiveManager.addPrimitive("discount-factor", new DiscountFactorCommand());
        primitiveManager.addPrimitive("lambda", new LambdaCommand());
        primitiveManager.addPrimitive("action-selection", new ActionSelectionCommand());
        primitiveManager.addPrimitive("action-selection-egreedy", new ActionSelectionEGreedyCommand());
        primitiveManager.addPrimitive("action-selection-random", new ActionSelectionRandomCommand());
        primitiveManager.addPrimitive("reward", new RewardCommand());
        primitiveManager.addPrimitive("end-episode", new IsEndEpisodeCommand());
        primitiveManager.addPrimitive("actions", new ActionsCommand());
        primitiveManager.addPrimitive("state-def", new StateDefinitionCommand());
        primitiveManager.addPrimitive("state-def-extra", new StateDefinitionExtraCommand());
        primitiveManager.addPrimitive("define-algorithm", new DefineAlgorithmCommand());
        primitiveManager.addPrimitive("setup", new SetupCommand());
        
        //COMANDOS GO
        primitiveManager.addPrimitive("learning", new LearningCommand()); // TODO debug=true
        primitiveManager.addPrimitive("decay-epsilon", new DecayEpsilonCommand());
        primitiveManager.addPrimitive("get-episode", new GetEpisodeCommand());
        //primitiveManager.addPrimitive("get-qtable", new GetQTableCommand()); // TODO
        primitiveManager.addPrimitive("get-learning-details", new GetQTableCommand());
        
        // COMANDOS TESTS
        primitiveManager.addPrimitive("tests", new FirstTezts());
    }
}

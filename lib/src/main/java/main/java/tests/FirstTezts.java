package main.java.tests;

import java.util.List;
import java.util.Map;

import org.nlogo.api.*;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import main.java.burlap.AgentStateModel;
import main.java.burlap.IsEndEpisode;
import main.java.burlap.Reward;
import main.java.burlap.SarsaAlgorithm;
import main.java.model.AgentLearning;
import main.java.model.Session;

public class FirstTezts implements Reporter{

	public Object report(Argument args[], Context context) throws ExtensionException {
		// preparando variaveis
		String src = args[0].getString();
		AgentLearning agent = Session.getInstance().getAgent(context.getAgent());
		StringBuilder sb = new StringBuilder("Agente: ").append(agent.agent.id()).append("\n");
		SarsaAlgorithm learning = SarsaAlgorithm.getInstance(args, context);
		State s = new GridWorldState(new GridAgent(3, 2));
		AgentStateModel  stateModel   = new AgentStateModel(args, context);
        RewardFunction   reward       = new Reward(args, context);
        TerminalFunction isEndEpisode = new IsEndEpisode(args, context);
		
		// verificando a QTable
		Map<State, List<QValue>> qtable = learning.getState();
		Planner planner = new ValueIteration((SADomain) learning.getSarsaAdapter().getDomain(), 0.99, new SimpleHashableStateFactory(), 0.001, 100);
        Policy p = planner.planFromState(s);
        SADomain domain = new SADomain();
        domain.setModel(new FactoredModel(stateModel, reward, isEndEpisode));

        //PolicyUtils.rollout(p, s, learning.getSarsaAdapter().getModel());
        simpleValueFunctionVis((ValueFunction)planner, p, (SADomain) learning.getSarsaAdapter().getDomain(), s);
		
		// realizando formatação em string
		learning.getState().forEach((estado, valores) -> sb.append("Estado: " + estado + "Valores Q: " + valores + "\n" ));
		sb.append("Caminho do Arquivo: " + src);
		
		return sb.toString();
	}
	
	public void simpleValueFunctionVis(ValueFunction valueFunction, Policy p, SADomain domain, State s){

        List<State> allStates = StateReachability.getReachableStates(s,
                domain, new SimpleHashableStateFactory());
        ValueFunctionVisualizerGUI gui = GridWorldDomain.getGridWorldValueFunctionVisualization(
                allStates, 11, 11, valueFunction, p);
        gui.initGUI();
        try {
            // Pausa a execução do programa por 3000 milissegundos (3 segundos)
            // Isso te dá tempo de ver o agente parado no bloco verde (4,4)
            Thread.sleep(3000); 
        } catch (InterruptedException e) {
            System.out.println("A contagem de tempo foi interrompida.");
            e.printStackTrace();
        }
        gui.dispose();

    }

	@Override
	public Syntax getSyntax() {
		// TODO Auto-generated method stub
		int [] src = {Syntax.StringType()};
		return SyntaxJ.reporterSyntax(src, Syntax.StringType());
	}
}

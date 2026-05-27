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
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.StateValuePainter2D;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.vardomain.VariableDomain;
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

	Map<State, List<QValue>> qtable;
	
	public Object report(Argument args[], Context context) throws ExtensionException {
		// preparando variaveis
		String src = args[0].getString();
		AgentLearning agent = Session.getInstance().getAgent(context.getAgent());
		StringBuilder sb = new StringBuilder("Agente: ").append(agent.agent.id()).append("\n");
		SarsaAlgorithm learning = SarsaAlgorithm.getInstance(args, context);
		State s = null;
		AgentStateModel  stateModel   = new AgentStateModel(args, context);
        RewardFunction   reward       = new Reward(args, context);
        TerminalFunction isEndEpisode = new IsEndEpisode(args, context);
		
		// verificando a QTable
		qtable = learning.getState();
		if (!qtable.isEmpty()) {
		    s = qtable.keySet().iterator().next(); 
		} else {
		    throw new ExtensionException("A Q-Table está vazia. Treine o agente primeiro!");
		}
		SADomain domain = (SADomain) learning.getSarsaAdapter().getDomain();
		Planner planner = new ValueIteration(domain, 0.99, new SimpleHashableStateFactory(), 0.001, 100);
        Policy p = planner.planFromState(s);

        //PolicyUtils.rollout(p, s, learning.getSarsaAdapter().getModel());
        simpleValueFunctionVis((ValueFunction)planner, p, domain, s, agent);
		
		// realizando formatação em string
		learning.getState().forEach((estado, valores) -> sb.append("Estado: " + estado + "Valores Q: " + valores + "\n" ));
		sb.append("Caminho do Arquivo: " + src);
		
		return sb.toString();
	}
	
	public void simpleValueFunctionVis(ValueFunction valueFunction, Policy p, SADomain domain, State s, AgentLearning agent){

        List<State> allStates = new java.util.ArrayList<State>(qtable.keySet());
        
        StateValuePainter2D svp = new StateValuePainter2D();
        svp.setXYKeys("XCOR", "YCOR", 
                new VariableDomain(0, 4), new VariableDomain(0, 3), 
                1, 1);

	  // ==========================================
	  // CAMADA 2: O PINTOR DE POLÍTICA (Setas de Decisão)
	  // ==========================================
	  // Opcional, mas essencial para ver o Value Iteration funcionando. Ele desenha setas.
	  PolicyGlyphPainter2D spp = new PolicyGlyphPainter2D();
	  spp.setXYKeys("XCOR", "YCOR", 
	                new VariableDomain(0, 4), new VariableDomain(0, 3), 
	                1, 1);
	
	  // ATENÇÃO 2: Você precisa mapear o nome das ações geradas pela sua extensão
	  // para as setas desenhadas na tela.
	  // Os números representam a direção (0=Norte, 1=Sul, 2=Leste, 3=Oeste).
	  // Substitua "nome_acao_cima", etc., pelas Strings reais que suas ações usam!
//	  for (AnonymousCommand action: agent.actions) {
//		  spp.setActionNameGlyphPainter(action.a, null);
//	  }
	  spp.setActionNameGlyphPainter("(anonymous command: [ goUp ])", new ArrowActionGlyph(0));
	  spp.setActionNameGlyphPainter("(anonymous command: [ goDown ])", new ArrowActionGlyph(1));
	  spp.setActionNameGlyphPainter("(anonymous command: [ goRight ])", new ArrowActionGlyph(2));
	  spp.setActionNameGlyphPainter("(anonymous command: [ goLeft ])", new ArrowActionGlyph(3));
	
	
	  // ==========================================
	  // MONTAGEM FINAL DA INTERFACE GRÁFICA
	  // ==========================================
	  // Juntamos os estados, as cores (svp), a política (spp) e os valores.
	  ValueFunctionVisualizerGUI gui = new ValueFunctionVisualizerGUI(allStates, svp, valueFunction);
	  gui.setSpp(spp);
	  gui.setPolicy(p);
	  gui.setBgColor(java.awt.Color.GRAY);
        
        gui.initGUI();
        try {
            // Pausa a execução do programa por 3000 milissegundos (3 segundos)
            // Isso te dá tempo de ver o agente parado no bloco verde (4,4)
            Thread.sleep(10000); 
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

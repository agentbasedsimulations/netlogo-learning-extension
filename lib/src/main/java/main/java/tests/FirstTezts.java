package main.java.tests;

import org.nlogo.api.*;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import main.java.burlap.SarsaAlgorithm;
import main.java.model.AgentLearning;
import main.java.model.Session;

public class FirstTezts implements Reporter{

	public Object report(Argument args[], Context context) throws ExtensionException {
		String src = args[0].getString();
		AgentLearning agent = Session.getInstance().getAgent(context.getAgent());
		
		StringBuilder sb = new StringBuilder("Agente: ").append(agent.agent.id()).append("\n");
		
		SarsaAlgorithm learning = SarsaAlgorithm.getInstance(args, context);
		learning.getState().forEach((estado, valores) -> sb.append("Estado: " + estado + "Valores Q: " + valores + "\n" ));
		
		sb.append("Caminho do Arquivo: " + src);
		
		return sb.toString();
	}

	@Override
	public Syntax getSyntax() {
		// TODO Auto-generated method stub
		int [] src = {Syntax.StringType()};
		return SyntaxJ.reporterSyntax(src, Syntax.StringType());
	}
}

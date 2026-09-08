package main.java.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Reporter;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.state.State;
import main.java.burlap.QLearningAlgorithm;
import main.java.model.AgentLearning;
import main.java.model.Session;

public class VerifyLearningCommand implements Reporter{

	private static final String COMMA_DELIMITER = ";";

	@Override
	public Syntax getSyntax() {
		int [] src = {Syntax.StringType()};
		return SyntaxJ.reporterSyntax(src, Syntax.StringType());
	}

	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException {
		// valores assistentes
		String src 						= args[0].getString();
		StringBuilder sb 				= new StringBuilder();
		QLearningAlgorithm learning 	= QLearningAlgorithm.getInstance(args, context);
		// verificar o algoritmo do agente, a fazer
		//AgentLearning agent = Session.getInstance().getAgent(context.getAgent());
		// tabelas q
		Map<State, List<QValue>> qtable = learning.getState();
		Map<List<Integer>, String> map 	= readFile(src);
		
		sb.append("QLEARNING VERIFIER\n");
		sb.append("File location: " + src + "\n\n");
		if(map.size() != qtable.size()) {
			if(qtable.size() == 0) {
				throw new ExtensionException("The model was not yet initialized!");

			}
			else {
				throw new ExtensionException("The Policy tables are not matching in size!\nUse the command get-learning-details to see the full list of available states\n" + 
						"Tamanho da política: " + qtable.size() + "\n" + "Tamanho da tabela: " + map.size());

			}
		}
		sb.append("Tamanho da política: " + qtable.size() + "\n" + "Tamanho da tabela: " + map.size() + "\n\n");
		sb.append(verifyStates(qtable, map));
		return sb.toString();
		
	}
	
	private StringBuilder verifyStates(Map<State, List<QValue>> qtable, Map<List<Integer>, String> map) throws ExtensionException {
		Set<State> estado 			= qtable.keySet();
		Set<List<Integer>> chaves 	= map.keySet();
		boolean isEqual 			= true;
		StringBuilder sb 			= new StringBuilder();
		
		Iterator<State> itr 				= estado.iterator();
		Iterator<List<Integer>> itr_chave 	= chaves.iterator();
		
		while(itr.hasNext()) {
			State elemento 		= itr.next();
			boolean wasFound 	= false;
			
			sb.append(elemento.variableKeys().toString());
			String nome_var1 = elemento.variableKeys().get(0).toString();
			String nome_var2 = elemento.variableKeys().get(1).toString();
			
			while(itr_chave.hasNext()) {
				List<Integer> elemento_chave 	= itr_chave.next();
				double cord_x 					= (double) elemento.get(nome_var1);
				double cord_y 					= (double) elemento.get(nome_var2);;
				
				if(Double.compare(cord_x, elemento_chave.get(0)) == 0 && Double.compare(cord_y, elemento_chave.get(1)) == 0) {
					
					wasFound = true;
					List<QValue> maxQValue 	= findMaxQValue(qtable.get(elemento));
					
					// nova implementação
					String expectedString = map.get(elemento_chave);

					if (!expectedString.contains("*")) {

					    // lista com as acoes do csv
					    List<String> expectedActions = new ArrayList<>();
					    for (String acao : expectedString.split(" -/ ")) {
					        expectedActions.add(acao.trim());
					    }

					    // lista com as acoes gerada pela extensão
					    List<String> foundActions = new ArrayList<>();

					    for (int i = 0; i < expectedActions.size(); i++) {
					        String cleanAction = maxQValue.get(i).a.actionName()
					                .replace("(anonymous command: [", "")
					                .replace("])", "")
					                .trim();
					        foundActions.add(cleanAction);
					    }
					    
					    // remove as que foram achadas na qtable
					    List<String> missingFromQTable = new ArrayList<>(expectedActions);
					    missingFromQTable.removeAll(foundActions); 

					    // remove as que eram esperadas (Sobram as ERRADAS que a Q-Table inventou)
					    List<String> wrongActionsFound = new ArrayList<>(foundActions);
					    wrongActionsFound.removeAll(expectedActions); 

					    if (!missingFromQTable.isEmpty() || !wrongActionsFound.isEmpty()) {
					        isEqual = false;
					        sb.append("Different Action Found!\n");
					        sb.append("State: ").append(elemento_chave).append("\n");
					        
					        // imprime o que faltou achar
					        if (!missingFromQTable.isEmpty()) {
					            sb.append("Expected action missing: ").append(String.join(", ", missingFromQTable)).append("\n");
					        }
					        
					        // imprime o que achou errado
					        if (!wrongActionsFound.isEmpty()) {
					            sb.append("Action found: ").append(String.join(", ", wrongActionsFound)).append("\n");
					        }
					        sb.append("\n");
					    }
					}
				
				}
				if(!itr_chave.hasNext() && !wasFound) {
					throw new ExtensionException("State "+ (int)cord_x + " " + (int)cord_y + " was not found on the desired policy table\nUse the command get-learning-details to see the full list of available states");
				}
			}
			itr_chave = chaves.iterator();
		}
		sb.append("\nActions verified, the result is...\n");
		if(isEqual) {
			sb.append("Both tables are equal!");
		}
		else {
			sb.append("Different values were found on the Q-Table!");
		}
		return sb;
	}
	
	private List<QValue> findMaxQValue(List<QValue> state) {
	    if (state == null || state.isEmpty()) {
	        return null; 
	    }
	    
	    if (state.size() == 1) {
	        return state;
	    }
	    List<QValue> values = new ArrayList<QValue>();
	    QValue firstMax = null;
	    QValue secondMax = null;

	    for (QValue qvalue : state) {
	        if (firstMax == null || qvalue.q > firstMax.q) {
	            secondMax = firstMax;
	            firstMax = qvalue; 
	        } 
	        else if (secondMax == null || qvalue.q > secondMax.q) {
	            secondMax = qvalue;
	        }
	    }
	    values.add(firstMax);
	    values.add(secondMax);
	    return values;
	}
	
	private Map<List<Integer>, String> readFile(String src){
		Map<List<Integer>, String> map 	= new HashMap<>();
		try (BufferedReader br 			= new BufferedReader(new FileReader(src))) {
		    String line 				= br.readLine();
		    while ((line = br.readLine()) != null) {
		    	String[] values 		= line.split(COMMA_DELIMITER);
		    	String actions = "";
		    	for (int i = 1; i<values.length; i++) {
		    		if(!actions.isBlank() || !actions.isEmpty()) {
		    			actions += " -/ ";
		    		}
		    		actions += values[i];
		    	}
		    	map.put(getStates(values[0]), actions );
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	private List<Integer> getStates (String coord){
		List<Integer> states = new ArrayList<Integer>();
		for (String state: coord.split("-")) {
			states.add(Integer.parseInt(state));
		}
		return states;
	}
	
//	private List<String> getActions (String act){
//		List<String> actions = new ArrayList<String>();
//		for (String action: act.split("-")) {
//			actions.add(action);
//		}
//		return actions;
//	}

}

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

public class VerifyLearningCommand implements Reporter {

	private static final String COMMA_DELIMITER = ";";

	@Override
	public Syntax getSyntax() {
		int[] src = { Syntax.StringType() };
		return SyntaxJ.reporterSyntax(src, Syntax.StringType());
	}

	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException {
		// valores assistentes
		String src 					= args[0].getString();
		StringBuilder sb 			= new StringBuilder();
		QLearningAlgorithm learning = QLearningAlgorithm.getInstance(args, context);
		// verificar o algoritmo do agente, a fazer
		// AgentLearning agent = Session.getInstance().getAgent(context.getAgent());
		// tabelas q
		Map<State, List<QValue>> qtable = learning.getState();
		Map<List<Integer>, String> map 	= readFile(src);

		sb.append("QLEARNING VERIFIER\n");
		sb.append("File location: " + src + "\n\n");
		if (map.size() != qtable.size()) {
			if (qtable.size() == 0) {
				throw new ExtensionException("The model was not yet initialized!");

			} else {
				throw new ExtensionException(
						"The Policy tables are not matching in size!\nUse the command get-learning-details to see the full list of available states\n"
								+ "Tamanho da política: " + qtable.size() + "\n" + "Tamanho da tabela: " + map.size());

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
		List<Object> stateVariables 		= getStateVariables(estado);
		
		sb.append(getStateVariables(estado));
		while(itr.hasNext()) { 
		  State elemento 	= itr.next(); 
		  boolean wasFound 	= false;
	  
		  // vou criar uma função que seja responsável por fazer a verificação dos estados 
		  // essa função deve operar de forma modular, onde pode operar com qualquer quantidade de estados
		  
		  while(itr_chave.hasNext() && !wasFound) {
			  List<Integer> elemento_chave = itr_chave.next();
			
			  if(elemento_chave.size() != stateVariables.size()) {
				  throw new ExtensionException("State "+ elemento_chave + " "
					  		+ "has a diferent value of state variables than expected\nUse the command get-learning-details to see the full list of available states");
			  }
			  // variavel resposavel por informar se os estados são iguais ou não
			  boolean isStatesEqual = true;
			  // é comparado as variaveis e caso seja encontrado alguma divergência
			  // é informado que os estados não são iguais
			  for(int i = 0; i<stateVariables.size(); i++) {
				  double var = (double) elemento.get(stateVariables.get(i).toString()); 
				  if(Double.compare(var, elemento_chave.get(i)) != 0) {
					  isStatesEqual = false;
					  break;
				  }
			  }
			  sb.append(isStatesEqual + "\n");
				  
			  if(isStatesEqual) {
		  
				  wasFound = true; 
//				  List<QValue> maxQValue = findMaxQValue(qtable.get(elemento));
//		  
//		  // nova implementação 
//				  String expectedString = map.get(elemento_chave);
//		  
//				  if (!expectedString.contains("")) {
//		  
//					  // lista com as acoes do csv 
//					  List<String> expectedActions = new ArrayList<>(); 
//					  for (String acao : expectedString.split(" -/ ")) {
//						  expectedActions.add(acao.trim()); 
//					  }
//					  
//				  //lista com as acoes gerada pela extensão 
//					  List<String> foundActions = new ArrayList<>();
//					  
//					  for (int i = 0; i < expectedActions.size(); i++) { 
//						  String cleanAction = maxQValue.get(i).a.actionName() 
//								  .replace("(anonymous command: [", "")
//								  .replace("])", "") .trim(); 
//						  foundActions.add(cleanAction); 
//					  }
//					  
//					  // remove as que foram achadas na qtable 
//					  List<String> missingFromQTable = new ArrayList<>(expectedActions); 
//					  missingFromQTable.removeAll(foundActions);
//		  
//					  // remove as que eram esperadas (Sobram as ERRADAS que a Q-Table inventou)
//					  List<String> wrongActionsFound = new ArrayList<>(foundActions);
//					  wrongActionsFound.removeAll(expectedActions);
//		  
//					  if (!missingFromQTable.isEmpty() || !wrongActionsFound.isEmpty()) { 
//						  isEqual = false; 
//						  sb.append("Different Action Found!\n");
//						  sb.append("State: ").append(elemento_chave).append("\n");
//		  
//						  // imprime o que faltou achar 
//						  if (!missingFromQTable.isEmpty()) {
//							  sb.append("Expected action missing: ").append(String.join(", ",
//									  missingFromQTable)).append("\n"); 
//							  }
//		  
//						  // imprime o que achou errado 
//						  if (!wrongActionsFound.isEmpty()) {
//							  sb.append("Action found: ").append(String.join(", ",
//									  wrongActionsFound)).append("\n"); 
//							  } 
//						  sb.append("\n"); 
//					  } 
//				  }
			  } 
		  	  if(!itr_chave.hasNext() && !wasFound) { 
		  		  throw new ExtensionException("State "+ elemento_chave + " "
		  		+ "was not found on the Qtable\nUse the command get-learning-details to see the full list of available states"); 
			  }
		  }
		  itr_chave = chaves.iterator(); 
		}
	  sb.append("\nActions verified, the result is...\n"); 
	  if(isEqual) {
  		sb.append("Both tables are equal!"); } else {
		sb.append("Different values were found on the Q-Table!"); 
  	  }
	 
	  return sb.append("\n" + estado);
	}
	
	

	private List<Object> getStateVariables(Set<State> states) {
		// dessa forma ele retorna o set como list das variaveis de estado, o que me permite manipular com mais facilidade
		State stateVariables = (State) states.toArray()[0];
		return stateVariables.variableKeys();
	}
	
	private boolean isStateFound(Set<State> state) {

		return true;
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
			} else if (secondMax == null || qvalue.q > secondMax.q) {
				secondMax = qvalue;
			}
		}
		values.add(firstMax);
		values.add(secondMax);
		return values;
	}

	private Map<List<Integer>, String> readFile(String src) {
		Map<List<Integer>, String> map = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(src))) {
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] values = line.split(COMMA_DELIMITER);
				String actions = "";
				for (int i = 1; i < values.length; i++) {
					if (!actions.isBlank() || !actions.isEmpty()) {
						actions += " -/ ";
					}
					actions += values[i];
				}
				map.put(getStates(values[0]), actions);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	private List<Integer> getStates(String coord) {
		List<Integer> states = new ArrayList<Integer>();
		for (String state : coord.split("-")) {
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

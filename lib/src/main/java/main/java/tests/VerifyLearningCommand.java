package main.java.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Reporter;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import main.java.burlap.QLearningAlgorithm;

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
		String src 					= args[0].getString();
		StringBuilder sb 			= new StringBuilder();
		QLearningAlgorithm learning = QLearningAlgorithm.getInstance(args, context);
		// tabelas q
		Map<State, List<QValue>> qtable = learning.getState();
		Map<List<Integer>, String> map 	= readFile(src);
		
		sb.append("\nCaminho do arquivo: " + src + "\n" + map.keySet() + "\n");
		sb.append("\nTabela Local: " + src + "\n" + qtable.keySet() + "\n");
		sb.append(verifyStates(qtable, map));
		
		return sb.toString();
	}
	
	private StringBuilder verifyStates(Map<State, List<QValue>> qtable, Map<List<Integer>, String> map) {
		Set<State> estado = qtable.keySet();
		Set<List<Integer>> chaves = map.keySet();
		
		StringBuilder sb = new StringBuilder();
		
		Iterator<State> itr = estado.iterator();
		Iterator<List<Integer>> itr_chave = chaves.iterator();
		
		while(itr.hasNext()) {
			State elemento = itr.next();
			while(itr_chave.hasNext()) {
				List<Integer> elemento_chave = itr_chave.next();
				double cord_x = (double) elemento.get("XCOR");
				double cord_y = (double) elemento.get("YCOR");;
				if(Double.compare(cord_x, elemento_chave.get(0)) == 0 && Double.compare(cord_y, elemento_chave.get(1)) == 0) {
					sb.append("Possui As coordenadas identicas\nValor do csv: " + elemento_chave + "\nValor da QTable: " + cord_x + " " + cord_y + "\n");
					sb.append("Valor da qtable" + qtable.get(elemento).get(0).q + "\n");
				}
//				else {
//					sb.append("Não achei :C" + "\nValor do csv: " + elemento_chave + "\nValor da QTable: " + cord_x + " " + cord_y + "\n");
//				}
			}
			itr_chave = chaves.iterator();
		}
		return sb;
	}
	
	private Map<List<Integer>, String> readFile(String src){
		Map<List<Integer>, String> map 	= new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(src))) {
		    String line = br.readLine();
		    while ((line = br.readLine()) != null) {
		    	String[] values = line.split(COMMA_DELIMITER);
		    	map.put(getStates(values[0]), values[1]);
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

}

package main.java.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
		
		sb.append("\nCaminho: " + map.keySet());
		
		return sb.toString();
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

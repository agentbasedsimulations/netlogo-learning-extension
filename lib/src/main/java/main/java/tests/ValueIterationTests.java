package main.java.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

public class ValueIterationTests implements FullStateModel{
	private final Map<String, List<State>> transitionsRecorded = new HashMap<>();
		
	public void recordTransition(State s, Action a, State sPrime) {
		String key = buildKey(s, a);
		transitionsRecorded
			.computeIfAbsent(key, k -> new ArrayList<>())
			.add(sPrime);
	}
	
	@Override
	public State sample(State s, Action a) {
		List<StateTransitionProb> transitions = stateTransitions(s, a);
        if (transitions.isEmpty()) return s;
        // Para ambientes determinísticos, só há uma transição
        return (State)transitions.get(0);
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		String key = buildKey(s, a);
        List<State> observed = transitionsRecorded.getOrDefault(key, new ArrayList<>());

        if (observed.isEmpty()) {
            return new ArrayList<>();
        }

        // Conta quantas vezes cada estado-destino foi observado
        Map<String, int[]> counts = new HashMap<>();
        Map<String, State> stateByKey = new HashMap<>();

        for (State dest : observed) {
            String destKey = stateToString(dest);
            counts.computeIfAbsent(destKey, k -> new int[]{0})[0]++;
            stateByKey.put(destKey, dest);
        }

        // Converte contagens em probabilidades
        List<StateTransitionProb> result = new ArrayList<>();
        int total = observed.size();

        for (Map.Entry<String, int[]> entry : counts.entrySet()) {
            double prob = (double) entry.getValue()[0] / total;
            State dest = stateByKey.get(entry.getKey());
            result.add(new StateTransitionProb(dest, prob));
        }

        return result;
	}

	private String buildKey(State s, Action a) {
        return stateToString(s) + "::" + a.actionName();
    }
	
	private String stateToString(State s) {
        // Constrói uma string ordenada das variáveis do estado
        List<String> parts = new ArrayList<>();
        for (Object key : s.variableKeys()) {
            parts.add(key + "=" + s.get(key));
        }
        java.util.Collections.sort(parts);
        return parts.toString();
    }
}

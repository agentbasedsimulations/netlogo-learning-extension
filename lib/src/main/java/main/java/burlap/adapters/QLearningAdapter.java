package main.java.burlap.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import main.java.burlap.util.BurlapUtils;

public class QLearningAdapter extends QLearning {

	public QLearningAdapter(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit,
			double learningRate, int maxEpisodeSize) {
		super(domain, gamma, hashingFactory, qInit, learningRate, maxEpisodeSize);
	}

	public QLearningAdapter(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit,
			double learningRate, Policy learningPolicy, int maxEpisodeSize) {
		super(domain, gamma, hashingFactory, qInit, learningRate, learningPolicy, maxEpisodeSize);
	}

	public QLearningAdapter(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit,
			double learningRate) {
		super(domain, gamma, hashingFactory, qInit, learningRate);
	}

	public QLearningAdapter(SADomain domain, double gamma, HashableStateFactory hashingFactory, QFunction qInit,
			double learningRate, Policy learningPolicy, int maxEpisodeSize) {
		super(domain, gamma, hashingFactory, qInit, learningRate, learningPolicy, maxEpisodeSize);
	}

	
	public String getLearningDetails() {
		StringBuilder sb = new StringBuilder("Q-Learning Details").append(System.lineSeparator());
		sb.append("learning rate: ").append(BurlapUtils.toString(super.learningRate)).append(System.lineSeparator());
		sb.append("discount factor: ").append(super.gamma).append(System.lineSeparator());
		sb.append("Q table").append(System.lineSeparator());

		for (HashableState hs : super.qFunction.keySet()) {
			State s = hs.s();
			sb.append("state: ").append(s).append("; actions values: ");
			
			List<QValue> values = super.qFunction.get(hs).qEntry; // QValue is a tuple: state s, action a, qValue q
			for (QValue qv : values) {
				sb.append(qv.a).append("=").append(qv.q).append("; ");
			}
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

	// METODO ADICIONADO, PARA REVISÃO DEPOIS 
	// Autor: Mateus Rissardi, Data: 11/05/2026
	public Map<State, List<QValue>> getQTable() {
		Map<State, List<QValue>> qtable = new HashMap<State, List<QValue>>();
		for (HashableState hs : super.qFunction.keySet()) {
			State s = hs.s();
			
			List<QValue> values = super.qFunction.get(hs).qEntry; // QValue is a tuple: state s, action a, qValue q
			qtable.put(s, values);
		}
		return qtable;
	}
}

package main.java.burlap;

// Java precisa de objetos, para especificar é usado a interface Serializable antes deles serem convertidos
// para byte e escrever o arquivo. Se os estados forem guardados a partir da Q-Table (AgentState) eles não
// são marcados como Serializable, e a exportação vai falhar com o erro NotSerializableException.
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nlogo.api.AgentException;
import org.nlogo.api.Context;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import main.java.model.AgentLearning;
import main.java.model.Session;

/**
 * Agent State Class
 * 
 * @author Eloisa Bazzanella
 * @since april, 2022
 */
public class AgentState implements MutableState, Serializable {

	private Context context;
	private Map<String, Object> state;

//	Para que funcione o import-file (command), Java ou a Burlap usam o reflexo do arquivo para 
//	reconstruir os objetos. Esse mecanismo depende em chamar um construtor sem argumento para 
//	iniciar um objeto vazio, antes de preencher as variáveis. Sem que receba falhas.
	public AgentState() {
		this.state = new HashMap<>();
	}
	
	public AgentState(Context context) throws AgentException { 
		this.context = context;
		this.state = new HashMap<>();

		AgentLearning agent = Session.getInstance().getAgent(context.getAgent());

		this.state = agent.getState(context);
	}

// 	Para que o Serializable funcione ele precisa acessar os getters e setters comuns pelo 
// 	JavaBean, assim consegue ler e escrever as estruturas de forma segura no processo.
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Map<String, Object> getState() {
		return state;
	}

	public void setState(Map<String, Object> state) {
		this.state = state;
	}
	
	@Override
	public MutableState set(Object variableKey, Object value) { 
		for (String s : state.keySet()) {
			if (variableKey.equals(s)) {
				state.replace(s, (Double) value);
			}
		}

		return this;
	}

	@Override
	public Object get(Object variableKey) { 
		return state.get(variableKey);
	}

	@Override
	public State copy() {
		try {
			return new AgentState(context);
		} catch (AgentException ex) {
			Logger.getLogger(AgentState.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public List<Object> variableKeys() {
		List<String> vars = new ArrayList<>();

		for (String s : state.keySet()) {
			vars.add(s);
		}

		return new ArrayList<Object>(vars);
	}

	@Override
	public String toString() {
		return state.toString();
	}

}
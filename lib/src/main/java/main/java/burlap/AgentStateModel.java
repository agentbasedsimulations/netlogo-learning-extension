package main.java.burlap;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nlogo.api.AgentException;
import org.nlogo.api.AnonymousCommand;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.statehashing.HashableState;
import main.java.burlap.adapters.QLearningAdapter;
import main.java.model.AgentLearning;
import main.java.model.Session;
import main.java.tests.ValueIterationTests;

/**
 * Agent State Model Class
 * @author Eloisa Bazzanella
 * @since  april, 2022
 */
public class AgentStateModel implements FullStateModel {

    private Argument[] args;
    private Context    context;
    private QLearning  learning;
    private SarsaLam   sarsa;
    private CriticImplementation critic;
    // VARIÁVEL ADICIONADA, PARA FUTURA REVISÃO
    // Autor: Mateus Rissardi, Data: 25/03/2026
    private ValueIterationTests transitionModel = null;
    
    public AgentStateModel(Argument[] args, Context context) {
        this.args = args;
        this.context = context;
    }
    
    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        return new ArrayList<StateTransitionProb>();
    }
    
    // METODO ADICIONADO, PARA FUTURA REVISÃO
    // Autor: Mateus Rissardi, Data: 25/03/2026
    public void setTransitionModel(ValueIterationTests transitionModel) {
        this.transitionModel = transitionModel;
    }

    @Override
    public State sample(State s, Action a) {        
        AgentState state = null;
        try {
            state = new AgentState(context);
        } catch (AgentException ex) {
            Logger.getLogger(AgentStateModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        AgentLearning agent =  Session.getInstance().getAgent(context.getAgent());
        String actionExecute = a.actionName();
       
        for(AnonymousCommand action : agent.actions) {
            if(actionExecute.equals(action.toString())){
                action.perform(context, args);  
            }
        }
      
        if(agent.algorithm.equals("qlearning")) {
        	System.out.println( state.toString());
            for (QValue qvalue : learning.qValues(state)) {
                System.out.println("action: " + qvalue.a.actionName() + " , value: " + qvalue.q);  
            }      
      }
        
        
        
        if(agent.algorithm.equals("sarsa-lambda")) {
        	System.out.println( state.toString());
        	for(QValue qvalue : sarsa.qValues(state)) {
                System.out.println("action: " + qvalue.a.actionName() + " , value: " + qvalue.q);
        	}
        }
        
        if(agent.algorithm.equals("actor-critic")) {
           
            for(HashableState v : critic.vIndex.keySet()) {
                String ss = "asm";
                for(Object aa : v.s().variableKeys()) {
                    ss += aa + ": " + v.s().get(a) + ", ";
                }
                System.out.println(s + ": " + critic.getV(v).v);
            }
            System.out.println("-------------------------------");
        }
        AgentState sPrime = null;
        try {
            sPrime = new AgentState(context);
        } catch (AgentException ex) {
            Logger.getLogger(AgentStateModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (transitionModel != null) {
            transitionModel.recordTransition(state, a, sPrime);
        }
       
        return state;
    }
    

    
    public void setQLearning(QLearning learning) {
        this.learning = learning;
    }
    
    public void setSarsa(SarsaLam sarsa) {
        this.sarsa = sarsa;
    }

    public void setCritic(CriticImplementation critic) {
        this.critic = critic;
    }
}

















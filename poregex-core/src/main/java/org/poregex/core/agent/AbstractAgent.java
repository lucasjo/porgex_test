package org.poregex.core.agent;

import org.poregex.core.service.AgentState;



public abstract class AbstractAgent implements Agent {
	
	private String name;
	private AgentState agentState = AgentState.IDLE;
	
	@Override
	public void start() {
		// TODO Auto-generated method stub

		agentState = AgentState.START;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		agentState = AgentState.STOP;

	}

	@Override
	public AgentState getAgentState() {
		// TODO Auto-generated method stub
		return agentState;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		this.name = name;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}


	
}

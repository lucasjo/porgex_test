package org.poregex.core.service;

public enum AgentState {

	IDLE, START,STOP, ERROR;
	
	public static final AgentState[] START_OR_ERROR = new AgentState[]{START, ERROR};
	public static final AgentState[] STOP_OR_ERROR = new AgentState[]{STOP, ERROR};
}

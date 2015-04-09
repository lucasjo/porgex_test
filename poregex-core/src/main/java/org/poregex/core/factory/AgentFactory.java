package org.poregex.core.factory;

import org.poregex.core.agent.Agent;
import org.poregex.core.exception.PorgexException;

public interface AgentFactory {

	public Agent create(String agentName, String type) throws PorgexException;
	
	public Class<? extends Agent> getClass(String type) throws PorgexException; 
	
}

/**
 * 
 */
package org.poregex.core.factory;

import java.util.Locale;

import org.poregex.core.agent.Agent;
import org.poregex.core.agent.AgentType;
import org.poregex.core.exception.PorgexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * @author kikimans
 *
 */
public class DefalutAgentFactory implements AgentFactory {

	private static final Logger logger = LoggerFactory.getLogger(DefalutAgentFactory.class);
	/* (non-Javadoc)
	 * @see org.poregex.core.factory.AgentFactory#create(java.lang.String, java.lang.String)
	 */
	@Override
	public Agent create(String agentName, String type) throws PorgexException {
		// TODO Auto-generated method stub
		Preconditions.checkNotNull(agentName, "agentServiceName");
		Preconditions.checkNotNull(type, "agentServicetype");
		
		logger.info("Create Instance of AgentService {}, type {}", agentName, type);
		
		Class<? extends Agent> agentClass = getClass(type);
		
		try {
			Agent agent = agentClass.newInstance();
			agent.setName(agentName);
			return agent;
		} catch (Exception e) {
			// TODO: handle exception
			throw new PorgexException("Unable to create agent : {0}, type: {1}, class: {2}", agentName, type, agentClass);
		}
	}

	/* (non-Javadoc)
	 * @see org.poregex.core.factory.AgentFactory#getClass(java.lang.String)
	 */
	@Override
	public Class<? extends Agent> getClass(String type) throws PorgexException {
		// TODO Auto-generated method stub
		String agentClassName = type;
		AgentType agentType = AgentType.OTHER;
		try {
			agentType = AgentType.valueOf(type.toUpperCase(Locale.ENGLISH));
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
			logger.error("Agent Type {} is a Customer type", type, e);
		}
		if(!agentType.equals(AgentType.OTHER)){
			agentClassName = agentType.getAgentClassName();
		}
		
		try {
			return (Class<? extends Agent>) Class.forName(agentClassName);
		} catch (Exception e) {
			new PorgexException("Unable to load agentService Type: {0}, class: {}", type, agentClassName, e);
		}
		return null;
		
	}

}

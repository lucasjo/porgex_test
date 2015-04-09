package org.poregex.core.agent;

public enum AgentType {

	OTHER(null),
	USERACTION("org.poregex.core.openshift.OpenShiftUserAction");
	
	private final String agentClassName;

	private AgentType(String agentClassName) {
	    this.agentClassName = agentClassName;
	}

	public String getAgentClassName() {
		return agentClassName;
	}
	
	
}

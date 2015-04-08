package org.poregex.core.service;

public class Supervisor {

	public AgentSupervisorPolicy policy;
	public AgentStatus status;
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Supervisor [policy=");
		builder.append(policy);
		builder.append(", status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
	
	
}

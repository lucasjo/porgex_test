package org.poregex.core.service;


public class AgentStatus {

	public AgentState desiredState;
    public int failures;
    public boolean discard;
    public volatile boolean error;
	public AgentState lastSeenState;
	public Long firstSeen;
	public Long lastSeen;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AgentStatus [desiredState=");
		builder.append(desiredState);
		builder.append(", failures=");
		builder.append(failures);
		builder.append(", discard=");
		builder.append(discard);
		builder.append(", error=");
		builder.append(error);
		builder.append(", lastSeenState=");
		builder.append(lastSeenState);
		builder.append(", firstSeen=");
		builder.append(firstSeen);
		builder.append(", lastSeen=");
		builder.append(lastSeen);
		builder.append("]");
		return builder.toString();
	}
    
	
    
    
}

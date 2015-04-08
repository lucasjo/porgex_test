package org.poregex.core.service;

public abstract class AgentSupervisorPolicy {

	abstract boolean isValid(AgentService service, AgentStatus status);
	
	public static class AlwaysRestartPolicy extends AgentSupervisorPolicy{

		@Override
		boolean isValid(AgentService service, AgentStatus status) {
			// TODO Auto-generated method stub
			return true;
		}
		
	}
	
	public static class OnlyOncePolicy extends AgentSupervisorPolicy{

		@Override
		boolean isValid(AgentService service, AgentStatus status) {
			// TODO Auto-generated method stub
			return status.failures == 0;
		}
		
	}
}

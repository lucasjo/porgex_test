/**
 * 
 */
package org.poregex.core;

import java.util.ArrayList;
import java.util.List;

import org.poregex.core.openshift.OpenShiftUserAction;
import org.poregex.core.service.AgentService;
import org.poregex.core.service.AgentServiceSupervisor;
import org.poregex.core.service.AgentState;
import org.poregex.core.service.AgentSupervisorPolicy;

import com.google.common.collect.Lists;

/**
 * @author kikimans
 *
 */
public class Application {
	
	private List<AgentService> services;
	private AgentServiceSupervisor supervisor;
	
	
	public Application(){
		this(new ArrayList<AgentService>(0));
	}

	public Application(List<AgentService> services) {		
		this.services = services;
		this.supervisor = new AgentServiceSupervisor();
	}
	
	public synchronized void start(){
		
		for(AgentService service : services){
			supervisor.supervisor(service, new AgentSupervisorPolicy.AlwaysRestartPolicy(), AgentState.START);
		}
	}
	
	public synchronized void stop(){
		supervisor.stop();
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String filepath = "C:\\Users\\kikimans\\.porgex\\user_action.log";
		
		OpenShiftUserAction agent = new OpenShiftUserAction(filepath);
		List<AgentService> components = Lists.newArrayList();
		
		agent.setName("OpenShiftLogName");
		components.add(agent);
		
		Application application = new Application(components);
		
		application.start();
		

	}

}

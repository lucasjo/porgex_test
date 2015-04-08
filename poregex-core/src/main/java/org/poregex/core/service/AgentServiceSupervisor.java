/**
 * 
 */
package org.poregex.core.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.poregex.core.exception.PorgexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;


/**
 * @author kikimans
 *
 */
public class AgentServiceSupervisor implements AgentService {
	
	private static final Logger logger = LoggerFactory.getLogger(AgentServiceSupervisor.class);
	
	private Map<AgentService, Supervisor> agentSupervisor;
	private Map<AgentService, ScheduledFuture<?>> callerFuture;
	
	private ScheduledThreadPoolExecutor callerService;
	
	private AgentState agentState;
	private AgentPurge agentPurge;
	private boolean needToAgentPurge;
	
	
	

	public AgentServiceSupervisor() {
		agentState = AgentState.IDLE;
		agentSupervisor = new HashMap<AgentService, Supervisor>();
		callerFuture = new HashMap<AgentService, ScheduledFuture<?>>();
		
		callerService = new ScheduledThreadPoolExecutor(
				1, new ThreadFactoryBuilder().setNameFormat("AgentServiceSupervisor-" + Thread.currentThread().getId() + "-$d")
				.build());
		callerService.setMaximumPoolSize(20);
		callerService.setKeepAliveTime(30, TimeUnit.SECONDS);
		agentPurge = new AgentPurge();
		needToAgentPurge = false;
	}

	/* (non-Javadoc)
	 * @see org.porgex.service.AgentService#start()
	 */
	@Override
	public synchronized void start() {
		logger.info("Stating AgentService supervisor {}", Thread.currentThread().getId());
		callerService.scheduleWithFixedDelay(agentPurge, 2, 2, TimeUnit.HOURS);
		
		agentState = AgentState.START;
		
		logger.info("AgentService supervisor stated");

	}

	/* (non-Javadoc)
	 * @see org.porgex.service.AgentService#stop()
	 */
	@Override
	public synchronized void stop() {
		logger.info("Stopping AgentService supervisor {}", Thread.currentThread().getId());
		
		if(callerService != null){
			callerService.shutdown();
			try {
				callerService.awaitTermination(10, TimeUnit.SECONDS);
			} catch (Exception e) {
				logger.error("Interrupted while waiting for caller service to stop");
			}
			if(callerService.isTerminated()){
				callerService.shutdownNow();
				try {
					while(!callerService.isTerminated()) callerService.awaitTermination(10, TimeUnit.SECONDS);
				} catch (Exception e) {
					logger.error("Interrupted while waiting for caller service to stop");
				}
			}
		}
		
		for(Entry<AgentService, Supervisor> entry : agentSupervisor.entrySet()){
			if (entry.getKey().getAgentState().equals(AgentState.START)) {
				
		        entry.getValue().status.desiredState = AgentState.STOP;
		        entry.getKey().stop();
		    }
		}
		
		if(agentState.equals(AgentState.START)){
			agentState = AgentState.STOP;
		}
		
		agentSupervisor.clear();
		callerFuture.clear();
		logger.info("AgentService supervisor stop");		

	}
	
	public synchronized void fail(){
		agentState = AgentState.ERROR;
	}
	
	public synchronized void supervisor(AgentService service, AgentSupervisorPolicy policy, AgentState state){
		if(callerService.isShutdown() || callerService.isTerminated() || callerService.isTerminating()){
			throw new PorgexException("Supervisor call on {0} after shutdown has beean initated. {1} will not be started", service, service);
		}
		
		Preconditions.checkArgument(!agentSupervisor.containsKey(service),  "Refusing to supervise " + service + " more than once");
		
		if(logger.isDebugEnabled()){
			logger.debug("Supervising service:{} policy:{} desiredState:{}",
			          new Object[] { service, policy, state });
		}
		
		Supervisor supervisor = new Supervisor();
		supervisor.status = new AgentStatus();
		
		supervisor.policy = policy;
		supervisor.status.desiredState = state;
		supervisor.status.error = false;
		
		CallerRunnable callerRunnable = new CallerRunnable();
		callerRunnable.agentService = service;
		callerRunnable.supervisor = supervisor;
		callerRunnable.callserService = callerService;
		
		agentSupervisor.put(service, supervisor);
		
		ScheduledFuture<?> future = callerService.scheduleWithFixedDelay(
				 callerRunnable, 0, 3, TimeUnit.SECONDS);
	    callerFuture.put(service, future);
		
	}
	
	public synchronized void unsupervise(AgentService service) {

	    Preconditions.checkState(agentSupervisor.containsKey(service),
	        "Unaware of " + service + " - can not unsupervise");

	    logger.debug("Unsupervising service:{}", service);

	    synchronized (service) {
	    Supervisor supervisor = agentSupervisor.get(service);
	    supervisor.status.discard = true;
	      this.setDesiredState(service, AgentState.STOP);
	      logger.info("Stopping component: {}", service);
	      service.stop();
	    }
	    agentSupervisor.remove(service);
	    //We need to do this because a reconfiguration simply unsupervises old
	    //components and supervises new ones.
	    callerFuture.get(service).cancel(false);
	    //purges are expensive, so it is done only once every 2 hours.
	    needToAgentPurge = true;
	    callerFuture.remove(service);
	  }
	

	private synchronized void setDesiredState(AgentService service, AgentState state) {
		// TODO Auto-generated method stub
		Preconditions.checkState(agentSupervisor.containsKey(service),
		        "Unaware of " + service + " - can not set desired state to "
		            + state);

	    logger.debug("Setting desiredState:{} on service:{}", state,
	    		service);

	    Supervisor supervisoree = agentSupervisor.get(service);
	    supervisoree.status.desiredState = state;
	}

	
	public class CallerRunnable implements Runnable{

		public ScheduledThreadPoolExecutor callserService;
		public Supervisor supervisor;
		public AgentService agentService;

		@Override
		public void run() {
			
			logger.debug("checking process:{} supervisoree:{}", agentService, supervisor);
			long now = System.currentTimeMillis();		
			
			try {
				if(supervisor.status.firstSeen == null){
					supervisor.status.firstSeen = now;
				}
				supervisor.status.lastSeen = now;
				synchronized (agentService) {
					if(supervisor.status.discard){
						logger.info("Agent Component has already been stopped {}", agentService);
					}else if(supervisor.status.error){
						logger.info("AgentComponent {} is in error state and PorgeX will not attempt to change its state", agentService);
						return;
					}
					
					supervisor.status.lastSeenState = agentService.getAgentState();
					
					logger.info("agentService state : {} {}", agentService.getAgentState(), supervisor.status.desiredState);
					
					if(!agentService.getAgentState().equals(supervisor.status.desiredState)){
						logger.debug("Want to transition {} from {} to {} (failures:{})",
				                new Object[] { agentService, supervisor.status.lastSeenState,
								supervisor.status.desiredState,
								supervisor.status.failures });
						logger.info("supervisor.status.desiredState : " + supervisor.status.desiredState);
						switch(supervisor.status.desiredState){
							case START:
								try {
									logger.info("START");
									agentService.start();									
								} catch (Throwable e) {
									 logger.error("Unable to start " + agentService
						                      + " - Exception follows.", e);
									 if(e instanceof Error){
										 supervisor.status.desiredState = AgentState.STOP;
										 try {
											agentService.stop();
											logger.warn("Component {} stopped, since it could not be"
							                          + "successfully started due to missing dependencies",
							                          agentService);
										} catch (Throwable e1) {
											logger.error("Unsuccessful attempt to "
							                          + "shutdown component: {} due to missing dependencies."
							                          + " Please shutdown the agent"
							                          + "or disable this component, or the agent will be"
							                          + "in an undefined state.", e1);
											supervisor.status.error = true;
											if(e1 instanceof Error){
												throw(Error) e1;
											}
										}
										 supervisor.status.failures++;
									 }
									 break;
								}
							case STOP:
								try {
					                  agentService.stop();
					                } catch (Throwable e) {
					                  logger.error("Unable to stop " + agentService
					                      + " - Exception follows.", e);
					                  if (e instanceof Error) {
					                    throw (Error) e;
					                  }
					                  supervisor.status.failures++;
					                }
					                break;
					              default:
					                logger.warn("I refuse to acknowledge {} as a desired state",
					                		supervisor.status.desiredState);						
						}
						
						if(!supervisor.policy.isValid(agentService, supervisor.status)){
							logger.error(
									"Policy {} of {} has been violated - supervisor should exit!",
									supervisor.policy, agentService);
						}
						
					}
				}
			} catch (Throwable et) {
				logger.error("Unexpected Error" , et);
				
			}
			logger.info("Status check Complete");
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.porgex.service.AgentService#getAgentState()
	 */
	@Override
	public AgentState getAgentState() {
		// TODO Auto-generated method stub
		return agentState;
	}
	
	
	public class AgentPurge implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			logger.info("needToAgentPurge " + needToAgentPurge);
			if(needToAgentPurge){
				callerService.purge();
				needToAgentPurge = false;
		      }
		}

	}

}

/**
 * 
 */
package org.poregex.core.openshift;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.poregex.core.agent.AbstractAgent;
import org.poregex.core.service.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * @author kikimans
 *
 */
public class OpenShiftUserAction extends AbstractAgent {
	
private static final Logger logger = LoggerFactory.getLogger(OpenShiftUserAction.class);
	
	private static final String SHELL="shell";
	
	private static final String COMMAND = "tail -F ";
	
	private ExecutorService executor;
	private Future<?> runnerFuture;
	
	private String filepath;	
	
	private AgentState agentstate = AgentState.IDLE;
	
	private UserActionRunner runner;	
	
	@Override
	public void start() {
		
		logger.info("OpenShift User Action Log Runner Start");
		executor = Executors.newSingleThreadExecutor();
		
		runner = new UserActionRunner(filepath);		
		runnerFuture = executor.submit(runner);	
		
		super.start();
		

	}

	

	public OpenShiftUserAction(String filepath) {
		
		this.filepath = filepath;
		
	}



	/* (non-Javadoc)
	 * @see org.porgex.service.AgentService#stop()
	 */
	@Override
	public void stop() {
		//logger.info("Stopping agentService:{}", this.getName());
	   
	    if(runner != null){
	    	runner.kill();
	    }

	    if (runnerFuture != null) {
	      logger.debug("Stopping exec runner");
	      runnerFuture.cancel(true);
	      logger.debug("User Action Agent Service stopped");
	    }
	    executor.shutdown();

	    while (!executor.isTerminated()) {
	      logger.debug("Waiting for exec executor service to stop");
	      try {
	        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
	      } catch (InterruptedException e) {
	        logger.debug("Interrupted while waiting for exec executor service "
	            + "to stop. Just exiting.");
	        Thread.currentThread().interrupt();
	      }
	    }

	    agentstate = AgentState.STOP;
	    super.stop();

	    logger.debug("User Action Agent Service Stopped");
		

	}

	/* (non-Javadoc)
	 * @see org.porgex.service.AgentService#getAgentState()
	 */
	@Override
	public AgentState getAgentState() {
		// TODO Auto-generated method stub
		return super.getAgentState();
	}
	
	public static class UserActionRunner implements Runnable{
		
		private final String filepath;
		private ExecutorService tailer_executor;
		private Future<?> tailerFuture;
		private Process process;
		
		private Map<String,String> logData = new HashMap<String,String>();
		

		public UserActionRunner(String filepath) {
			// TODO Auto-generated constructor stub
			this.filepath = filepath;
		}

		public int kill() {
			if(process != null){
				synchronized (process) {
					process.destroy();
					
					try {
						return process.waitFor();						
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				return Integer.MIN_VALUE;
			}
			return Integer.MIN_VALUE/2;
			
			
		}
		
		private static String[] formulateShellCommand(String shell, String command) {
		      String[] shellArgs = shell.split("\\s+");
		      String[] result = new String[shellArgs.length + 1];
		      System.arraycopy(shellArgs, 0, result, 0, shellArgs.length);
		      result[shellArgs.length] = command;
		      return result;
		}

		@Override
		public void run() {
			/*
			 * process tail 실행
			 */
			
			String tailCommand = COMMAND+filepath;
			BufferedReader reader = null;
			String line = null;
			
			String exitcode = "unknown";
			
			try {
				String[] commandArgs = tailCommand.split("\\s+");
				process = new ProcessBuilder(commandArgs).start();
				
				reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charsets.UTF_8));
				
				while((line=reader.readLine()) != null){
					synchronized (logData) {
						logData = convertLogMap(line);
						flushLogData(logData);
					}
				}
				
				synchronized (logData) {
					if(!logData.isEmpty()){
						flushLogData(logData);
					}
				}
				
			} catch (Exception e) {
				logger.error("Failed while running command: " + tailCommand, e);
		        if(e instanceof InterruptedException) {
		            Thread.currentThread().interrupt();
		        }
			}finally{
				if(reader != null){
					try {
						reader.close();
					} catch (Exception e2) {
						logger.error("Failed to close reader for OpenShiftUserAction Runner", e2);
					}
				}
				exitcode = String.valueOf(kill());
			}
			
		}
		
		private void flushLogData(Map<String, String> logData2) {
			/**
			 * 몽고DB 에 입력 프로세스 셋팅
			 */
			logger.info("flushLogData {} : ", logData2);		
			
		}

		public Map<String,String> convertLogMap(String line){
			logger.info("Convert ok");
			Map<String,String> logMaps = Maps.newHashMap();
			
			List<String> logs = Lists.newArrayList(Splitter.on(" ").trimResults().omitEmptyStrings().split(line));
			List<String> convertorLogs = new ArrayList<String>();
			
			int idx=0;
			for(String s: logs){			
				if(s.contains("DOMAIN") || s.contains("SCOPE") || s.contains("PROVIDER")){				
					for(int i=idx+1; i < logs.size(); i++){
						s += " " + logs.get(i);
					}
					convertorLogs.add(s);
					break;
				}else{
					convertorLogs.add(s);
				}
				idx +=1;
				
			}
			
			for(String log : convertorLogs){
				List<String> clogs = Lists.newArrayList(Splitter.on("=").trimResults().omitEmptyStrings().split(log));
				
				if(clogs.size() != 2){
					clogs.add(1, "-");
				}
				logMaps.put(clogs.get(0), clogs.get(1));			
			}
			
			return logMaps;
		}
		
	}
	
	
	
	
	

}

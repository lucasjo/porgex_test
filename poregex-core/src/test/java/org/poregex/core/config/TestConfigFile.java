package org.poregex.core.config;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class TestConfigFile {
	
	private BufferedReader br = null;
	private File file;
	
	@Before
	public void setUp(){
		file = new File(System.getProperty("user.home") + "\\.porgex\\porgex.conf");
	}

	@Test
	public void testPropertiesConfig() throws IOException {
		br = new BufferedReader(new FileReader(file));
		Properties properties = new Properties();
	    properties.load(br);
	    
	    System.out.println(properties);
	    
	    Map<String, String> result = Maps.newHashMap();
	    Enumeration<?> propertyNames = properties.propertyNames();
	    while (propertyNames.hasMoreElements()) {
	      String name = (String) propertyNames.nextElement();
	      String value = properties.getProperty(name);
	      result.put(name, value);
	    }
	    
	    for(String name : result.keySet()) {
	    	System.out.println(name);
	    	System.out.println(result.get(name));
	    }
	        
	    
	    System.out.println(result);
	}
	
	@Test
	public void test_user_home(){
		System.out.println(System.getProperty("user.home") + "\\.porgex\\porgex.conf");
	}

}

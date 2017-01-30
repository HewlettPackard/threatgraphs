//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.

package com.hp.hpl.bd4s.wfm.template;

/**
 * @author  marco casassa mont
 *
 */

public class RunnableTemplate implements Runnable {

	protected WFMTemplate ctr;
	protected Vertica db;
	protected String moduleName = "templateModule";


	protected boolean startRun() {
		System.out.println(ctr.wfmType+": Module "+this.moduleName+" started.");
		return true;
	}

	protected boolean stopRun() {
		System.out.println(ctr.wfmType+": Module "+this.moduleName+" stopped.");
		return true;
	}


	@Override
	public void run() {
		this.startRun();
		// do fancy analytics
		this.stopRun();
	}

	
}
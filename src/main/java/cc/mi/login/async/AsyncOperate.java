package cc.mi.login.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cc.mi.core.task.base.AsyncTask;

public enum AsyncOperate {
	INSTANCE;
	
	private final ExecutorService executor = Executors.newFixedThreadPool(1<<3);
	
	private AsyncOperate(){}
	
	public void submitTask(AsyncTask task) {
		executor.submit(task);
	}
}

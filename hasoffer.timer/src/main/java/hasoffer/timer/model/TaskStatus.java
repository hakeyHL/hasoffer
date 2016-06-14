package hasoffer.timer.model;


import hasoffer.core.persistence.dbm.osql.Identifiable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskStatus<T extends Identifiable> {

	private BlockingDeque<T> objectQueue;

	private Set<Long> processedIdSet;

	private AtomicBoolean findTaskFinished;

	public TaskStatus() {
		objectQueue = new LinkedBlockingDeque<T>();
		findTaskFinished = new AtomicBoolean(false);
		processedIdSet = new HashSet<Long>();
	}

	public int processedCount() {
		return processedIdSet.size();
	}

	public boolean containsT(Long tid) {
		return processedIdSet.contains(tid);
	}

	public boolean isFindTaskFinished() {
		return findTaskFinished.get();
	}

	public void findTaskFinished() {
		findTaskFinished.set(true);
	}

	public void addJob(T t) {
		objectQueue.offer(t);
	}

	public T getJob() {
		return objectQueue.poll();
	}

	public int queueSize() {
		return objectQueue.size();
	}

	public void addIdToSet(Long id) {
		this.processedIdSet.add(id);
	}
}

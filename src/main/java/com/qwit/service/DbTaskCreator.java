package com.qwit.service;

import java.util.List;

import com.qwit.domain.MObject;
import com.qwit.domain.Task;
import com.qwit.domain.Tree;

public class DbTaskCreator extends DbObjMCreator {

	Task task;
	public DbTaskCreator (){
		setSql("SELECT d FROM Task d WHERE d.task= :task AND d.type=:type AND d.taskvar=:taskvar");
		task = new Task();
	}
	public Tree createReadTree() {
		log.debug(1);
		create();
		log.debug(2);
		MObject read = read();
		log.debug(3);
		Tree find = em.find(Tree.class, read.getId());
		log.debug(4);
		find.setMtlO(read);
		return find;
	}
	@Override
	public void create() {
		log.info("----- 1");
		log.debug(task);
		log.debug(task.getDuration());
		if(null==task.getDuration()){
			task.setDuration(1);
		}
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(task.getTask())
		.setParam(task.getType())
		.setParam(task.getTaskvar())
		.setParam(task.getDuration().toString())
		;
		log.info(2);
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
		Task pvDb=null;
		List<Task> dL = em.createQuery(getSql())
		.setParameter("task",		task.getTask())
		.setParameter("type",		task.getType())
		.setParameter("taskvar",	task.getTaskvar())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	public DbTaskCreator setMtlO(Task task) {
		this.task=task;
		return this;
	}
	public DbTaskCreator setTask(String task) {
		this.task.setTask(task);
		return this;
	}
	public DbTaskCreator setType(String type) {
		task.setType(type);
		return this;
	}
	public DbTaskCreator setTaskvar(String taskvar) {
		task.setTaskvar(taskvar);
		return this;
	}
	@Override
	public boolean isNull() {
		if(!task.hasTask()) return true;
		return false;
	}
	/*
	public DbObjMCreator setMtlO(Task mtlO) {
		task=mtlO;
		return this;
	}
	 */

}

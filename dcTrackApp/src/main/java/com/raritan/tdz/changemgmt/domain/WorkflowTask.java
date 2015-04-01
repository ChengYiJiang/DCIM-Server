package com.raritan.tdz.changemgmt.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * A domain object representing a single dcTrack workflow task.
 * @author Andrew Cohen
 * @version 3.0
 */
@Entity
@Table(name = "dct_workflow_tasks")
public class WorkflowTask {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_workflow_tasks_seq")
	@SequenceGenerator(name="dct_workflow_tasks_seq", sequenceName="dct_workflow_tasks_workflow_task_id_seq", allocationSize=1)
	@Column(name="workflow_task_id")
	private long id;
	
	@Column(name = "workflow_task_name")
	private String name;
	
	@Column(name = "workflow_task_description")
	private String description;
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

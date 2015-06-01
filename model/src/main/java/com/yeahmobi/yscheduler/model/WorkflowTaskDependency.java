package com.yeahmobi.yscheduler.model;

import java.util.Date;

public class WorkflowTaskDependency {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column workflow_task_dependency.id
     *
     * @mbggenerated
     */
    private Long id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column workflow_task_dependency.workflow_detail_id
     *
     * @mbggenerated
     */
    private Long workflowDetailId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column workflow_task_dependency.dependency_task_id
     *
     * @mbggenerated
     */
    private Long dependencyTaskId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column workflow_task_dependency.create_time
     *
     * @mbggenerated
     */
    private Date createTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column workflow_task_dependency.update_time
     *
     * @mbggenerated
     */
    private Date updateTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column workflow_task_dependency.id
     *
     * @return the value of workflow_task_dependency.id
     *
     * @mbggenerated
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column workflow_task_dependency.id
     *
     * @param id the value for workflow_task_dependency.id
     *
     * @mbggenerated
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column workflow_task_dependency.workflow_detail_id
     *
     * @return the value of workflow_task_dependency.workflow_detail_id
     *
     * @mbggenerated
     */
    public Long getWorkflowDetailId() {
        return workflowDetailId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column workflow_task_dependency.workflow_detail_id
     *
     * @param workflowDetailId the value for workflow_task_dependency.workflow_detail_id
     *
     * @mbggenerated
     */
    public void setWorkflowDetailId(Long workflowDetailId) {
        this.workflowDetailId = workflowDetailId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column workflow_task_dependency.dependency_task_id
     *
     * @return the value of workflow_task_dependency.dependency_task_id
     *
     * @mbggenerated
     */
    public Long getDependencyTaskId() {
        return dependencyTaskId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column workflow_task_dependency.dependency_task_id
     *
     * @param dependencyTaskId the value for workflow_task_dependency.dependency_task_id
     *
     * @mbggenerated
     */
    public void setDependencyTaskId(Long dependencyTaskId) {
        this.dependencyTaskId = dependencyTaskId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column workflow_task_dependency.create_time
     *
     * @return the value of workflow_task_dependency.create_time
     *
     * @mbggenerated
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column workflow_task_dependency.create_time
     *
     * @param createTime the value for workflow_task_dependency.create_time
     *
     * @mbggenerated
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column workflow_task_dependency.update_time
     *
     * @return the value of workflow_task_dependency.update_time
     *
     * @mbggenerated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column workflow_task_dependency.update_time
     *
     * @param updateTime the value for workflow_task_dependency.update_time
     *
     * @mbggenerated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table workflow_task_dependency
     *
     * @mbggenerated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", workflowDetailId=").append(workflowDetailId);
        sb.append(", dependencyTaskId=").append(dependencyTaskId);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table workflow_task_dependency
     *
     * @mbggenerated
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        WorkflowTaskDependency other = (WorkflowTaskDependency) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getWorkflowDetailId() == null ? other.getWorkflowDetailId() == null : this.getWorkflowDetailId().equals(other.getWorkflowDetailId()))
            && (this.getDependencyTaskId() == null ? other.getDependencyTaskId() == null : this.getDependencyTaskId().equals(other.getDependencyTaskId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table workflow_task_dependency
     *
     * @mbggenerated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getWorkflowDetailId() == null) ? 0 : getWorkflowDetailId().hashCode());
        result = prime * result + ((getDependencyTaskId() == null) ? 0 : getDependencyTaskId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }
}
package com.yeahmobi.yscheduler.web.controller.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.Validate;

import com.yeahmobi.yscheduler.web.vo.WorkflowDetailVO;

/**
 * @author Ryan Sun
 */
public class WorkflowDependencyValidate {

    private List<WorkflowDetailVO> details;

    private Map<Long, List<Long>>  tasks = new HashMap<Long, List<Long>>();

    private Long                   endTask;

    private void buildTasks() {
        for (WorkflowDetailVO detail : this.details) {
            Long taskId = detail.getWorkflowDetail().getTaskId();
            String name = detail.getTaskName();
            if (taskId == null) {
                Validate.fail("添加任务不存在！");
            }
            if (this.tasks.containsKey(taskId)) {
                Validate.fail("重复添加任务" + name + "!");
            }

            List<Long> dependencyTaskIds = new ArrayList<Long>(detail.getDependencies());

            this.tasks.put(taskId, dependencyTaskIds);

            for (Long dependencyTaskId : dependencyTaskIds) {
                if (taskId.equals(dependencyTaskId)) {
                    Validate.fail("任务" + name + "依赖了它自己！");
                }
            }
        }
    }

    private void buildEndTasks() {
        List<Long> endTasks = new ArrayList<Long>();
        for (Long taskId : this.tasks.keySet()) {
            endTasks.add(taskId);
        }
        for (WorkflowDetailVO detail : this.details) {
            endTasks.removeAll(detail.getDependencies());
        }
        if (endTasks.size() > 1) {
            String names = "";
            for (Long id : endTasks) {
                for (WorkflowDetailVO vo : this.details) {
                    if (vo.getWorkflowDetail().getTaskId() == id) {
                        if (!StringUtils.isEmpty(names)) {
                            names += ", ";
                        }
                        names += vo.getTaskName();
                    }
                }
            }
            Validate.fail("工作流存在多个终止节点(不被任何任务依赖的任务): " + names);
        } else if (endTasks.size() == 0) {
            Validate.fail("工作流中存在循环依赖！");
        } else {
            this.endTask = endTasks.get(0);
        }
    }

    private void dependOnTaskNotInThisWorkflow() {
        for (WorkflowDetailVO detail : this.details) {
            List<Long> taskIds = detail.getDependencies();
            for (Long taskId : taskIds)
                if ((taskId != null) && !this.tasks.containsKey(taskId)) {
                    String name = detail.getTaskName();
                    Validate.fail("任务" + name + "依赖了没有配置在工作流中的任务！");
                }
        }
    }

    private boolean circularDependency() {
        Stack<Long> stack = new Stack<Long>();
        stack.push(this.endTask);
        boolean result = cicular(stack);
        if (!result) {
            result = this.tasks.size() != 0;
        }
        return result;
    }

    private boolean cicular(Stack<Long> stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Long item = stack.peek();
        List<Long> dependencyTasks = this.tasks.get(item);
        if ((dependencyTasks == null) || (dependencyTasks.size() == 0)) {
            this.tasks.remove(stack.pop());
            return cicular(stack);
        } else {
            Long dependencyTask = dependencyTasks.remove(0);
            if (stack.contains(dependencyTask)) {
                return true;
            }
            stack.push(dependencyTask);
            return cicular(stack);
        }
    }

    void validate() {
        buildTasks();
        buildEndTasks();
        dependOnTaskNotInThisWorkflow();
        Validate.isFalse(circularDependency(), "工作流中存在循环依赖！");
    }

    WorkflowDependencyValidate(List<WorkflowDetailVO> detailVos) {
        this.details = detailVos;
    }
}

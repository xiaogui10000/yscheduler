package com.yeahmobi.yscheduler.web.controller.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.yeahmobi.yscheduler.common.Paginator;
import com.yeahmobi.yscheduler.executor.TaskInstanceExecutor;
import com.yeahmobi.yscheduler.model.Attempt;
import com.yeahmobi.yscheduler.model.Task;
import com.yeahmobi.yscheduler.model.TaskInstance;
import com.yeahmobi.yscheduler.model.WorkflowInstance;
import com.yeahmobi.yscheduler.model.common.Query;
import com.yeahmobi.yscheduler.model.common.Query.TaskScheduleType;
import com.yeahmobi.yscheduler.model.service.AttemptService;
import com.yeahmobi.yscheduler.model.service.TaskInstanceService;
import com.yeahmobi.yscheduler.model.service.TaskService;
import com.yeahmobi.yscheduler.model.service.WorkflowInstanceService;
import com.yeahmobi.yscheduler.model.service.WorkflowService;
import com.yeahmobi.yscheduler.model.type.TaskInstanceStatus;
import com.yeahmobi.yscheduler.web.controller.AbstractController;
import com.yeahmobi.yscheduler.web.controller.workflow.WorkflowInstanceTasksController;
import com.yeahmobi.yscheduler.web.vo.TaskInstanceVO;

/**
 * @author wukezhu
 */
@Controller
@RequestMapping(value = { TaskInstanceController.SCREEN_NAME })
public class TaskInstanceController extends AbstractController {

    public static final String      SCREEN_NAME                = "task/instance";

    private static final Logger     LOGGER                     = LoggerFactory.getLogger(TaskInstanceController.class);

    @Autowired
    private TaskService             taskService;

    @Autowired
    private TaskInstanceService     taskInstanceService;

    @Autowired
    private AttemptService          attemptService;

    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    @Autowired
    private WorkflowService         workflowService;

    @Autowired
    private TaskInstanceExecutor    instanceExecutor;

    private static final String     WORKFLOW_INSTANCE_TASK_URL = WorkflowInstanceTasksController.SCREEN_NAME
                                                                 + "?instanceId=";

    @RequestMapping(value = { "" })
    public ModelAndView instanceList(Integer taskInstanceStatus, Integer taskScheduleType, Integer pageNum, long taskId) {
        Map<String, Object> map = new HashMap<String, Object>();

        Query query = buildQuery(map, taskInstanceStatus, taskScheduleType);

        Paginator paginator = new Paginator();
        pageNum = ((pageNum == null) || (pageNum < 0)) ? 0 : pageNum;

        List<TaskInstance> instances = this.taskInstanceService.list(query, taskId, pageNum, paginator);
        List<TaskInstanceVO> list = new ArrayList<TaskInstanceVO>(instances.size());

        if (instances != null) {
            for (TaskInstance instance : instances) {

                TaskInstanceVO vo = new TaskInstanceVO(instance);
                Attempt attempt = this.attemptService.getLastOne(instance.getId());

                if (attempt != null) {
                    vo.setAttempt(attempt);
                    int execTimes = this.attemptService.countActive(instance.getId());
                    vo.setExecTimes(execTimes);
                }

                Long workflowInstanceId = instance.getWorkflowInstanceId();
                if (workflowInstanceId != null) {
                    WorkflowInstance workflowInstance = this.workflowInstanceService.get(workflowInstanceId);
                    Long workflowId = workflowInstance.getWorkflowId();
                    String name = this.workflowService.get(workflowId).getName();
                    vo.setWorkflowName(name);
                }
                list.add(vo);
            }
        }

        // calculate the succcessCount
        int successCount = 0;
        int totalRunCount = 0;

        for (TaskInstance instance : this.taskInstanceService.listAll(taskId)) {
            if (instance.getStatus().isCompleted()) {
                totalRunCount++;
                if (instance.getStatus() == TaskInstanceStatus.SUCCESS) {
                    successCount++;
                }
            }
        }

        Task task = this.taskService.get(taskId);

        map.put("task", task);
        map.put("list", list);
        map.put("workflowUrl", WORKFLOW_INSTANCE_TASK_URL);
        map.put("paginator", paginator);
        map.put("successRate", (int) ((successCount * 100.00d) / totalRunCount));
        map.put("totalRunCount", totalRunCount);

        return screen(map, SCREEN_NAME);
    }

    private Query buildQuery(Map<String, Object> map, Integer taskInstanceStatus, Integer scheduleType) {
        Query query = new Query();

        if (taskInstanceStatus != null) {
            TaskInstanceStatus status = TaskInstanceStatus.valueOf(taskInstanceStatus);
            query.setTaskInstanceStatus(status);
        }

        if (scheduleType != null) {
            TaskScheduleType type = TaskScheduleType.valueOf(scheduleType);
            query.setTaskScheduleType(type);
        }

        map.put("query", query);
        map.put("allStatus", TaskInstanceStatus.values());
        map.put("scheduleTypes", TaskScheduleType.values());

        return query;
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object cancel(HttpSession session, long instanceId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            this.instanceExecutor.cancel(instanceId);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOGGER.error(e.getMessage(), e);
        }
        return JSON.toJSONString(map);

    }

    @RequestMapping(value = "/rerun", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object rerun(HttpSession session, long instanceId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            this.taskInstanceService.updateStatus(instanceId, TaskInstanceStatus.READY);
            TaskInstance instance = this.taskInstanceService.get(instanceId);
            this.instanceExecutor.submit(instance);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOGGER.error(e.getMessage(), e);
        }
        return JSON.toJSONString(map);

    }
}

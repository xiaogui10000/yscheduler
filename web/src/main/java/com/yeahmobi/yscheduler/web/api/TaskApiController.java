package com.yeahmobi.yscheduler.web.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeahmobi.yscheduler.common.CrontabUtils;
import com.yeahmobi.yscheduler.model.Agent;
import com.yeahmobi.yscheduler.model.Task;
import com.yeahmobi.yscheduler.model.User;
import com.yeahmobi.yscheduler.model.service.AgentService;
import com.yeahmobi.yscheduler.model.service.TaskService;
import com.yeahmobi.yscheduler.model.service.UserService;
import com.yeahmobi.yscheduler.model.type.DependingStatus;
import com.yeahmobi.yscheduler.model.type.TaskStatus;
import com.yeahmobi.yscheduler.model.type.TaskType;
import com.yeahmobi.yscheduler.web.common.HttpServletRequestHashMap;
import com.yeahmobi.yscheduler.web.common.HttpServletRequestMapWrapper;

/**
 * @author Leo Liang
 */
@Controller
@RequestMapping(value = { "api/task" })
public class TaskApiController extends ApiBaseController {

    @Autowired
    private TaskService  taskService;

    @Autowired
    private UserService  userService;

    @Autowired
    private AgentService agentService;

    @RequestMapping(value = { "name_exists" }, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String nameExists(@HttpServletRequestHashMap HttpServletRequestMapWrapper request, final String taskName)
                                                                                                                    throws ServletException,
                                                                                                                    IOException {
        return handleRequest(request, new ApiHandler() {

            public void handle(ApiRequest apiRequest, ApiResponse apiResponse) throws Exception {
                if (StringUtils.isBlank(taskName)) {
                    apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
                    apiResponse.setMessage("Task name must not be null or empty");
                } else {
                    apiResponse.addReturnValue("exists", TaskApiController.this.taskService.nameExist(taskName));
                    apiResponse.setStatus(ApiStatusCode.SUCCESS);
                }
            }
        });
    }

    @RequestMapping(value = { "disable_schedule" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String disableSchedule(@HttpServletRequestHashMap HttpServletRequestMapWrapper request,
                                  final String taskName, final String userName, final String userToken)
                                                                                                       throws ServletException,
                                                                                                       IOException {
        return handleRequest(request, new ApiHandler() {

            public void handle(ApiRequest apiRequest, ApiResponse apiResponse) throws Exception {
                updateTaskStatus(taskName, userName, userToken, TaskStatus.PAUSED, apiResponse);
            }

        });
    }

    @RequestMapping(value = { "enable_schedule" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String enableSchedule(@HttpServletRequestHashMap HttpServletRequestMapWrapper request,
                                 final String taskName, final String userName, final String userToken)
                                                                                                      throws ServletException,
                                                                                                      IOException {
        return handleRequest(request, new ApiHandler() {

            public void handle(ApiRequest apiRequest, ApiResponse apiResponse) throws Exception {
                updateTaskStatus(taskName, userName, userToken, TaskStatus.OPEN, apiResponse);
            }

        });
    }

    @RequestMapping(value = { "create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String create(final @HttpServletRequestHashMap HttpServletRequestMapWrapper request, final String taskName,
                         final String userName, final String userToken) throws ServletException, IOException {
        return handleRequest(request, new ApiHandler() {

            public void handle(ApiRequest apiRequest, ApiResponse apiResponse) throws Exception {
                Task task = extractTaskFromRequest(request, taskName, userName, userToken, apiResponse);
                if (apiResponse.isSuccess()) {
                    TaskApiController.this.taskService.add(task);
                    apiResponse.setStatus(ApiStatusCode.SUCCESS);
                    apiResponse.addReturnValue("id", task.getId());
                }
            }
        });
    }

    private Task extractTaskFromRequest(HttpServletRequestMapWrapper request, String taskName, String userName,
                                        String userToken, ApiResponse apiResponse) {
        User user = validateUserNameAndUserToken(userName, userToken, apiResponse);

        if (!apiResponse.isSuccess()) {
            return null;
        }

        Task task = new Task();

        String retry = request.get("retryTimes");
        setRetryTimes(task, retry);

        String timeoutStr = request.get("timeout");
        setTimeout(task, timeoutStr);

        String typeStr = request.get("type");
        setTaskType(apiResponse, task, typeStr);
        if (!apiResponse.isSuccess()) {
            return null;
        }

        task.setName(taskName);
        task.setOwner(user.getId());

        String crontab = request.get("crontab");
        // API新增的默认自动打开调度
        if (StringUtils.isNotBlank(crontab)) {
            try {
                crontab = CrontabUtils.normalize(crontab, false);
                task.setCrontab(crontab);
                task.setStatus(TaskStatus.OPEN);
            } catch (IllegalArgumentException e) {
                apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
                apiResponse.setMessage(e.getMessage());
                return null;
            }
        } else {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage("Task crontab must not be null or empty");
            return null;
        }

        String canSkip = request.get("canSkip");

        if (StringUtils.equals("false", canSkip)) {
            task.setCanSkip(false);
            String concurrent = request.get("concurrent");
            if (StringUtils.equals("false", concurrent)) {
                String condition = request.get("condition");
                if (StringUtils.equals("completed", condition)) {
                    task.setLastStatusDependency(DependingStatus.COMPLETED);
                } else if (StringUtils.equals("success", condition)) {
                    task.setLastStatusDependency(DependingStatus.SUCCESS);
                }

            } else {
                task.setLastStatusDependency(DependingStatus.NONE);
            }
        } else {
            task.setCanSkip(true);
            task.setLastStatusDependency(DependingStatus.NONE);

        }

        processTaskTypeCustomizedParams(request, apiResponse, task);

        if (!apiResponse.isSuccess()) {
            return null;
        }

        task.setDescription(request.get("description"));
        return task;
    }

    private void processTaskTypeCustomizedParams(HttpServletRequestMapWrapper request, ApiResponse apiResponse,
                                                 Task task) {
        switch (task.getType()) {
            case HTTP: {
                // agent
                List<Agent> inPlatform = this.agentService.listInPlatform();
                if ((inPlatform == null) || inPlatform.isEmpty()) {
                    apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
                    apiResponse.setMessage("There is no platform agent for create an HTTP type task");
                    return;
                }
                task.setAgentId(inPlatform.get(0).getId());
                // command
                String calloutUrl = request.get("calloutUrl");
                if (StringUtils.isBlank(calloutUrl)) {
                    apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
                    apiResponse.setMessage("CalloutUrl must not be null or empty");
                    return;
                }
                String cancelUrl = StringUtils.trimToNull(request.get("cancelUrl"));
                if (cancelUrl == null) {
                    cancelUrl = "";
                }
                String needCallbackStr = request.get("needCallback");
                if (StringUtils.isBlank(needCallbackStr)
                    || (!StringUtils.equalsIgnoreCase(needCallbackStr, "true") && !StringUtils.equalsIgnoreCase(needCallbackStr,
                                                                                                                "false"))) {
                    apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
                    apiResponse.setMessage("needCallback must not be null or empty and must be true or false");
                    return;
                }
                boolean needCallback = BooleanUtils.toBoolean(needCallbackStr);
                task.setCommand(calloutUrl + ';' + needCallback + ';' + cancelUrl);
            }
                break;
            case SHELL: { // agent
                String agentName = request.get("agent");
                if (StringUtils.isNotBlank(agentName)) {
                    Agent agent = this.agentService.get(agentName);
                    if (agent == null) {
                        apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
                        apiResponse.setMessage(String.format("Agent(%s) not found", agentName));
                        return;
                    }
                    task.setAgentId(agent.getId());
                } else {
                    apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
                    apiResponse.setMessage("Agent id must not be null or empty and must be numeric");
                    return;
                }
                // command
                String command = request.get("command");
                if (StringUtils.isBlank(command)) {
                    apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
                    apiResponse.setMessage("Command must not be null or empty");
                    return;
                }
                task.setCommand(command);
            }
                break;
        }
    }

    private void setTaskType(ApiResponse apiResponse, Task task, String typeStr) {
        if (StringUtils.isBlank(typeStr) || !StringUtils.isNumeric(typeStr)) {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage("Task type must not be null or empty and must be numeric");
            return;
        }
        int type = Integer.parseInt(typeStr);
        TaskType taskType = TaskType.valueOf(type);
        if (taskType == null) {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage("Task type error");
            return;
        } else {
            task.setType(taskType);
        }
    }

    private void setTimeout(Task task, String timeoutStr) {
        if (StringUtils.isNotBlank(timeoutStr) && StringUtils.isNumeric(timeoutStr)) {
            int timeout = Integer.parseInt(timeoutStr);
            task.setTimeout(timeout);
        } else {
            task.setTimeout(0);
        }
    }

    private void setRetryTimes(Task task, String retry) {
        if (StringUtils.isNotBlank(retry) && StringUtils.isNumeric(retry)) {
            int retryTimes = Integer.parseInt(retry);
            task.setRetryTimes(retryTimes);
        } else {
            task.setRetryTimes(0);
        }
    }

    private void updateTaskStatus(final String taskName, final String userName, final String userToken,
                                  TaskStatus status, ApiResponse apiResponse) {

        User user = validateUserNameAndUserToken(userName, userToken, apiResponse);
        if (!apiResponse.isSuccess()) {
            return;
        }

        if (StringUtil.isBlank(taskName)) {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage("Task name must not be null or empty");
            return;
        }

        if (!TaskApiController.this.taskService.nameExist(taskName)) {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage(String.format("Task(%s) does not exist", taskName));
            return;
        }

        Task task = TaskApiController.this.taskService.get(taskName);

        if ((task != null) && TaskApiController.this.taskService.canModify(task.getId(), user.getId())) {
            if (!status.equals(task.getStatus())) {
                task.setStatus(status);
                TaskApiController.this.taskService.update(task);
            }
            apiResponse.setStatus(ApiStatusCode.SUCCESS);
        } else {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage(String.format("User(%s) can not modify task(%s)", userName, taskName));
        }

    }

    private User validateUserNameAndUserToken(String userName, String userToken, ApiResponse apiResponse) {
        if (StringUtil.isBlank(userName)) {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage("User name must not be null or empty");
            return null;
        }
        User user = null;

        try {
            user = TaskApiController.this.userService.get(userName);
        } catch (Exception e) {
            // ignore
        }

        if (user == null) {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage(String.format("User(%s) does not exist", userName));
            return null;
        }

        if (StringUtil.isBlank(userToken)) {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage("User token must not be null or empty");
            return null;
        }

        if (!userToken.equals(user.getToken())) {
            apiResponse.setStatus(ApiStatusCode.BIZ_ERROR);
            apiResponse.setMessage(String.format("User token(%s) error", userToken));
            return null;
        }
        return user;
    }
}

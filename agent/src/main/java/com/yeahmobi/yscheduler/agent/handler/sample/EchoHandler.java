package com.yeahmobi.yscheduler.agent.handler.sample;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeahmobi.yscheduler.agentframework.agent.event.EventHandler;
import com.yeahmobi.yscheduler.agentframework.agent.event.HandlerResult;

/**
 * @author Leo.Liang
 */
public class EchoHandler implements EventHandler {

    public static final String  EVENT_TYPE = "echo";

    private static final Logger log        = LoggerFactory.getLogger(EchoHandler.class);

    public void onEvent(Map<String, String> params, HandlerResult handlerResult) {
        log.info("Hello {}", params.get("name"));
        handlerResult.setResult(params.get("name"));
    }

}

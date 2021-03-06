package com.yeahmobi.yscheduler.model.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeahmobi.yscheduler.model.TaskAuthority;
import com.yeahmobi.yscheduler.model.TaskAuthorityExample;
import com.yeahmobi.yscheduler.model.dao.TaskAuthorityDao;
import com.yeahmobi.yscheduler.model.service.TaskAuthorityService;
import com.yeahmobi.yscheduler.model.type.AuthorityMode;

/**
 * @author Ryan Sun
 */
@Service
public class TaskAuthorityServiceImpl implements TaskAuthorityService {

    @Autowired
    private TaskAuthorityDao authorityDao;

    public List<Long> listReadonlyUser(long taskId) {
        List<Long> result = new ArrayList<Long>();
        TaskAuthorityExample example = new TaskAuthorityExample();
        example.or().andTaskIdEqualTo(taskId);
        List<TaskAuthority> authorities = this.authorityDao.selectByExample(example);
        for (TaskAuthority authority : authorities) {
            if (authority.getMode() == AuthorityMode.READONLY) {
                result.add(authority.getUserId());
            }
        }
        return result;
    }

    public List<Long> listWritableUser(long taskId) {
        List<Long> result = new ArrayList<Long>();
        TaskAuthorityExample example = new TaskAuthorityExample();
        example.or().andTaskIdEqualTo(taskId);
        List<TaskAuthority> authorities = this.authorityDao.selectByExample(example);
        for (TaskAuthority authority : authorities) {
            if (authority.getMode() == AuthorityMode.WRITABLE) {
                result.add(authority.getUserId());
            }
        }
        return result;
    }

    public List<Long> listFollowUser(long taskId) {
        List<Long> result = new ArrayList<Long>();
        TaskAuthorityExample example = new TaskAuthorityExample();
        example.or().andTaskIdEqualTo(taskId);
        List<TaskAuthority> authorities = this.authorityDao.selectByExample(example);
        for (TaskAuthority authority : authorities) {
            if (authority.getFollow()) {
                result.add(authority.getUserId());
            }
        }
        return result;
    }

    @Transactional
    public void add(List<Long> readableUsers, List<Long> writeableUsers, List<Long> followingUsers, long taskId) {
        Map<Long, TaskAuthority> authorities = new HashMap<Long, TaskAuthority>();
        for (long userId : readableUsers) {
            TaskAuthority authority = createOrFindAuthority(authorities, userId, taskId);
            authority.setMode(AuthorityMode.READONLY);
        }
        for (long userId : writeableUsers) {
            TaskAuthority authority = createOrFindAuthority(authorities, userId, taskId);
            authority.setMode(AuthorityMode.WRITABLE);
        }
        for (long userId : followingUsers) {
            TaskAuthority authority = createOrFindAuthority(authorities, userId, taskId);
            authority.setFollow(true);
        }
        for (TaskAuthority authority : authorities.values()) {
            Date time = new Date();
            authority.setCreateTime(time);
            authority.setUpdateTime(time);
            this.authorityDao.insert(authority);
        }
    }

    @Transactional
    public void update(List<Long> readableUsers, List<Long> writeableUsers, List<Long> followingUsers, long taskId) {
        TaskAuthorityExample example = new TaskAuthorityExample();
        example.or().andTaskIdEqualTo(taskId);
        this.authorityDao.deleteByExample(example);
        add(readableUsers, writeableUsers, followingUsers, taskId);
    }

    private TaskAuthority createOrFindAuthority(Map<Long, TaskAuthority> authorities, long userId, long taskId) {
        TaskAuthority authority = authorities.get(userId);
        if (authority == null) {
            authority = new TaskAuthority();
            authority.setUserId(userId);
            authority.setTaskId(taskId);
            authority.setMode(AuthorityMode.NONE);
            authority.setFollow(false);
            authorities.put(userId, authority);
        }
        return authority;
    }

    public void deleteByUser(long userId) {
        TaskAuthorityExample example = new TaskAuthorityExample();
        example.createCriteria().andUserIdEqualTo(userId);
        this.authorityDao.deleteByExample(example);
    }

    @SuppressWarnings("unchecked")
    public List<Long> listReadonlyTaskIds(long userId) {
        TaskAuthorityExample example = new TaskAuthorityExample();
        example.createCriteria().andUserIdEqualTo(userId).andModeEqualTo(AuthorityMode.READONLY);
        return new ArrayList<Long>(CollectionUtils.collect(this.authorityDao.selectByExample(example),
                                                           new Transformer() {

                                                               public Object transform(Object input) {
                                                                   return ((TaskAuthority) input).getTaskId();
                                                               }
                                                           }));
    }

    @SuppressWarnings("unchecked")
    public List<Long> listWritableTaskIds(long userId) {
        TaskAuthorityExample example = new TaskAuthorityExample();
        example.createCriteria().andUserIdEqualTo(userId).andModeEqualTo(AuthorityMode.WRITABLE);
        return new ArrayList<Long>(CollectionUtils.collect(this.authorityDao.selectByExample(example),
                                                           new Transformer() {

                                                               public Object transform(Object input) {
                                                                   return ((TaskAuthority) input).getTaskId();
                                                               }
                                                           }));
    }

}

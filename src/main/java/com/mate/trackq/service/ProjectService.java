package com.mate.trackq.service;

import com.mate.trackq.dao.ProjectDao;
import com.mate.trackq.model.Project;
import com.mate.trackq.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProjectService {

    void create(Project project);

    Project getById(Long projectId);

    Project getByName(String projectName);

    List<Project> getUserProjects(Long userId);

    List<User> getProjectUserList();
}

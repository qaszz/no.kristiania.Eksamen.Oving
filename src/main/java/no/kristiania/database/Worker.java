package no.kristiania.database;

import java.net.URLDecoder;

public class Worker {
    private String name;
    private String email;
    private Long id;
    private Integer projectId;

    public String getName() {
        return URLDecoder.decode(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return URLDecoder.decode(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getProjectId() {
        long projectId = Project.getId();
        return projectId;
    }

    public void setProjectId(long project_id) {
        this.projectId = projectId;
    }
}

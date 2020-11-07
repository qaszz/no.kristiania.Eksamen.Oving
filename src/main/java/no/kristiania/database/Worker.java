package no.kristiania.database;

import java.net.URLDecoder;

public class Worker {
    private String name;
    private String email;
    private Integer id;
    private Integer projectId;

    public String getName() {
        return URLDecoder.decode(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return URLDecoder.decode(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
}

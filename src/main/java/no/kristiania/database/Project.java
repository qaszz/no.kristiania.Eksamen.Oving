package no.kristiania.database;

import java.net.URLDecoder;

public class Project {
    private String projectName;
    private long id;

    public String getName() {
        return URLDecoder.decode(projectName);
    }

    public void setName(String name) {
        this.projectName = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}

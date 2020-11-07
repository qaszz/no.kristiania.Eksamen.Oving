package no.kristiania.database;

import java.net.URLDecoder;

public class Project {
    private static Integer id;
    private String projectName;

    public String getName() {
        return URLDecoder.decode(projectName);
    }

    public void setName(String name) {
        this.projectName = name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static Integer getId() {
        return id;
    }
}

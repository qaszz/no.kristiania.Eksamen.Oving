package no.kristiania.database;

import java.net.URLDecoder;

public class Project {
    private static long id;
    private String projectName;

    public String getName() {
        return URLDecoder.decode(projectName);
    }

    public void setName(String name) {
        this.projectName = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public static long getId() {
        return id;
    }
}

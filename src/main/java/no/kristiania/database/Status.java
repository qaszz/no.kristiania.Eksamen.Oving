package no.kristiania.database;

import java.net.URLDecoder;

public class Status {

    private long id;
    private String statusName;

    public String getStatusName() {
        return URLDecoder.decode(statusName);
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

package no.kristiania.database;

import java.net.URLDecoder;

public class Worker {
    private String name;
    private String email;
    private Long id;

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
}

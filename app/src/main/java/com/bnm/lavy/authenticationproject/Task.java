package com.bnm.lavy.authenticationproject;



public class Task {
    private String content; // task text
    private String deadLine; // Example 25.12.2019, if no dead line - empty string
    private boolean hiPriority; // if true - hi priority
    private String id; // unique id

    public Task() {}
    public Task(String id, String content, String deadLine, boolean hiPriority) {
        this.content = content;
        this.deadLine = deadLine;
        this.hiPriority = hiPriority;
        this.id = id;
    }

    public Task(String content, String deadLine, boolean hiPriority) {
        this.content = content;
        this.deadLine = deadLine;
        this.hiPriority = hiPriority;
        this.id = "" +System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(String deadLine) {
        this.deadLine = deadLine;
    }

    public boolean isHiPriority() {
        return hiPriority;
    }

    public void setHiPriority(boolean hiPriority) {
        this.hiPriority = hiPriority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

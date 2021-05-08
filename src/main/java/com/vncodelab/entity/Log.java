package com.vncodelab.entity;

import com.google.cloud.Timestamp;

public class Log {
    private int step;
    private int duration;
    private int enter;
    private Timestamp lastEnter;

    public Timestamp getLastEnter() {
        return lastEnter;
    }

    public void setLastEnter(Timestamp lastEnter) {
        this.lastEnter = lastEnter;
    }

    public int getEnter() {
        return enter;
    }

    public void setEnter(int enter) {
        this.enter = enter;
    }

    public int getLeave() {
        return leave;
    }

    public void setLeave(int leave) {
        this.leave = leave;
    }

    private int leave;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type;
}

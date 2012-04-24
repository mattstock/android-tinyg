package org.csgeeks.TinyG.system;

import java.util.List;

public class Axis {

    private float machine_position;
    private float work_position;
    private boolean axis_mode;
    private float feed_rate_maximum;
    private float velocity_maximum;
    private float travel_maximum;
    private float jerk_maximum;
    private float junction_devation;
    private int switch_mode;
    private float homing_travel;
    private float homing_search_velocity;
    private float homing_latch_velocity;
    private float homing_zero_offset;
    private float homing_work_offset;
    private String axis_name;
    private List<Motor> motors;

    public enum AXIS {

        X, Y, Z, A;
    }

    public Axis() {
        //
    }

    public Axis(String name) {
        this.setAxis_name(name);
    }

    public Axis(AXIS ax) {
        if (ax == AXIS.A) {
            this.setAxis_name("A");
        } else if (ax == AXIS.Z) {
            this.setAxis_name("Z");
        } else if (ax == AXIS.Y) {
            this.setAxis_name("Y");
        } else {
            this.setAxis_name("X");
        }
    }
    
 
    public boolean isAxis_mode() {
        return axis_mode;
    }

    public void setAxis_mode(boolean axis_mode) {
        this.axis_mode = axis_mode;
    }

    public String getAxis_name() {
        return axis_name;
    }

    public void setAxis_name(String axis_name) {
        this.axis_name = axis_name;
    }

    public float getFeed_rate_maximum() {
        return feed_rate_maximum;
    }

    public void setFeed_rate_maximum(float feed_rate_maximum) {
        this.feed_rate_maximum = feed_rate_maximum;
    }

    public float getHoming_latch_velocity() {
        return homing_latch_velocity;
    }

    public void setHoming_latch_velocity(float homing_latch_velocity) {
        this.homing_latch_velocity = homing_latch_velocity;
    }

    public float getHoming_search_velocity() {
        return homing_search_velocity;
    }

    public void setHoming_search_velocity(float homing_search_velocity) {
        this.homing_search_velocity = homing_search_velocity;
    }

    public float getHoming_travel() {
        return homing_travel;
    }

    public void setHoming_travel(float homing_travel) {
        this.homing_travel = homing_travel;
    }

    public float getHoming_work_offset() {
        return homing_work_offset;
    }

    public void setHoming_work_offset(float homing_work_offset) {
        this.homing_work_offset = homing_work_offset;
    }

    public float getHoming_zero_offset() {
        return homing_zero_offset;
    }

    public void setHoming_zero_offset(float homing_zero_offset) {
        this.homing_zero_offset = homing_zero_offset;
    }

    public float getJerk_maximum() {
        return jerk_maximum;
    }

    public void setJerk_maximum(float jerk_maximum) {
        this.jerk_maximum = jerk_maximum;
    }

    public float getJunction_devation() {
        return junction_devation;
    }

    public void setJunction_devation(float junction_devation) {
        this.junction_devation = junction_devation;
    }

    public float getMachine_position() {
        return machine_position;
    }

    public void setMachine_position(float machine_position) {
        this.machine_position = machine_position;
    }

    public List<Motor> getMotors() {
        return motors;
    }

    public boolean addMotor(Motor motor) {
        if (!motors.contains(motor)) {
            motors.add(motor);
            return true;
        }
        return false;
    }

    public void setMotors(List<Motor> motors) {
        this.motors = motors;
    }

    public int getSwitch_mode() {
        return switch_mode;
    }

    public void setSwitch_mode(int switch_mode) {
        this.switch_mode = switch_mode;
    }

    public float getTravel_maximum() {
        return travel_maximum;
    }

    public void setTravel_maximum(float travel_maximum) {
        this.travel_maximum = travel_maximum;
    }

    public float getVelocity_maximum() {
        return velocity_maximum;
    }

    public void setVelocity_maximum(float velocity_maximum) {
        this.velocity_maximum = velocity_maximum;
    }

    public float getWork_position() {
        return work_position;
    }

    public void setWork_position(float work_position) {
        this.work_position = work_position;
    }
}

package org.example;

public class Trafic {
    private int sensor_id;
    private int count1;
    private int count2;
    private int timegreen1;
    private int timegreen2;
    private int new_green1;
    private int new_green2;


    public Trafic() {}

    public Trafic(int sensor_id, int count1, int count2, int timegreen1, int timegreen2) {
        this.sensor_id = sensor_id;
        this.count1 = count1;
        this.count2 = count2;
        this.timegreen1 = timegreen1;
        this.timegreen2 = timegreen2;
        this.new_green1 = timegreen1;
        this.new_green2= timegreen2;
    }

    public int getsensor_id() { return sensor_id; }
    public void setsensor_id(int sensor_id) { this.sensor_id = sensor_id; }
    public int getcount1() { return count1; }
    public void setcount1(int count1) { this.count1 = count1; }
    public int getcount2() { return count2; }
    public void setcount2(int count2) { this.count2 = count2; }
    public int gettimegreen1() { return timegreen1; }
    public void settimegreen1 (int timegreen1) { this.timegreen1 = timegreen1; }
    public int gettimegreen2() { return timegreen2; }
    public void settimegreen2 (int timegreen2) { this.timegreen2 = timegreen2; }
    public int getnew_green1() { return new_green1; }
    public void setnew_green1 (int new_green1) { this.new_green1 = new_green1; }
    public int getnew_green2() { return new_green2; }
    public void setnew_green2 (int new_green2) { this.new_green2 = new_green2; }
}

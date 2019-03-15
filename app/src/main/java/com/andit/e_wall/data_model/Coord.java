package com.andit.e_wall.data_model;

public class Coord {
    double x;
    double y;


    public Coord(double x, double y){
        setX(x);
        setY(y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }


}

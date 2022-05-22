package com.alibaba.fastjson2.support.geo;

import com.alibaba.fastjson2.annotation.JSONType;

/**
 * @since 1.2.68
 */
@JSONType(typeName = "Polygon", orders = {"type", "bbox", "coordinates"})
public class Polygon
        extends Geometry {
    private double[][][] coordinates;

    public Polygon() {
        super("Polygon");
    }

    public double[][][] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[][][] coordinates) {
        this.coordinates = coordinates;
    }
}

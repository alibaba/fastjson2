package com.alibaba.fastjson2.support.geo;

import com.alibaba.fastjson2.annotation.JSONType;

/**
 * @since 1.2.68
 */
@JSONType(typeName = "MultiLineString", orders = {"type", "bbox", "coordinates"})
public class MultiLineString
        extends Geometry {
    private double[][][] coordinates;

    public MultiLineString() {
        super("MultiLineString");
    }

    public double[][][] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[][][] coordinates) {
        this.coordinates = coordinates;
    }
}

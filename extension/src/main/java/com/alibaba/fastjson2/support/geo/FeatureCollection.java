package com.alibaba.fastjson2.support.geo;

import com.alibaba.fastjson2.annotation.JSONType;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.2.68
 */
@JSONType(typeName = "FeatureCollection", orders = {"type", "bbox", "coordinates"})
public class FeatureCollection
        extends Geometry {
    private List<Feature> features = new ArrayList<Feature>();
    public FeatureCollection() {
        super("FeatureCollection");
    }
    public List<Feature> getFeatures() {
        return features;
    }
}

package com.alibaba.fastjson2.support.geo;

import com.alibaba.fastjson2.annotation.JSONType;

/**
 * Base class for GeoJSON geometry objects.
 * Represents geometric shapes following the GeoJSON specification (RFC 7946).
 *
 * <p>Supported geometry types:</p>
 * <ul>
 * <li>Point, MultiPoint</li>
 * <li>LineString, MultiLineString</li>
 * <li>Polygon, MultiPolygon</li>
 * <li>GeometryCollection</li>
 * <li>Feature, FeatureCollection</li>
 * </ul>
 *
 * @since 1.2.68
 */
@JSONType(
        seeAlso = {
            GeometryCollection.class,
            LineString.class,
            MultiLineString.class,
            Point.class,
            MultiPoint.class,
            Polygon.class,
            MultiPolygon.class,
            Feature.class,
            FeatureCollection.class},
        typeKey = "type"
)
public abstract class Geometry {
    private final String type;
    private double[] bbox;

    /**
     * Creates a new geometry with the specified type.
     *
     * @param type the geometry type (e.g., "Point", "LineString", "Polygon")
     */
    protected Geometry(String type) {
        this.type = type;
    }

    /**
     * Gets the geometry type.
     *
     * @return the geometry type string
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the bounding box of this geometry.
     * The bounding box format is [west, south, east, north] for 2D geometries.
     *
     * @return the bounding box coordinates, or null if not set
     */
    public double[] getBbox() {
        return bbox;
    }

    /**
     * Sets the bounding box of this geometry.
     *
     * @param bbox the bounding box coordinates
     */
    public void setBbox(double[] bbox) {
        this.bbox = bbox;
    }
}

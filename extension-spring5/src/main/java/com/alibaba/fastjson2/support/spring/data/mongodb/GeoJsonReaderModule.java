package com.alibaba.fastjson2.support.spring.data.mongodb;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonMultiPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.util.List;

public class GeoJsonReaderModule
        implements ObjectReaderModule {
    public static final GeoJsonReaderModule INSTANCE = new GeoJsonReaderModule();

    @Override
    public void init(ObjectReaderProvider provider) {
        provider.mixIn(Point.class, PointMixin.class);
        provider.mixIn(GeoJsonLineString.class, GeoJsonLineStringMixin.class);
        provider.mixIn(GeoJsonMultiPoint.class, GeoJsonMultiPointMixin.class);
        provider.register(GeoJsonPoint.class, GeoJsonPointReader.INSTANCE);
        provider.register(GeoJsonPolygon.class, GeoJsonPolygonReader.INSTANCE);
    }

    @JSONType(deserializeFeatures = JSONReader.Feature.SupportArrayToBean)
    static class PointMixin {
        @JSONCreator(parameterNames = {"x", "y"})
        public PointMixin(double x, double y) {
        }
    }

    @JSONType(
            typeKey = "type",
            typeName = "MultiPoint",
            deserializeFeatures = JSONReader.Feature.SupportArrayToBean)
    abstract static class GeoJsonMultiPointMixin {
        @JSONCreator(parameterNames = "coordinates")
        public GeoJsonMultiPointMixin(List<Point> points) {
        }

        @JSONField(deserialize = false)
        public abstract List<Point> getCoordinates();
    }

    @JSONType(
            typeKey = "type",
            typeName = "LineString",
            deserializeFeatures = JSONReader.Feature.SupportArrayToBean
    )
    abstract static class GeoJsonLineStringMixin {
        @JSONCreator(parameterNames = "coordinates")
        public GeoJsonLineStringMixin(List<Point> points) {
        }

        @JSONField(deserialize = false)
        public abstract List<Point> getCoordinates();
    }
}

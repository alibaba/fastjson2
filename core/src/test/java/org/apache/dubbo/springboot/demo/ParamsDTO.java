package org.apache.dubbo.springboot.demo;

import lombok.Data;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Data
public class ParamsDTO
        implements Serializable {
    private List<ParamsItemDTO> paramsItems;

    private EnumSet<TimeUnit> paramsItemSet;
}

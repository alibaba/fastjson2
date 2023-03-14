package org.apache.dubbo.springboot.demo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ParamsDTO
        implements Serializable {
    private List<ParamsItemDTO> paramsItems;
}

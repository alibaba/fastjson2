package com.alibaba.json.bvtVO;

import java.util.ArrayList;
import java.util.List;

public class OfferRankResultVO {
    private List<SearchCenterOfferModel> models = new ArrayList<SearchCenterOfferModel>();

    public OfferRankResultVO() {
        models.add(new SearchCenterOfferModel());
    }

    public List<SearchCenterOfferModel> getModel() {
        return models;
    }

    public void setModel(List<SearchCenterOfferModel> models) {
        this.models = models;
    }
}

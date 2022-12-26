package com.alibaba.fastjson2.diff.path;

import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;

import java.util.ArrayList;
import java.util.List;

public class JsonCompareResult {

    private Boolean match = true;

    private List<Defects> defectsList;


    public boolean isMatch(){
        if(match == null){
            return  false;
        }
        return match;
    }

    /**
     * Add comparison information
     * @param defects
     */
    public void addDefects(Defects defects) {
        if(defectsList == null) {
            defectsList = new ArrayList<>();
        }

        // Add Variance
        if (RunTimeDataFactory.getTempDataInstance().isAddDiff()) {
            if (match) {
                match = false;
            }
            defectsList.add(defects);
        }else {
            RunTimeDataFactory.getTempDataInstance().addDefects(defects);
        }
    }

    public Boolean getMatch() {
        return match;
    }

    public void setMatch(Boolean match) {
        this.match = match;
    }

    public List<Defects> getDefectsList() {
        return defectsList;
    }

    public void setDefectsList(List<Defects> defectsList) {
        this.defectsList = defectsList;
    }
}

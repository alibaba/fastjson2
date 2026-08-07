package com.alibaba.json.bvtVO;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

/**
 *
 * @author wb_jianhui.shijh
 */
public class IEvent
        implements Serializable {
    private static final long serialVersionUID = -791431935700654454L;

    /**
     * �¼������
     */
    private String name;

    /**
     * �¼�����Դ
     */
    private String source;

    /**
     * �¼����
     */
    private Map<String, Object> detailData;

    /**
     * �¼�����ʱ��
     */
    private Timestamp generateTime;

    /**
     * ���¼����������һ��Ψһ��־��ID.
     */
    private String externalId;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, Object> getDetailData() {
        return detailData;
    }

    public void setDetailData(Map<String, Object> detailData) {
        this.detailData = detailData;
    }

    public Timestamp getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(Timestamp generateTime) {
        this.generateTime = generateTime;
    }

    @Override
    public String toString() {
        return "IEvent [name=" + name + ", source=" + source + ", externalId=" + externalId
                + ", generateTime=" + generateTime + ", detailData=" + detailData + "]";
    }
}

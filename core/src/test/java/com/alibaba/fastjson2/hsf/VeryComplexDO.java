package com.alibaba.fastjson2.hsf;

import java.io.Serializable;
import java.util.*;

/**
 * 复杂对象
 *
 * @author yp148590
 */
public class VeryComplexDO
        implements Serializable {
    private static final long serialVersionUID = -6315109222257881914L;
    private int pint;
    private long plong;
    private float pfloat;
    private short fshort;
    private byte pbyte;
    private double pdouble;
    private long[] plongArray;
    private List<String> plist;
    private Map<String, String> pmap;
    private TreeSet<String> ptreeset;
    private BaseDO pBaseDO;

    public VeryComplexDO() {
    }

    public int getPint() {
        return pint;
    }

    public void setPint(int pint) {
        this.pint = pint;
    }

    public long getPlong() {
        return plong;
    }

    public void setPlong(long plong) {
        this.plong = plong;
    }

    public float getPfloat() {
        return pfloat;
    }

    public void setPfloat(float pfloat) {
        this.pfloat = pfloat;
    }

    public short getFshort() {
        return fshort;
    }

    public void setFshort(short fshort) {
        this.fshort = fshort;
    }

    public byte getPbyte() {
        return pbyte;
    }

    public void setPbyte(byte pbyte) {
        this.pbyte = pbyte;
    }

    public double getPdouble() {
        return pdouble;
    }

    public void setPdouble(double pdouble) {
        this.pdouble = pdouble;
    }

    public long[] getPlongArray() {
        return plongArray;
    }

    public void setPlongArray(long[] plongArray) {
        this.plongArray = plongArray;
    }

    public List<String> getPlist() {
        return plist;
    }

    public void setPlist(List<String> plist) {
        this.plist = plist;
    }

    public Map<String, String> getPmap() {
        return pmap;
    }

    public void setPmap(Map<String, String> pmap) {
        this.pmap = pmap;
    }

    public TreeSet<String> getPtreeset() {
        return ptreeset;
    }

    public void setPtreeset(TreeSet<String> ptreeset) {
        this.ptreeset = ptreeset;
    }

    public BaseDO getpBaseDO() {
        return pBaseDO;
    }

    public void setpBaseDO(BaseDO pBaseDO) {
        this.pBaseDO = pBaseDO;
    }

    public static VeryComplexDO getFixedComplexDO() {
        VeryComplexDO vdo = new VeryComplexDO();
        vdo.setFshort((short) 2);
        vdo.setPbyte((byte) 3);
        vdo.setPfloat(1.2f);
        vdo.setPint(69);
        vdo.setPdouble(9999.9999);
        List<String> tmp = new ArrayList<String>();
        tmp.add("taobao");
        tmp.add("java");
        tmp.add("linux");
        vdo.setPlist(tmp);
        vdo.setPlong(56);
        vdo.setPlongArray(new long[]{1, 2, 3, 4, 5, 6});
        BaseDO bdo = new BaseDO();
        bdo.setId(45);
        vdo.setpBaseDO(bdo);
        Map<String, String> map = new HashMap<String, String>();
        map.put("me", "you");
        map.put("love", "taobao");
        map.put("test", "HSF");
//        vdo.setPmap(map);
        TreeSet<String> ts = new TreeSet<String>();
        ts.add("bbb");
        ts.add("aaa");
        ts.add("bbb");
//        vdo.setPtreeset(ts);
        return vdo;
    }

    public static void verify(VeryComplexDO actual) throws Exception {
        VeryComplexDO expected = VeryComplexDO.getFixedComplexDO();
        if (expected.getPlong() != actual.getPlong()) {
            throw new Exception("VeryComplexDO verify error");
        }
    }
}

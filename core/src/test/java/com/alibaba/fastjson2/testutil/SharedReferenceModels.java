package com.alibaba.fastjson2.testutil;

import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Shared data models and builders for
 * {@code SharedReferenceInCollectionTest} (JSON text and JSONB).
 */
public final class SharedReferenceModels {
    private SharedReferenceModels() {
    }

    public static <S extends Set<Map<String, Object>>> S buildMapRows(S outer) {
        Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (String sn : new String[]{"sn-1", "sn-2", "sn-3"}) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sn", sn);
            row.put("codes", sharedInner);
            outer.add(row);
        }
        return outer;
    }

    public static <C extends Collection<Map<String, Object>>> C buildAliasedMapRows(C outer, int size) {
        for (int i = 1; i <= size; i++) {
            Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sn", "sn-" + i);
            row.put("codes", sharedInner);
            row.put("codesAlias", sharedInner);
            outer.add(row);
        }
        return outer;
    }

    public static <S extends Set<Bean>> S buildBeanRows(S outer) {
        Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (String sn : new String[]{"sn-1", "sn-2", "sn-3"}) {
            outer.add(new Bean(sn, sharedInner));
        }
        return outer;
    }

    public static <S extends Set<AliasedBean>> S buildAliasedBeanRows(S outer) {
        for (String sn : new String[]{"sn-1", "sn-2", "sn-3"}) {
            Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
            outer.add(new AliasedBean(sn, sharedInner, sharedInner));
        }
        return outer;
    }

    public static class Bean implements Serializable, Comparable<Bean> {
        private static final long serialVersionUID = 1L;

        public String sn;
        public Set<String> codes;

        public Bean() {
        }

        public Bean(String sn, Set<String> codes) {
            this.sn = sn;
            this.codes = codes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Bean)) {
                return false;
            }
            Bean bean = (Bean) o;
            return Objects.equals(sn, bean.sn)
                    && Objects.equals(codes, bean.codes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sn, codes);
        }

        @Override
        public int compareTo(Bean o) {
            return sn.compareTo(o.sn);
        }
    }

    public static class AliasedBean implements Serializable, Comparable<AliasedBean> {
        private static final long serialVersionUID = 1L;

        public String sn;
        public Set<String> codes;
        public Set<String> codesAlias;

        public AliasedBean() {
        }

        public AliasedBean(String sn, Set<String> codes, Set<String> codesAlias) {
            this.sn = sn;
            this.codes = codes;
            this.codesAlias = codesAlias;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AliasedBean)) {
                return false;
            }
            AliasedBean bean = (AliasedBean) o;
            return Objects.equals(sn, bean.sn)
                    && Objects.equals(codes, bean.codes)
                    && Objects.equals(codesAlias, bean.codesAlias);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sn, codes, codesAlias);
        }

        @Override
        public int compareTo(AliasedBean o) {
            return sn.compareTo(o.sn);
        }
    }

    // Uses identity equality so cycles do not affect Set hashing
    public static class CyclicBean implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public CyclicBean self;
        public CyclicBean child;
    }

    // Uses identity equality while referencing the enclosing Set
    public static class SetBackRefBean implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public Set<SetBackRefBean> parentSet;
    }

    // Outer/inner Set elements each hold a parentSet back-reference to their immediate enclosing Set
    public static class NestedSetBackRefBean implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public Set<NestedSetBackRefBean> parentSet;
        public Set<NestedSetBackRefBean> nested;
    }

    // Two collection fields that both point back to the owning bean
    public static class DualCyclicBean implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public List<DualCyclicBean> loop1;
        public List<DualCyclicBean> loop2;
    }

    public static class DualCyclicHolder implements Serializable {
        private static final long serialVersionUID = 1L;

        @JSONField(ordinal = 1)
        public List<DualCyclicBean> stable;

        @JSONField(ordinal = 2)
        public Set<DualCyclicBean> set;
    }

    // Reaches the owning bean through two independent intermediate links
    public static class DualLinkBean implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public Link link1;
        public Link link2;
    }

    public static class Link implements Serializable {
        private static final long serialVersionUID = 1L;

        public DualLinkBean back;
    }

    public static class DualLinkHolder implements Serializable {
        private static final long serialVersionUID = 1L;

        @JSONField(ordinal = 1)
        public List<DualLinkBean> stable;

        @JSONField(ordinal = 2)
        public Set<DualLinkBean> set;
    }

    public static class EnclosingSharedWrapper implements Serializable {
        private static final long serialVersionUID = 1L;

        @JSONField(ordinal = 1)
        public Set<String> shared;

        @JSONField(ordinal = 2)
        public Set<Bean> data;
    }

    public static class Wrapper implements Serializable {
        private static final long serialVersionUID = 1L;

        public Set<Bean> data;
    }

    public static class MapWrapper implements Serializable {
        private static final long serialVersionUID = 1L;

        public Set<Map<String, Object>> data;
    }
}

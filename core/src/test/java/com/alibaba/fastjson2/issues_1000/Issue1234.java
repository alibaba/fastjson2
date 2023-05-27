package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue1234 {
    @Test
    public void test() {
        QueryDTO queryDTOFather = new QueryDTO();
        QueryDTO queryDTOOutDoor = new QueryDTO();
        QueryDTO queryDTOEquipment = new QueryDTO();
        RelationShipDTO relationShipDTO = new RelationShipDTO();
        relationShipDTO.setPoint(1);
//        AssetDTO assetDTO = new AssetDTO();
//        assetDTO.setQueryCI(Arrays.asList(queryDTOFather, queryDTOOutDoor, queryDTOEquipment));
//        assetDTO.setQueryRe(Arrays.asList(relationShipDTO,relationShipDTO));
        AssetDTO assetDTO = AssetDTO.BuilderQueryCI.startBuild()
                .queryCI(queryDTOFather)
                .queryCI(queryDTOOutDoor)
                .queryCI(queryDTOEquipment)
                .queryRe(relationShipDTO).queryRe(relationShipDTO)
                .page(1).pageSize(9999)
                .create();

        String x = JSONObject.toJSONString(assetDTO, JSONWriter.Feature.ReferenceDetection);
        assertEquals("{\"page\":1,\"pageSize\":9999,\"queryCI\":[{},{},{}],\"queryRe\":[{\"point\":1},{\"$ref\":\"$.queryRe[0]\"}]}", x);
        AssetDTO assetDTO1 = JSONObject.parseObject(x, AssetDTO.class);
        assertSame(assetDTO1.queryRe.get(0), assetDTO1.queryRe.get(1));
    }

    public static class AssetDTO {
        private List<QueryDTO> queryCI;

        private List<RelationShipDTO> queryRe;

        private Integer page;

        private Integer pageSize;

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }

        public AssetDTO() {
        }

        private AssetDTO(BuilderQueryCI builderQueryCI) {
            this.queryCI = builderQueryCI.queryCI;
            this.queryRe = builderQueryCI.queryRe;
            this.page = builderQueryCI.page;
            this.pageSize = builderQueryCI.pageSize;
        }

        public List<QueryDTO> getQueryCI() {
            return queryCI;
        }

        public void setQueryCI(List<QueryDTO> queryCI) {
            this.queryCI = queryCI;
        }

        public List<RelationShipDTO> getQueryRe() {
            return queryRe;
        }

        public void setQueryRe(List<RelationShipDTO> queryRe) {
            this.queryRe = queryRe;
        }

        public static class BuilderQueryCI {
            private List<QueryDTO> queryCI;

            private List<RelationShipDTO> queryRe;

            private Integer page;

            private Integer pageSize;

            public AssetDTO create() {
                return new AssetDTO(this);
            }

            public BuilderQueryCI() {
            }

            public static BuilderQueryCI startBuild() {
                return new BuilderQueryCI();
            }

            public BuilderQueryCI queryCI(QueryDTO... queryDTO) {
                if (this.queryCI == null) {
                    this.queryCI = new ArrayList<>();
                }
                queryCI.addAll(Arrays.asList(queryDTO));
                return this;
            }

            public BuilderQueryCI queryRe(RelationShipDTO... relationShipDTO) {
                if (this.queryRe == null) {
                    this.queryRe = new ArrayList<>();
                }
                queryRe.addAll(Arrays.asList(relationShipDTO));
                return this;
            }

            public BuilderQueryCI page(Integer page) {
                this.page = page;
                return this;
            }

            public BuilderQueryCI pageSize(Integer pageSize) {
                this.pageSize = pageSize;
                return this;
            }
        }
    }

    public static class RelationShipDTO {
        private Integer point;

        public RelationShipDTO() {
        }

        public Integer getPoint() {
            return point;
        }

        public void setPoint(Integer point) {
            this.point = point;
        }
    }

    public static class QueryParam {
    }

    public static class QueryDTO {
    }
}

package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue531 {
    @Test
    public void test() throws Exception {
        URL resource = Issue531.class.getClassLoader().getResource("issues/issue531.json");
        EdsDataSourceDto dto = JSON.parseObject(resource, EdsDataSourceDto.class);
        assertNotNull(dto);

        EdsDataSourceDto dto1 = JSON.parseObject(resource, (Type) EdsDataSourceDto.class);
        assertNotNull(dto1);
    }

    @Data
    public static class VesselVoyageDto {
        private String vslNameCn;
        private String vslName;
        private String voyage;
        private String vslCallSign;
        private String vslImoNo;
        private String primaryRegNo;
        private String vslTypeCode;
        private String shipBuildDate;
        private String vslDeadwtTon;
        private String chemicalTankTypeCode;
        private String gasTankTypeCode;
        private String oilTankStrucCode;
        private String deptDate;
        private String arrivalDate;
        private String msaOperPlaceCode;
        private String adMarkCode;
        private String lineType;
        private String carrierName;
        private String carrierCntctInfo;
    }

    @Data
    public static class UnitDto {
        private String unitTypeCode;
        private String unitIdNo;
        private String unitOpenCode;
        private String ctnrSizeType;
        private String ctnrztnTankFillEntCode;

        private CtnDeclOrCertList containerDeclarationOrCertificateList;
        private VehicleCertificate vehicleCertificate;
        private TankCertificate tankCertificate;

        /**
         *  集装箱装箱申明/ 证明信息
         */
        @Data
        static class CtnDeclOrCertList {
            private String certCtnrztnNo;
            private String ctnrztnInspectorNo;
            private String estPackDate;
            private String estLoadDate;
        }

        /**
         * 车载危险货物证书信息
         */
        @Data
        static class VehicleCertificate {
            private String certVehicleDgrgdGoodsNo;
        }

        /**
         * 罐柜船检证书信息
         */
        @Data
        static class TankCertificate {
            private String certTankVslInspecNo;
            private String firstInspecDate;
            private String annualInspecDate;
            private String perdclInspecDate;
            private String specialInspecDate;
        }
    }

    @Data
    public static class PackagesDto {
        private String pkgSeqNo;
        private String pkgMark;
        private String pkgTypeCode;
        private String pkgType;
        private String pkgQty;
        private String cargoNetWt;
        private String dgrsPkgLimepQtyTypeCode;
        private String cargoNetWtInCtnr;
        private String pkgNetWt;
        private String contTemp;
        private String emgTemp;
        private String maritimePollutionFlag;
        private String rejectamentaFlag;
        private String emptyUncleanPkgFlag;
        private String emsNo;
        private String mfagNo;

        private List<PackagesAppendix> packagesAppendix;

        @Data
        static class PackagesAppendix {
            private String attachType;
            private String attachNo;
            private String attachQty;
            private String remark;
            private String certUniqueId;
        }
    }

    @Data
    public static class MessageAndDischargeDto {
        private String dgrgdDeclMsgNo;
        private String msaDeclAudtNo;
        private String blNo;
        private String spmtMeans;
        private String tradeFlagCode;
        private String cargoWhereaboutCode;
        private String shipperInfoEn;
        private String shipperInfoCn;
        private String cneeInfoEn;
        private String cneeInfoCn;
        private String loadPortCode;
        private String loadPort;
        private String dischargePortCode;
        private String dischargePort;
        private String deliveryPlaceCode;
        private String deliveryPlace;
        private String bookingParty;

        private DangerousCargo dangerousCargo;
        /**
         * 货信息内部类
         */
        static class DangerousCargo {
            private String cargoDesp;
            private String cargoDespEn;
            private String dgrgdUnNo;
            private String domDgrgdGoodsNo;
            private String dgrgdClass;
            private String subDgrgdClass;
            private String ttlGrossWt;
            private String dgrgdFlashPoint;
            private String emgCntctNo;

            public String getCargoDesp() {
                return cargoDesp;
            }

            public void setCargoDesp(String cargoDesp) {
                this.cargoDesp = cargoDesp;
            }

            public String getCargoDespEn() {
                return cargoDespEn;
            }

            public void setCargoDespEn(String cargoDespEn) {
                this.cargoDespEn = cargoDespEn;
            }

            public String getDgrgdUnNo() {
                return dgrgdUnNo;
            }

            public void setDgrgdUnNo(String dgrgdUnNo) {
                this.dgrgdUnNo = dgrgdUnNo;
            }

            public String getDomDgrgdGoodsNo() {
                return domDgrgdGoodsNo;
            }

            public void setDomDgrgdGoodsNo(String domDgrgdGoodsNo) {
                this.domDgrgdGoodsNo = domDgrgdGoodsNo;
            }

            public String getDgrgdClass() {
                return dgrgdClass;
            }

            public void setDgrgdClass(String dgrgdClass) {
                this.dgrgdClass = dgrgdClass;
            }

            public String getSubDgrgdClass() {
                return subDgrgdClass;
            }

            public void setSubDgrgdClass(String subDgrgdClass) {
                this.subDgrgdClass = subDgrgdClass;
            }

            public String getTtlGrossWt() {
                return ttlGrossWt;
            }

            public void setTtlGrossWt(String ttlGrossWt) {
                this.ttlGrossWt = ttlGrossWt;
            }

            public String getDgrgdFlashPoint() {
                return dgrgdFlashPoint;
            }

            public void setDgrgdFlashPoint(String dgrgdFlashPoint) {
                this.dgrgdFlashPoint = dgrgdFlashPoint;
            }

            public String getEmgCntctNo() {
                return emgCntctNo;
            }

            public void setEmgCntctNo(String emgCntctNo) {
                this.emgCntctNo = emgCntctNo;
            }
        }
    }

    @Data
    public class EdsDataSourceDto {
        private String msgType;
        private String fileDesp;
        private String fileFun;
        private String senderCode;
        private String recipientCode;
        private String fileCreateTime;
        private String declrCode;
        private String receiverCode;
        private String sendCode;

        private VesselVoyageDto vesselVoyageInformation;
        private List<MessageAndDischargeDto> messageAndDischargeList;
    }
}

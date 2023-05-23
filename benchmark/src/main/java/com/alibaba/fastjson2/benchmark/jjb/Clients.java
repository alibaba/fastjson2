package com.alibaba.fastjson2.benchmark.jjb;

import com.alibaba.fastjson.annotation.JSONField;
import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@CompiledJson
public class Clients {
    private List<Client> clients;

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    @CompiledJson
    public static final class Client {
        private long id;
        private int index;
        private UUID guid;
        @JSONField(name = "isActive")
        private boolean isActive;
        private BigDecimal balance;
        private String picture;
        private int age;
        private EyeColor eyeColor;
        private String name;
        private String gender;
        private String company;
        private String[] emails;
        private long[] phones;
        private String address;
        private String about;
        private LocalDate registered;
        private double latitude;
        private double longitude;
        @JsonAttribute(nullable = false)
        private List<String> tags;
        @JsonAttribute(nullable = false)
        private List<Partner> partners;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public UUID getGuid() {
            return guid;
        }

        public void setGuid(UUID guid) {
            this.guid = guid;
        }

        public boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(boolean active) {
            isActive = active;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public EyeColor getEyeColor() {
            return eyeColor;
        }

        public void setEyeColor(EyeColor eyeColor) {
            this.eyeColor = eyeColor;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String[] getEmails() {
            return emails;
        }

        public void setEmails(String[] emails) {
            this.emails = emails;
        }

        public long[] getPhones() {
            return phones;
        }

        public void setPhones(long[] phones) {
            this.phones = phones;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAbout() {
            return about;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public LocalDate getRegistered() {
            return registered;
        }

        public void setRegistered(LocalDate registered) {
            this.registered = registered;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public List<Partner> getPartners() {
            return partners;
        }

        public void setPartners(List<Partner> partners) {
            this.partners = partners;
        }
    }

    public enum EyeColor {
        BROWN,
        BLUE,
        GREEN;

        public static EyeColor fromNumber(int i) {
            if (i == 0) {
                return BROWN;
            }
            if (i == 1) {
                return BLUE;
            }
            return GREEN;
        }
    }

    @CompiledJson
    public static final class Partner {
        private long id;
        private String name;
        private OffsetDateTime since;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public OffsetDateTime getSince() {
            return since;
        }

        public void setSince(OffsetDateTime since) {
            this.since = since;
        }
    }
}

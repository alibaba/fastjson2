package com.alibaba.fastjson3.benchmark.jjb;

import java.util.List;

public class Clients {
    private List<Client> clients;

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public static final class Client {
        private long id;
        private int index;
        private String guid;
        private boolean isActive;
        private double balance;
        private String picture;
        private int age;
        private String eyeColor;
        private String name;
        private String gender;
        private String company;
        private String[] emails;
        private long[] phones;
        private String address;
        private String about;
        private String registered;
        private double latitude;
        private double longitude;
        private List<String> tags;
        private List<Partner> partners;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public String getGuid() { return guid; }
        public void setGuid(String guid) { this.guid = guid; }
        public boolean getIsActive() { return isActive; }
        public void setIsActive(boolean active) { isActive = active; }
        public double getBalance() { return balance; }
        public void setBalance(double balance) { this.balance = balance; }
        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEyeColor() { return eyeColor; }
        public void setEyeColor(String eyeColor) { this.eyeColor = eyeColor; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String[] getEmails() { return emails; }
        public void setEmails(String[] emails) { this.emails = emails; }
        public long[] getPhones() { return phones; }
        public void setPhones(long[] phones) { this.phones = phones; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getAbout() { return about; }
        public void setAbout(String about) { this.about = about; }
        public String getRegistered() { return registered; }
        public void setRegistered(String registered) { this.registered = registered; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public List<Partner> getPartners() { return partners; }
        public void setPartners(List<Partner> partners) { this.partners = partners; }
    }

    public static final class Partner {
        private long id;
        private String name;
        private String since;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSince() { return since; }
        public void setSince(String since) { this.since = since; }
    }
}

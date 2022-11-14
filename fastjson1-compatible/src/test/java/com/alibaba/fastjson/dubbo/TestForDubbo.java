package com.alibaba.fastjson.dubbo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestForDubbo {
    static Person person;

    static {
        person = new Person();
        person.setPersonId("superman111");
        person.setLoginName("superman");
        person.setEmail("sm@1.com");
        person.setPenName("pname");
        person.setStatus(PersonStatus.ENABLED);

        ArrayList<Phone> phones = new ArrayList<Phone>();
        Phone phone1 = new Phone("86", "0571", "87654321", "001");
        Phone phone2 = new Phone("86", "0571", "87654322", "002");
        phones.add(phone1);
        phones.add(phone2);
        PersonInfo pi = new PersonInfo();
        pi.setPhones(phones);
        Phone fax = new Phone("86", "0571", "87654321", null);
        pi.setFax(fax);
        FullAddress addr = new FullAddress("CN", "zj", "3480", "wensanlu", "315000");
        pi.setFullAddress(addr);
        pi.setMobileNo("13584652131");
        pi.setMale(true);
        pi.setDepartment("b2b");
        pi.setHomepageUrl("www.capcom.com");
        pi.setJobTitle("qa");
        pi.setName("superman");
        person.setInfoProfile(pi);
    }

    private HelloServiceImpl helloService = new HelloServiceImpl();

    @Test
    public void testPerson() {
        Person p = helloService.showPerson(person);
        String text = JSON.toJSONString(p, SerializerFeature.WriteClassName);
        System.out.println(text);

        Person result = JSON.parseObject(text, Person.class);

        assertEquals(result.getInfoProfile().getPhones().get(0).getArea(),
                person.getInfoProfile().getPhones().get(0).getArea());
        assertEquals(result.getInfoProfile().getPhones().get(0).getCountry(),
                person.getInfoProfile().getPhones().get(0).getCountry());
        assertEquals(result.getInfoProfile().getPhones().get(0).getExtensionNumber(),
                person.getInfoProfile().getPhones().get(0).getExtensionNumber());
        assertEquals(result.getInfoProfile().getPhones().get(0).getNumber(),
                person.getInfoProfile().getPhones().get(0).getNumber());
    }
}

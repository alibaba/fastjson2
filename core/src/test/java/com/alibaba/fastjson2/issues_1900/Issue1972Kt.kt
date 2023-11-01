package com.alibaba.fastjson2.issues_1900

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONReader
import com.alibaba.fastjson2.reader.ObjectReader
import org.junit.Assert
import org.junit.Test
import java.lang.reflect.Type

class Issue1972Kt {
    @Test
    fun test() {
        val json = "{" +
                "       \"groupCode\": \"A12\"," +
                "       \"items\": [" +
                "           {" +
                "               \"impl1Code\": \"code1\"" +
                "           }," +
                "           {" +
                "               \"impl2Code\": \"code2\"" +
                "           }" +
                "       ]" +
                "   }"
        JSON.register(InterfaceDemo::class.java, InterfaceDemoReader())

        val result = JSON.parseObject(json, InterfaceDemo::class.java)
        Assert.assertTrue(result is InterfaceImplGroup)

        val group = result as InterfaceImplGroup
        Assert.assertEquals(group.groupCode, "A12")
        Assert.assertNotNull(group.items)
        Assert.assertEquals(group.items?.size, 2)

        Assert.assertTrue(group.items?.get(0) is InterfaceImpl1)
        Assert.assertTrue(group.items?.get(1) is InterfaceImpl2)

        val impl1 = group.items?.get(0) as InterfaceImpl1
        Assert.assertEquals(impl1.impl1Code, "code1")

        val impl2 = group.items?.get(1) as InterfaceImpl2
        Assert.assertEquals(impl2.impl2Code, "code2")
    }
}

class InterfaceDemoReader : ObjectReader<InterfaceDemo> {
    override fun readObject(reader: JSONReader?, fieldType: Type?, fieldName: Any?, features: Long): InterfaceDemo? {
        if (reader == null || reader.nextIfNull()) return null
        return resolveObject(reader.readJSONObject())
    }

    override fun createInstance(map: MutableMap<Any?, Any?>, features: Long): InterfaceDemo {
        if (map is JSONObject) {
            val value = resolveObject(map)
            if (value != null) return value
        }
        return super.createInstance(map, features)
    }

    private fun resolveObject(obj: JSONObject): InterfaceDemo? {
        if (obj.containsKey("impl1Code")) {
            return obj.to(InterfaceImpl1::class.java)
        } else if (obj.containsKey("impl2Code")) {
            return obj.to(InterfaceImpl2::class.java)
        } else if (obj.containsKey("groupCode")) {
            return obj.to(InterfaceImplGroup::class.java)
        }
        return null
    }
}

interface InterfaceDemo

class InterfaceImpl1 : InterfaceDemo {
    var impl1Code: String? = null
}

class InterfaceImpl2 : InterfaceDemo {
    var impl2Code: String? = null
}

class InterfaceImplGroup : InterfaceDemo {
    var groupCode: String? = null
    var items: List<InterfaceDemo>? = null
}

package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.toJSONString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author kraity
 */
class Issue587 {

    @Test
    fun test0() {
        data class BackupMMKV(
            val mmkvKey: String,
            val mmkvValue: Boolean
        )

        val mmkvList = mutableListOf<BackupMMKV>()
        mmkvList.add(BackupMMKV("test1", true))
        mmkvList.add(BackupMMKV("test2", false))

        assertEquals(
            """{"mmkvKey":"test1","mmkvValue":true}""",
            BackupMMKV("test1", true).toJSONString()
        )
        assertEquals(
            """[{"mmkvKey":"test1","mmkvValue":true},{"mmkvKey":"test2","mmkvValue":false}]""",
            mmkvList.toJSONString()
        )
    }
}

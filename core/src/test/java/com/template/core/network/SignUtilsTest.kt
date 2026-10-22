package com.template.core.network

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 请求签名的契约测试。
 *
 * 为什么这条链最该先测：sign 错了服务端会统一返回 401/403，
 * 现场表现像"账号异常"或"网络问题"，排查成本极高，而它本身是纯函数，测起来最便宜。
 */
class SignUtilsTest {

    private data class Payload(val a: Int, val b: String)

    @After
    fun tearDown() {
        // SignUtils 是单例 object，测完必须复位，否则会污染其他用例
        SignUtils.init("")
    }

    @Test
    fun `未设置密钥时签名为空串`() {
        SignUtils.init("")
        assertEquals("", SignUtils.signData(Payload(1, "x")))
    }

    @Test
    fun `同一份数据与密钥，签名稳定且为 32 位小写十六进制`() {
        SignUtils.init("secret")
        val first = SignUtils.signData(Payload(1, "x"))
        val second = SignUtils.signData(Payload(1, "x"))

        assertEquals(first, second)
        assertEquals(32, first.length)
        assertTrue("签名应全为小写十六进制：$first", first.matches(Regex("^[0-9a-f]{32}$")))
    }

    @Test
    fun `密钥变化会改变签名`() {
        SignUtils.init("secret")
        val withSecretA = SignUtils.signData(Payload(1, "x"))

        SignUtils.init("secret2")
        val withSecretB = SignUtils.signData(Payload(1, "x"))

        assertNotEquals(withSecretA, withSecretB)
    }

    @Test
    fun `业务数据变化会改变签名`() {
        SignUtils.init("secret")
        assertNotEquals(
            SignUtils.signData(Payload(1, "x")),
            SignUtils.signData(Payload(1, "y")),
        )
    }

    /**
     * 黄金值（golden value）：锁定"JSON + &key=secret + MD5"这套算法。
     * 如果哪天有人把分隔符、大小写或哈希算法改了，这条会立刻失败。
     */
    @Test
    fun `签名算法与后端约定一致`() {
        SignUtils.init("secret")
        assertEquals("70ace9aba3e36f892fbb69bc7d1b237e", SignUtils.signData(Payload(1, "x")))
    }
}

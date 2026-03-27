package me.hker.contract

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ServiceInterfaceStructureTest {

    @Test
    fun `auth service should use interface plus impl structure`() {
        val service = Class.forName("me.hker.module.auth.service.AuthService")
        val impl = Class.forName("me.hker.module.auth.service.impl.AuthServiceImpl")
        assertTrue(service.isInterface)
        assertTrue(service.isAssignableFrom(impl))
    }

    @Test
    fun `credit service should use interface plus impl structure`() {
        val service = Class.forName("me.hker.module.credit.service.CreditService")
        val impl = Class.forName("me.hker.module.credit.service.impl.CreditServiceImpl")
        assertTrue(service.isInterface)
        assertTrue(service.isAssignableFrom(impl))
    }

    @Test
    fun `reward service should use interface plus impl structure`() {
        val service = Class.forName("me.hker.module.reward.service.RewardService")
        val impl = Class.forName("me.hker.module.reward.service.impl.RewardServiceImpl")
        assertTrue(service.isInterface)
        assertTrue(service.isAssignableFrom(impl))
    }

    @Test
    fun `template service should use interface plus impl structure`() {
        val service = Class.forName("me.hker.module.template.service.TemplateService")
        val impl = Class.forName("me.hker.module.template.service.impl.TemplateServiceImpl")
        assertTrue(service.isInterface)
        assertTrue(service.isAssignableFrom(impl))
    }
}

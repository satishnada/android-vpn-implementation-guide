package com.satishnada.scopevpn.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AppRoutingPolicyTest {
    @Test
    fun onlySelected_requiresAtLeastOnePackage() {
        val policy = AppRoutingPolicy(mode = RoutingMode.ONLY_SELECTED)

        assertNotNull(policy.validationError())
    }

    @Test
    fun excludedMode_allowsAnEmptyDenylist() {
        val policy = AppRoutingPolicy(mode = RoutingMode.EXCLUDE_SELECTED)

        assertNull(policy.validationError())
    }

    @Test
    fun toggle_addsAndRemovesTheSamePackage() {
        val packageName = "com.example.app"
        val initial = AppRoutingPolicy(mode = RoutingMode.ONLY_SELECTED)

        val selected = initial.toggle(packageName)
        val removed = selected.toggle(packageName)

        assertEquals(setOf(packageName), selected.selectedPackages)
        assertEquals(emptySet<String>(), removed.selectedPackages)
    }
}

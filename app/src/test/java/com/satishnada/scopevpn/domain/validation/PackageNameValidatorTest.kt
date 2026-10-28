package com.satishnada.scopevpn.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageNameValidatorTest {
    @Test
    fun acceptsTypicalAndroidPackageNames() {
        assertTrue(isValidAndroidPackageName("com.example.app"))
        assertTrue(isValidAndroidPackageName("io.company_app.mobile2"))
    }

    @Test
    fun rejectsMalformedPackageNames() {
        assertFalse(isValidAndroidPackageName("chrome"))
        assertFalse(isValidAndroidPackageName("com..example"))
        assertFalse(isValidAndroidPackageName("1com.example.app"))
        assertFalse(isValidAndroidPackageName("com.example.app-name"))
    }
}

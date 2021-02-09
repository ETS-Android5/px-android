package com.mercadopago.android.px.securitycode

import com.mercadopago.android.px.internal.features.security_code.domain.model.BusinessSecurityCodeDisplayData
import com.mercadopago.android.px.internal.features.security_code.mapper.SecurityCodeDisplayModelMapper
import com.mercadopago.android.px.internal.features.security_code.model.SecurityCodeDisplayModel
import com.mercadopago.android.px.internal.viewmodel.LazyString
import com.mercadopago.android.px.internal.mappers.CardUiMapper
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals

class SecurityCodeDisplayModelMapperTest {

    @Test
    fun whenBusinessSecurityCodeDisplayDataToSecurityCodeDisplayModel() {
        val businessSecurityCodeDisplayData = mock(BusinessSecurityCodeDisplayData::class.java)
        val  mapper = SecurityCodeDisplayModelMapper(CardUiMapper)

        `when`(businessSecurityCodeDisplayData.title).thenReturn(LazyString("title"))
        `when`(businessSecurityCodeDisplayData.message).thenReturn(LazyString("message", 3))
        `when`(businessSecurityCodeDisplayData.securityCodeLength).thenReturn(3)

        val resultExpected = SecurityCodeDisplayModel(
            businessSecurityCodeDisplayData.title,
            businessSecurityCodeDisplayData.message,
            businessSecurityCodeDisplayData.securityCodeLength
        )

        val actualResult = mapper.map(businessSecurityCodeDisplayData)

        assertTrue(ReflectionEquals(actualResult).matches(resultExpected))
    }
}

package com.joins.kidcenter.service.persistence

import org.junit.Assert.assertEquals
import org.junit.Test

class StudentCodeGeneratorTest {

    @Test
    fun testTrialIdSimple() {
        assertEquals("001Z", StudentCodeTransformer().stringifyTrialId(1))
    }

    @Test
    fun testTrialIdFirstSymbolLastCode() {
        assertEquals("999Z", StudentCodeTransformer().stringifyTrialId(999))
    }

    @Test
    fun testTrialIdSecondSymbolFirstCode() {
        assertEquals("000Y", StudentCodeTransformer().stringifyTrialId(1000))
    }

    @Test
    fun testTrialIdMax() {
        assertEquals("999A", StudentCodeTransformer().stringifyTrialId(25999))
    }

    @Test
    fun testTrialIdZero() {
        assertEquals("000Z", StudentCodeTransformer().stringifyTrialId(0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrialIdNegative() {
        StudentCodeTransformer().stringifyTrialId(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrialGtMax() {
        StudentCodeTransformer().stringifyTrialId(26000)
    }

    @Test
    fun testRegularIdSimple() {
        assertEquals("A001", StudentCodeTransformer().stringifyRegularId(1))
    }

    @Test
    fun testRegularIdFirstSymbolLastCode() {
        assertEquals("A999", StudentCodeTransformer().stringifyRegularId(999))
    }

    @Test
    fun testRegularIdSecondSymbolFirstCode() {
        assertEquals("B000", StudentCodeTransformer().stringifyRegularId(1000))
    }

    @Test
    fun testRegularIdZero() {
        assertEquals("A000", StudentCodeTransformer().stringifyRegularId(0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRegularIdNegative() {
        StudentCodeTransformer().stringifyRegularId(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRegularIdGtMax() {
        StudentCodeTransformer().stringifyRegularId(26000)
    }
}
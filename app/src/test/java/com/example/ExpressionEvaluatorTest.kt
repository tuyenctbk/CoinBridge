package com.example

import com.example.ui.calculator.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpressionEvaluatorTest {

    @Test
    fun testSimpleAddition() {
        val result = ExpressionEvaluator.evaluate("10 + 20")
        assertEquals(30.0, result, 0.0001)
    }

    @Test
    fun testMultiplicationPrecedence() {
        val result = ExpressionEvaluator.evaluate("10 + 5 * 2")
        assertEquals(20.0, result, 0.0001)
    }

    @Test
    fun testDecimalOperations() {
        val result = ExpressionEvaluator.evaluate("12.5 + 7.5")
        assertEquals(20.0, result, 0.0001)
    }

    @Test
    fun testKeypadInputSequences() {
        var input = "0"
        input = ExpressionEvaluator.onKeyPress(input, "1")
        input = ExpressionEvaluator.onKeyPress(input, "5")
        input = ExpressionEvaluator.onKeyPress(input, "+")
        input = ExpressionEvaluator.onKeyPress(input, "5")
        assertEquals("15+5", input)
        assertEquals(20.0, ExpressionEvaluator.evaluate(input), 0.0001)
    }

    @Test
    fun testPercentageKey() {
        val input = "250"
        val pctResult = ExpressionEvaluator.onKeyPress(input, "%")
        assertEquals("2.5", pctResult)
    }
}

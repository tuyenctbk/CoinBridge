package com.example.ui.calculator

import java.util.Stack

object ExpressionEvaluator {

    fun onKeyPress(currentInput: String, key: String): String {
        return when (key) {
            "C" -> "0"
            "⌫" -> {
                if (currentInput.length <= 1) "0"
                else currentInput.dropLast(1)
            }
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                if (currentInput == "0") key
                else currentInput + key
            }
            "00" -> {
                if (currentInput == "0") "0"
                else currentInput + "00"
            }
            "." -> {
                // Prevent multiple decimals in the last number token
                val lastToken = currentInput.split('+', '-', '*', '/', '×', '÷').lastOrNull() ?: ""
                if (lastToken.contains(".")) currentInput
                else if (currentInput.isEmpty() || isOperator(currentInput.last())) currentInput + "0."
                else currentInput + "."
            }
            "+", "-", "×", "÷", "*", "/" -> {
                val op = when (key) {
                    "*", "×" -> "×"
                    "/", "÷" -> "÷"
                    else -> key
                }
                if (currentInput.isEmpty()) "0$op"
                else if (isOperator(currentInput.last())) currentInput.dropLast(1) + op
                else currentInput + op
            }
            "%" -> {
                if (currentInput.isEmpty() || isOperator(currentInput.last())) currentInput
                else {
                    val evaluated = evaluate(currentInput)
                    (evaluated / 100.0).toCleanString()
                }
            }
            else -> currentInput
        }
    }

    private fun isOperator(c: Char): Boolean = c == '+' || c == '-' || c == '×' || c == '÷' || c == '*' || c == '/'

    fun evaluate(expression: String): Double {
        if (expression.isBlank()) return 0.0
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .trim()

        // If ends with an operator, strip it for evaluation
        val cleanExpr = if (sanitized.isNotEmpty() && (sanitized.last() == '+' || sanitized.last() == '-' || sanitized.last() == '*' || sanitized.last() == '/')) {
            sanitized.dropLast(1)
        } else {
            sanitized
        }

        return try {
            evalSimple(cleanExpr)
        } catch (e: Exception) {
            cleanExpr.toDoubleOrNull() ?: 0.0
        }
    }

    private fun evalSimple(expr: String): Double {
        val tokens = mutableListOf<String>()
        var numberBuffer = StringBuilder()

        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            if (c.isDigit() || c == '.') {
                numberBuffer.append(c)
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                if (numberBuffer.isNotEmpty()) {
                    tokens.add(numberBuffer.toString())
                    numberBuffer = StringBuilder()
                } else if (c == '-' && (tokens.isEmpty() || isOpToken(tokens.last()))) {
                    // Unary minus
                    numberBuffer.append('-')
                    i++
                    continue
                }
                tokens.add(c.toString())
            }
            i++
        }
        if (numberBuffer.isNotEmpty()) {
            tokens.add(numberBuffer.toString())
        }

        if (tokens.isEmpty()) return 0.0

        // Pass 1: Multiplication and Division
        val values = mutableListOf<Double>()
        val ops = mutableListOf<String>()

        var idx = 0
        while (idx < tokens.size) {
            val token = tokens[idx]
            if (token == "*" || token == "/") {
                val nextVal = tokens.getOrNull(idx + 1)?.toDoubleOrNull() ?: 1.0
                val prevVal = values.removeAt(values.size - 1)
                val res = if (token == "*") prevVal * nextVal else if (nextVal != 0.0) prevVal / nextVal else 0.0
                values.add(res)
                idx += 2
            } else if (token == "+" || token == "-") {
                ops.add(token)
                idx++
            } else {
                values.add(token.toDoubleOrNull() ?: 0.0)
                idx++
            }
        }

        // Pass 2: Addition and Subtraction
        if (values.isEmpty()) return 0.0
        var total = values[0]
        for (j in ops.indices) {
            val op = ops[j]
            val nextVal = values.getOrNull(j + 1) ?: 0.0
            if (op == "+") total += nextVal
            else if (op == "-") total -= nextVal
        }

        return total
    }

    private fun isOpToken(token: String): Boolean = token == "+" || token == "-" || token == "*" || token == "/"

    fun Double.toCleanString(): String {
        return if (this == this.toLong().toDouble()) {
            this.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.4f", this).trimEnd('0').trimEnd('.')
        }
    }
}

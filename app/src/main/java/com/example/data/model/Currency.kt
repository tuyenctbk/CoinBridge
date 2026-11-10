package com.example.data.model

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val flagOrIcon: String,
    val isCrypto: Boolean = false,
    val isPopular: Boolean = false
) {
    companion object {
        val ALL_CURRENCIES = listOf(
            // Popular Fiat
            Currency("USD", "US Dollar", "$", "🇺🇸", isPopular = true),
            Currency("EUR", "Euro", "€", "🇪🇺", isPopular = true),
            Currency("JPY", "Japanese Yen", "¥", "🇯🇵", isPopular = true),
            Currency("GBP", "British Pound", "£", "🇬🇧", isPopular = true),
            Currency("AUD", "Australian Dollar", "A$", "🇦🇺", isPopular = true),
            Currency("CAD", "Canadian Dollar", "C$", "🇨🇦", isPopular = true),
            Currency("CHF", "Swiss Franc", "CHF", "🇨🇭", isPopular = true),
            Currency("CNY", "Chinese Yuan", "¥", "🇨🇳", isPopular = true),
            Currency("HKD", "Hong Kong Dollar", "HK$", "🇭🇰"),
            Currency("NZD", "New Zealand Dollar", "NZ$", "🇳🇿"),
            Currency("SGD", "Singapore Dollar", "S$", "🇸🇬", isPopular = true),
            Currency("KRW", "South Korean Won", "₩", "🇰🇷", isPopular = true),
            Currency("MXN", "Mexican Peso", "$", "🇲🇽"),
            Currency("INR", "Indian Rupee", "₹", "🇮🇳", isPopular = true),
            Currency("BRL", "Brazilian Real", "R$", "🇧🇷"),
            Currency("RUB", "Russian Ruble", "₽", "🇷🇺"),
            Currency("ZAR", "South African Rand", "R", "🇿🇦"),
            Currency("TRY", "Turkish Lira", "₺", "🇹🇷"),
            Currency("THB", "Thai Baht", "฿", "🇹🇭", isPopular = true),
            Currency("VND", "Vietnamese Dong", "₫", "🇻🇳", isPopular = true),
            Currency("IDR", "Indonesian Rupiah", "Rp", "🇮🇩"),
            Currency("MYR", "Malaysian Ringgit", "RM", "🇲🇾"),
            Currency("PHP", "Philippine Peso", "₱", "🇵🇭"),
            Currency("PLN", "Polish Zloty", "zł", "🇵🇱"),
            Currency("SEK", "Swedish Krona", "kr", "🇸🇪"),
            Currency("NOK", "Norwegian Krone", "kr", "🇳🇴"),
            Currency("DKK", "Danish Krone", "kr", "🇩🇰"),
            Currency("AED", "UAE Dirham", "🇦🇪", "🇦🇪"),
            Currency("SAR", "Saudi Riyal", "🇸🇦", "🇸🇦"),
            Currency("EGP", "Egyptian Pound", "E£", "🇪🇬"),

            // Popular Crypto (Coinbase)
            Currency("BTC", "Bitcoin", "₿", "🪙", isCrypto = true, isPopular = true),
            Currency("ETH", "Ethereum", "Ξ", "🔷", isCrypto = true, isPopular = true),
            Currency("SOL", "Solana", "SOL", "☀️", isCrypto = true, isPopular = true),
            Currency("XRP", "XRP Ledger", "XRP", "✕", isCrypto = true, isPopular = true),
            Currency("DOGE", "Dogecoin", "Ð", "🐕", isCrypto = true, isPopular = true),
            Currency("ADA", "Cardano", "₳", "🔵", isCrypto = true),
            Currency("USDT", "Tether USD", "₮", "🟢", isCrypto = true, isPopular = true),
            Currency("USDC", "USD Coin", "₵", "🔵", isCrypto = true, isPopular = true),
            Currency("LTC", "Litecoin", "Ł", "🥈", isCrypto = true),
            Currency("BNB", "BNB Smart Chain", "BNB", "🟡", isCrypto = true),
            Currency("LINK", "Chainlink", "LINK", "🔗", isCrypto = true),
            Currency("DOT", "Polkadot", "DOT", "🔴", isCrypto = true),
            Currency("AVAX", "Avalanche", "AVAX", "🔺", isCrypto = true),
            Currency("NEAR", "Near Protocol", "NEAR", "🌐", isCrypto = true),
            Currency("SHIB", "Shiba Inu", "SHIB", "🦊", isCrypto = true)
        )

        val DEFAULT_BASE = ALL_CURRENCIES.first { it.code == "USD" }
        val DEFAULT_TARGETS = listOf("EUR", "JPY", "GBP", "BTC", "ETH", "VND").mapNotNull { code ->
            ALL_CURRENCIES.find { it.code == code }
        }

        fun find(code: String): Currency {
            return ALL_CURRENCIES.find { it.code.equals(code, ignoreCase = true) }
                ?: Currency(code.uppercase(), code.uppercase(), code.uppercase(), "🏳️")
        }
    }
}

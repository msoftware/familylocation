package com.fullmob.familylocation.models

enum class Actions {
    OBTAIN_LOCATION {
        override fun toString(): String {
            return "Obtain Location"
        }
    },
    FORCE_CONNECT {
        override fun toString(): String {
            return "Force Connect"
        }
    },
    UNSILENCE_PHONE {
        override fun toString(): String {
            return "Change phone from silent to normal"
        }
    };
}

typealias ActionableKeywords = Map<Actions, Array<String>>
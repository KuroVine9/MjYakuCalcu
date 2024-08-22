package dev.kuro9.service.mahjong.model

import dev.kuro9.service.mahjong.model.PaiType.Companion.isPaiType

data class MjPai private constructor(
    val num: Int,
    val type: PaiType,
    val isAkaDora: Boolean = false,
    val isHuro: Boolean = false
) : Comparable<MjPai> {
    companion object {
        fun of (num: Int, type: PaiType, isHuro: Boolean = false): MjPai {
            when (type) {
                PaiType.M, PaiType.P, PaiType.S -> check(num in 0..9)
                PaiType.Z -> check(num in 1..7)
            }

            return if (num == 0) MjPai(5, type, true, isHuro)
            else MjPai(num, type, false, isHuro)
        }

        fun String.parseMjPai(isHuro: Boolean = false): List<MjPai> {
            var typePtr: PaiType = PaiType.of(this.last())
            val resultList = mutableListOf<MjPai>()

            for (char in this.replace(" ", "").uppercase().reversed().drop(1)) {
                when {
                    char.isDigit() -> resultList.add(of(char.digitToInt(), typePtr, isHuro))
                    char.isPaiType() -> typePtr = PaiType.of(char)
                    else -> throw IllegalArgumentException("$char is not a valid pai")
                }
            }

            return resultList
        }

        fun String.parseOneHai(): MjPai {
            return parseMjPai(false).takeIf { it.size == 1 }?.first() ?: throw IllegalArgumentException()
        }
    }



    override fun compareTo(other: MjPai): Int {
        check(type == other.type) { "type not matches" }
        return num.compareTo(other.num)
    }

    override fun toString(): String {
        return if (isAkaDora) "0$type" else "$num$type"
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is MjPai -> false
            else -> num == other.num && type == other.type
        }
    }

    override fun hashCode(): Int {
        var result = num
        result = 31 * result + type.hashCode()
        result = 31 * result + isAkaDora.hashCode()
        return result
    }
}

/**
 * - M: 만수
 * - P: 통수
 * - S: 삭수
 * - Z: 자패
 */
enum class PaiType {
    M, P, S, Z;

    companion object {
        fun of(char: Char): PaiType {
            return PaiType.valueOf(char.uppercase())
        }

        fun Char.isPaiType() = this.uppercaseChar().run {
            this == 'M' || this == 'P' || this == 'S' || this == 'Z'
        }
    }
}
package dev.kuro9.service.mahjong.model

import dev.kuro9.service.mahjong.model.MjPai.Companion.of
import dev.kuro9.service.mahjong.model.PaiType.Companion.isPaiType

fun String.parseMjPai(): List<MjPai> {
    var typePtr: PaiType = PaiType.of(this.last())
    val resultList = mutableListOf<MjPai>()

    for (char in this.replace(" ", "").uppercase().reversed().drop(1)) {
        when {
            char.isDigit() -> resultList.add(of(char.digitToInt(), typePtr))
            char.isPaiType() -> typePtr = PaiType.of(char)
            else -> throw IllegalArgumentException("$char is not a valid pai")
        }
    }

    return resultList
}

fun String.parseOneHai(): MjPai {
    return parseMjPai().takeIf { it.size == 1 }?.first() ?: throw IllegalArgumentException()
}

fun String.parseMjBody(isHuro: Boolean = false): MjBody {
    return MjBody.of(this.parseMjPai(), isHuro)
}
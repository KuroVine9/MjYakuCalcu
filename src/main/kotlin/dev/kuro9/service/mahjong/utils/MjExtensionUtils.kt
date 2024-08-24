package dev.kuro9.service.mahjong.utils

import dev.kuro9.service.mahjong.model.MjPai
import dev.kuro9.service.mahjong.model.PaiType

fun List<MjPai>.isMjBody(): Boolean {
    val (standardNum, standardType) = this.firstOrNull() ?: return false
    when {
        this.size !in 3..4 -> return false
        this.all { it.type == standardType }.not() -> return false
        when (this.first().type) {
            PaiType.M, PaiType.P, PaiType.S -> this.all { it.num == standardNum } or
                    (this.sorted().withIndex()
                        .all { (index, pai) -> standardNum + index == pai.num } and (this.size == 3))

            PaiType.Z -> this.all { it.num == standardNum }
        }.not() -> return false
    }
    return true
}

fun MjPai.countDora(doraPaiList: List<MjPai>): Int {
    return (if (isAkaDora) 1 else 0) +
            doraPaiList.count { it == this }
}

package dev.kuro9.service.mahjong.model

class MjHead(private val paiList: List<MjPai>) : MjComponent {
    init {
        check(paiList.size == 2)
        check(paiList.first() == paiList.last())
    }

    override fun getPaiType(): PaiType = paiList.first().type

    override fun isMenzen(): Boolean = true

    override fun isHuro(): Boolean = false

    override fun getDoraCount(doraPaiList: List<MjPai>): Int {
        return paiList.count { it.isAkaDora } +
                doraPaiList.sumOf { doraPai -> paiList.count { doraPai == it } }
    }

    override fun containsYaoPai(): Boolean = paiList.any { it.isYao() }
    override fun isAllYaoPai(): Boolean = paiList.all { it.isYao() }
    override fun isAllNoduPai(): Boolean = paiList.all { it.isNodu() }

    override fun toString(): String {
        val (num, type) = paiList.first()
        return "$num$num$type"
    }
}
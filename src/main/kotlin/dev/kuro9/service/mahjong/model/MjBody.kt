package dev.kuro9.service.mahjong.model

import dev.kuro9.service.mahjong.enumuration.MjKaze


sealed interface MjBody : MjComponent, MjFuuProvider {
    val paiList: List<MjPai>
    val isHuroBody: Boolean

    override fun getPaiType(): PaiType = paiList.first().type
    override fun isMenzen(): Boolean = !isHuroBody
    override fun isHuro(): Boolean = isHuroBody
    override fun getDoraCount(doraPaiList: List<MjPai>): Int {
        return paiList.count { it.isAkaDora } +
                doraPaiList.sumOf { doraPai ->
                    paiList.count { doraPai == it }
                }
    }

    override fun hasAgariHai(agariHai: MjPai): Boolean {
        return agariHai in paiList
    }

    override fun all(predicate: (MjPai) -> Boolean): Boolean {
        return paiList.all(predicate)
    }

    override fun any(predicate: (MjPai) -> Boolean): Boolean {
        return paiList.any(predicate)
    }

    companion object {
        fun of(paiList: List<MjPai>, isHuro: Boolean = false): MjBody {
            val paiListSorted = paiList.sorted()
            val (firstElementNum, firstElementType) = paiListSorted.first()
            return when {
                paiList.size !in 3..4 -> throw IllegalArgumentException("몸통은 3개 또는 4개의 패로만 구성될 수 있습니다. ")
                paiList.any { firstElementType != it.type } -> throw IllegalArgumentException("몸통은 서로 다른 종류로 구성될 수 없습니다. ")

                // 슌쯔
                paiList.any { firstElementNum != it.num } -> {
                    check(paiList.size == 3) { "슌쯔는 3개의 패로만 구성될 수 있습니다." }
                    check(
                        paiListSorted.reversed().withIndex().all { (index, pai) ->
                            firstElementNum + 2 == index + pai.num
                        }
                    ) { "슌쯔는 패가 순서대로 존재해야 합니다." }

                    ShunzuBody(paiList, isHuro)
                }

                paiList.size == 3 -> PongBody(paiList, isHuro)
                paiList.size == 4 -> KanBody(paiList, isHuro)

                else -> throw IllegalStateException("Unhandled case. 패 파싱 실패.")
            }
        }
    }

    sealed interface Shunzu : MjBody
    sealed interface Kutsu : MjBody


    class ShunzuBody internal constructor(
        override val paiList: List<MjPai>,
        override val isHuroBody: Boolean = false,
    ) : Shunzu {
        fun isRyoumen(agariHai: MjPai): Boolean {
            return when (paiList.indexOf(agariHai)) {
                0 -> (paiList.last().num != 9)
                1 -> false
                2 -> (paiList.last().num != 1)
                else -> throw IllegalArgumentException("$agariHai 가 이 몸통($this)에 존재하지 않습니다.")
            }
        }

        override fun getAgariBlockFuu(agariHai: MjAgariHai, ziKaze: MjKaze, baKaze: MjKaze): Int {
            return if (isRyoumen(agariHai.pai)) 0 else 2
        }

        override fun getBlockFuu(ziKaze: MjKaze, baKaze: MjKaze): Int = 0

        override fun containsYaoPai(): Boolean = paiList.any { it.isYao() }
        override fun isAllYaoPai(): Boolean = false
        override fun isAllNoduPai(): Boolean = false

        override fun toString(): String {
            return paiList.joinToString(separator = "", postfix = paiList.first().type.toString()) { it.num.toString() }
                .let {
                    if (isHuroBody) "CHI-($it)" else it
                }
        }

        override fun equals(other: Any?): Boolean {
            return when (other) {
                null -> return false
                !is ShunzuBody -> return false
                else -> (other.isHuroBody == isHuroBody) and other.paiList.zip(paiList).all { (a, b) -> a == b }
            }
        }

        override fun hashCode(): Int {
            var result = paiList.hashCode()
            result = 31 * result + isHuroBody.hashCode()
            return result
        }

    }

    class PongBody internal constructor(
        override val paiList: List<MjPai>,
        override val isHuroBody: Boolean = false,
    ) : Kutsu {
        override fun getAgariBlockFuu(agariHai: MjAgariHai, ziKaze: MjKaze, baKaze: MjKaze): Int {
            var basic = 2
            if (isAllYaoPai()) basic *= 2

            if (agariHai.pai != paiList.first()) return if (isHuro()) basic else basic * 2

            if (isMenzen() && agariHai.isTsumo()) basic *= 2

            return basic
        }

        override fun getBlockFuu(ziKaze: MjKaze, baKaze: MjKaze): Int {
            var basic = 2
            if (isAllYaoPai()) basic *= 2
            if (isMenzen()) basic *= 2
            return basic
        }

        override fun containsYaoPai(): Boolean = paiList.first().isYao()
        override fun isAllYaoPai(): Boolean = containsYaoPai()
        override fun isAllNoduPai(): Boolean = paiList.first().isNodu()

        override fun toString(): String {
            return paiList.joinToString(separator = "", postfix = paiList.first().type.toString()) { it.num.toString() }
                .let {
                    if (isHuroBody) "PONG($it)" else it
                }
        }

        override fun equals(other: Any?): Boolean {
            when (other) {
                null -> return false
                (other !is PongBody) -> return false
            }

            other as PongBody
            return (other.isHuroBody == isHuroBody) and (other.paiList.first() == paiList.first())
        }

        override fun hashCode(): Int {
            var result = paiList.hashCode()
            result = 31 * result + isHuroBody.hashCode()
            return result
        }

    }

    class KanBody internal constructor(
        override val paiList: List<MjPai>,
        override val isHuroBody: Boolean = false,
    ) : Kutsu {
        override fun getAgariBlockFuu(agariHai: MjAgariHai, ziKaze: MjKaze, baKaze: MjKaze): Int {
            var basic = 8
            if (isAllYaoPai()) basic *= 2

            if (agariHai.pai != paiList.first()) return if (isHuro()) basic else basic * 2

            if (isMenzen() && agariHai.isTsumo()) basic *= 2

            return basic
        }

        override fun getBlockFuu(ziKaze: MjKaze, baKaze: MjKaze): Int {
            var basic = 8
            if (isAllYaoPai()) basic *= 2
            if (isMenzen()) basic *= 2
            return basic
        }

        override fun containsYaoPai(): Boolean = paiList.first().isYao()
        override fun isAllYaoPai(): Boolean = containsYaoPai()
        override fun isAllNoduPai(): Boolean = paiList.first().isNodu()

        override fun toString(): String {
            return paiList.joinToString(separator = "", postfix = paiList.first().type.toString()) { it.num.toString() }
                .let {
                    if (isHuroBody) "KANG($it)" else it
                }
        }

        override fun equals(other: Any?): Boolean {
            when (other) {
                null -> return false
                (other !is KanBody) -> return false
            }

            other as KanBody
            return (other.isHuroBody == isHuroBody) and (other.paiList.first() == paiList.first())
        }

        override fun hashCode(): Int {
            var result = paiList.hashCode()
            result = 31 * result + isHuroBody.hashCode()
            return result
        }

    }
}
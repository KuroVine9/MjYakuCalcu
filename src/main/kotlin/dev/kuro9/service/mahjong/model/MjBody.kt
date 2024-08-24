package dev.kuro9.service.mahjong.model


sealed interface MjBody : MjComponent {

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
                paiList.size == 4 -> KangBody(paiList, isHuro)

                else -> throw IllegalStateException("Unhandled case. 패 파싱 실패.")
            }
        }
    }


    class ShunzuBody internal constructor(
        private val paiList: List<MjPai>,
        private val isHuro: Boolean = false,
    ) : MjBody {
        override fun getPaiType(): PaiType = paiList.first().type

        override fun isMenzen(): Boolean = !isHuro

        override fun isHuro(): Boolean = isHuro

        override fun getDoraCount(doraPaiList: List<MjPai>): Int {
            return paiList.count { it.isAkaDora } +
                    doraPaiList.sumOf { doraPai ->
                        paiList.count { doraPai == it }
                    }
        }

        override fun containsYaoPai(): Boolean = paiList.any { it.isYao() }
        override fun isAllYaoPai(): Boolean = false
        override fun isAllNoduPai(): Boolean = false

        override fun toString(): String {
            return paiList.joinToString(separator = "", postfix = paiList.first().type.toString()) { it.num.toString() }
                .let {
                    if (isHuro) "CHI-($it)" else it
                }
        }

    }

    class PongBody internal constructor(
        private val paiList: List<MjPai>,
        private val isHuro: Boolean = false,
    ) : MjBody {
        override fun getPaiType(): PaiType = paiList.first().type

        override fun isMenzen(): Boolean = !isHuro

        override fun isHuro(): Boolean = isHuro

        override fun getDoraCount(doraPaiList: List<MjPai>): Int {
            return paiList.count { it.isAkaDora } +
                    (doraPaiList.count { paiList.first() == it } * 3)
        }

        override fun containsYaoPai(): Boolean = paiList.first().isYao()
        override fun isAllYaoPai(): Boolean = containsYaoPai()
        override fun isAllNoduPai(): Boolean = paiList.first().isNodu()

        override fun toString(): String {
            return paiList.joinToString(separator = "", postfix = paiList.first().type.toString()) { it.num.toString() }
                .let {
                    if (isHuro) "PONG($it)" else it
                }
        }

    }

    class KangBody internal constructor(
        private val paiList: List<MjPai>,
        private val isHuro: Boolean = false,
    ) : MjBody {
        override fun getPaiType(): PaiType = paiList.first().type

        override fun isMenzen(): Boolean = !isHuro

        override fun isHuro(): Boolean = isHuro

        override fun getDoraCount(doraPaiList: List<MjPai>): Int {
            return paiList.count { it.isAkaDora } +
                    (doraPaiList.count { paiList.first() == it } * 4)
        }

        override fun containsYaoPai(): Boolean = paiList.first().isYao()
        override fun isAllYaoPai(): Boolean = containsYaoPai()
        override fun isAllNoduPai(): Boolean = paiList.first().isNodu()

        override fun toString(): String {
            return paiList.joinToString(separator = "", postfix = paiList.first().type.toString()) { it.num.toString() }
                .let {
                    if (isHuro) "KANG($it)" else it
                }
        }

    }
}
package dev.kuro9.service.mahjong.model

import dev.kuro9.service.mahjong.model.MjPai.Companion.parseMjPai
import dev.kuro9.service.mahjong.model.MjPai.Companion.parseOneHai
import dev.kuro9.service.mahjong.model.MjTeHai.Body.Companion.parseMjBody
import kotlinx.css.em

data class MjTeHai (
    private val head: Head,
    private val memzenBody: List<Body>,
    private val tsumoHai: MjPai,
    private val huroBody: List<Body> = emptyList(),
) {
    init { check(memzenBody.size + huroBody.size == 4) }

    fun isHuro(): Boolean = huroBody.isNotEmpty()
    fun isMenzen(): Boolean = !isHuro()
    fun getKanzuCount(): Int = memzenBody.count { it.type == Body.Type.KANZU } +
            huroBody.count { it.type == Body.Type.KANZU }


    companion object {
        fun parse(teHai: List<MjPai>, tsumoHai: MjPai, vararg huroBody: Body): List<MjTeHai> {
            val paiMap: Map<PaiType, MutableList<MjPai>> = mapOf(
                PaiType.M to mutableListOf(),
                PaiType.P to mutableListOf(),
                PaiType.S to mutableListOf(),
                PaiType.Z to mutableListOf(),
            )

            (teHai + tsumoHai).forEach { paiMap[it.type]!!.add(it) }
            paiMap.values.forEach { it.sort() }

            return separateHead(paiMap).map { (head, leftPaiList) -> head to separateBody(leftPaiList) }
                .filterNot { (_, resultBody) -> resultBody.isNullOrEmpty() }
                .flatMap { (head, possibleBodyKatachi) ->
                    possibleBodyKatachi
                        ?.filter { it.size + huroBody.size == 4 }
                        ?.map { MjTeHai(head, it, tsumoHai, huroBody.toList()) } ?: emptyList()
                }
        }

        private fun separateHead(paiMap: Map<PaiType, List<MjPai>>): List<Pair<Head, Map<PaiType, List<MjPai>>>> {
            val resultList: MutableList<Pair<Head, Map<PaiType, List<MjPai>>>> = mutableListOf()
            paiMap.forEach { (type, list) ->
                list.groupBy { it.num }
                    .filter { (_, samePaiList) -> samePaiList.size >= 2 }
                    .forEach { (_, samePaiList) ->
                        paiMap.toMutableMap().also { result ->
                            val targetHead = samePaiList.take(2)
                            result[type] = list.toMutableList().also {
                                it.remove(samePaiList.first())
                                it.remove(samePaiList.last())
                            }
                            resultList.add(Head(targetHead) to result)
                        }
                    }
            }
            return resultList
        }

        /**
         * 패에서 몸통을 분리해 가능한 모든 경우의 수를 출력합니다.
         */
        private fun separateBody(paiMap: Map<PaiType, List<MjPai>>): List<List<Body>>? {
            val ziPaiBody = paiMap.getOrElse(PaiType.Z) { emptyList() }.groupBy { it.num }
                .also {
                    if (it.values.any { samePaiList -> samePaiList.size !in 3..4 }) return null
                }
                .map { (_, list) -> Body(list) }

            val bodyByType: Map<PaiType, List<List<Body>>> = paiMap.filterNot { it.key == PaiType.Z }
                .map { (type, paiList) ->type to separateBodyR(leftPai = paiList.sorted().toMutableList()) }
                .toMap()

            val manzuBodyList = bodyByType.getOrDefault(PaiType.M, emptyList())
            val souzuBodyList = bodyByType.getOrDefault(PaiType.S, emptyList())
            val pinzuBodyList = bodyByType.getOrDefault(PaiType.P, emptyList())

            return manzuBodyList.flatMap { first ->
                souzuBodyList.flatMap { second ->
                    pinzuBodyList.map { third ->
                            first + second + third + ziPaiBody
                    }
                }
            }
        }

        /**
         * 패에서 몸통을 분리해 가능한 모든 경우의 수를 출력합니다.
         * @param nowPai 현재 빌딩중인 패의 몸통 리스트
         * @param leftPai 몸통을 만들고 남은 패
         * @return List<가능한 몸통 형태>
         */
        private fun separateBodyR(nowPai: List<Body> = emptyList(), leftPai: List<MjPai>): List<List<Body>> {
            when {
                leftPai.isEmpty() -> return listOf(nowPai)
                leftPai.size < 3 -> return emptyList()

            }

            val firstPai = leftPai.first()

            val nextPai = leftPai.find { it.num == firstPai.num + 1 }
            val nextNextPai = nextPai?.let { leftPai.find { target -> target.num == it.num + 1 }}

            val samePai = leftPai.filter { it.num == firstPai.num }

            val resultList: MutableList<List<Body>> = mutableListOf()

            // 슌쯔일 때
            if (nextPai != null && nextNextPai != null) {
                val body = Body(listOf(firstPai, nextPai, nextNextPai))
                resultList += separateBodyR(
                    nowPai = nowPai.toMutableList().also { it.add(body) },
                    leftPai = leftPai.toMutableList().also {
                        it.remove(firstPai)
                        it.remove(nextPai)
                        it.remove(nextNextPai)
                    }
                )
            }

            // 커쯔(3개)일 때
            if (samePai.size >= 3) {
                val toUse = samePai.take(3)
                val body = Body(toUse)
                resultList += separateBodyR(
                    nowPai = nowPai.toMutableList().also { it.add(body) },
                    leftPai = leftPai.toMutableList().also {
                        it.remove(toUse[0])
                        it.remove(toUse[1])
                        it.remove(toUse[2])
                    }
                )
            }

            // 깡쯔일 때
            if (samePai.size == 4) {
                val body = Body(samePai)
                resultList += separateBodyR(
                    nowPai = nowPai.toMutableList().also { it.add(body) },
                    leftPai = leftPai.toMutableList().also {
                        it.remove(samePai[0])
                        it.remove(samePai[1])
                        it.remove(samePai[2])
                        it.remove(samePai[3])
                    }
                )
            }

            return resultList
        }

        /**
         *
         */
        private fun isMjBody(paiList: List<MjPai>): Boolean {
            val (standardNum, standardType) = paiList.firstOrNull() ?: return false
            when {
                paiList.size !in 3..4 -> return false
                paiList.all { it.type == standardType }.not() -> return false
                when(paiList.first().type) {
                    PaiType.M, PaiType.P, PaiType.S -> paiList.all { it.num == standardNum } or
                            (paiList.sorted().withIndex().all { (index, pai) -> standardNum + index == pai.num } and (paiList.size == 3))
                    PaiType.Z -> paiList.all { it.num == standardNum }
                }.not() -> return false
            }
            return true
        }
    }

    class Head(private val paiList: List<MjPai>) {
        init {
            check(paiList.size == 2)
            check(paiList.first() == paiList.last())
        }

        override fun toString(): String {
            val (num, type) = paiList.first()
            return "$num$num$type"
        }
    }

    class Body(private val paiList: List<MjPai>) {
        init { check(isMjBody(paiList)) }

        val type: Type = when {
            paiList.size == 4 -> Type.KANZU
            paiList.all { it.num == paiList.first().num } -> Type.KUTSU
            else -> Type.SHUNZU
        }

        override fun toString(): String {
            return paiList.joinToString(separator = "", postfix = paiList.first().type.toString()) { it.num.toString() }
        }

        companion object {
            fun String.parseMjBody(): Body {
                return Body(parseMjPai(true))
            }
        }

        enum class Type {
            SHUNZU, KUTSU, KANZU
        }
    }
}


fun main() {
    // println(MjTeHai.parse("111222333s1222z".parseMjPai(), "1z".parseOneHai()))

    println()

    println(MjTeHai.parse("11333444466s".parseMjPai(), "1s".parseOneHai(), "777s".parseMjBody()))
}
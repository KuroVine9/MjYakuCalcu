package dev.kuro9.service.mahjong.model

import dev.kuro9.service.mahjong.MjYakuParser
import dev.kuro9.service.mahjong.utils.MjFuuToHanVo

data class MjTeHai(
    private val head: MjHead,
    private val body: List<MjBody>,
    private val agariHai: MjAgariHai,
) {
    init {
        check(body.size == 4)
    }

    val isHuro: Boolean by lazy { body.any { it.isHuro() } }
    val isMenzen: Boolean = !isHuro
    fun getDoraCount(doraPaiList: List<MjPai>): Int {
        return head.getDoraCount(doraPaiList) +
                body.sumOf { it.getDoraCount(doraPaiList) } +
                agariHai.getDoraCount(doraPaiList)
    }

    /**
     * 가능한 부/판수의 형태를 모두 리턴합니다.
     */
    fun getPossibleFuuHan(gameInfo: MjGameInfoVo): Set<MjFuuToHanVo> {
        val blockList = (body + head)
        val hasAgariHaiBlockIndex = blockList.withIndex().filter { (index, block) ->
            block.hasAgariHai(agariHai.pai)
        }.map { it.index }

        return hasAgariHaiBlockIndex.map{
            val notAgariBlockList = blockList.filterIndexed { index, _ -> index != it }

            val fuu = blockList[it].getAgariBlockFuu(agariHai, gameInfo.zikaze, gameInfo.bakaze) + notAgariBlockList
                .sumOf { block -> block.getBlockFuu(gameInfo.zikaze, gameInfo.bakaze) }
            val yakuSet = MjYakuParser.getYaku(
                agariHai,
                blockList[it],
                gameInfo.bakaze,
                gameInfo.zikaze,
                *notAgariBlockList.toTypedArray()
            )
            MjFuuToHanVo(fuu, yakuSet.sumOf { it.han })
        }.toSet()
    }

    companion object {
        fun parse(teHai: List<MjPai>, agariHai: MjAgariHai, vararg huroBody: MjBody): List<MjTeHai> {
            val paiMap: Map<PaiType, MutableList<MjPai>> = mapOf(
                PaiType.M to mutableListOf(),
                PaiType.P to mutableListOf(),
                PaiType.S to mutableListOf(),
                PaiType.Z to mutableListOf(),
            )

            (teHai + agariHai.pai).forEach { paiMap[it.type]!!.add(it) }
            paiMap.values.forEach { it.sort() }

            return separateHead(paiMap).map { (head, leftPaiList) -> head to separateBody(leftPaiList) }
                .filterNot { (_, resultBody) -> resultBody.isNullOrEmpty() }
                .flatMap { (head, possibleBodyKatachi) ->
                    possibleBodyKatachi
                        ?.filter { it.size + huroBody.size == 4 }
                        ?.map { MjTeHai(head, it + huroBody, agariHai) } ?: emptyList()
                }
        }

        private fun separateHead(paiMap: Map<PaiType, List<MjPai>>): List<Pair<MjHead, Map<PaiType, List<MjPai>>>> {
            val resultList: MutableList<Pair<MjHead, Map<PaiType, List<MjPai>>>> = mutableListOf()
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
                            resultList.add(MjHead(targetHead) to result)
                        }
                    }
            }
            return resultList
        }

        /**
         * 패에서 몸통을 분리해 가능한 모든 경우의 수를 출력합니다.
         */
        private fun separateBody(paiMap: Map<PaiType, List<MjPai>>): List<List<MjBody>>? {
            val ziPaiBody = paiMap.getOrElse(PaiType.Z) { emptyList() }.groupBy { it.num }
                .also {
                    if (it.values.any { samePaiList -> samePaiList.size !in 3..4 }) return null
                }
                .map { (_, list) -> MjBody.of(list, false) }

            val bodyByType: Map<PaiType, List<List<MjBody>>> = paiMap.filterNot { it.key == PaiType.Z }
                .map { (type, paiList) -> type to separateBodyR(leftPai = paiList.sorted().toMutableList()) }
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
        private fun separateBodyR(nowPai: List<MjBody> = emptyList(), leftPai: List<MjPai>): List<List<MjBody>> {
            when {
                leftPai.isEmpty() -> return listOf(nowPai)
                leftPai.size < 3 -> return emptyList()

            }

            val firstPai = leftPai.first()

            val nextPai = leftPai.find { it.num == firstPai.num + 1 }
            val nextNextPai = nextPai?.let { leftPai.find { target -> target.num == it.num + 1 } }

            val samePai = leftPai.filter { it.num == firstPai.num }

            val resultList: MutableList<List<MjBody>> = mutableListOf()

            // 슌쯔일 때
            if (nextPai != null && nextNextPai != null) {
                val body = MjBody.of(listOf(firstPai, nextPai, nextNextPai), false)
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
                val body = MjBody.of(toUse, false)
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
                val body = MjBody.of(samePai, false)
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
    }
}


fun main() {
    // println(MjTeHai.parse("111222333s1222z".parseMjPai(), "1z".parseOneHai()))

    println()

    // println(MjTeHai.parse("1112223334445s".parseMjPai(), "5s".parseOneHai()))
}
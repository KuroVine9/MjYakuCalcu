package dev.kuro9.service.mahjong.utils

import dev.kuro9.service.mahjong.model.MjTeHai

object MjScoreUtil {

    private val MANKAN = (8000 to 12000) to MjScoreI.Tsumo.MANKAN
    private val scoreTable: Map<Int, List<Pair<Pair<Int, Int>, MjScoreI.Tsumo>>> = buildMap {
        put {
            20 to listOf(
                (700 to 1000) to (200 mjTo 400),
                (1300 to 2000) to (400 mjTo 700),
                (2600 to 3900) to (700 mjTo 1300),
                (5200 to 7700) to (1300 mjTo 2600),
                MANKAN
            )
        }
        put {
            30 to listOf(
                (1000 to 1500) to (300 mjTo 500),
                (2000 to 2900) to (500 mjTo 1000),
                (3900 to 5800) to (1000 mjTo 2000),
                (7700 to 11600) to (2000 mjTo 3900),
                MANKAN
            )
        }
        put {
            40 to listOf(
                (1300 to 2000) to (400 mjTo 700),
                (2600 to 3900) to (700 mjTo 1300),
                (5200 to 7700) to (1300 mjTo 2600),
                MANKAN,
                MANKAN
            )
        }
        put {
            50 to listOf(
                (1600 to 2400) to (400 mjTo 800),
                (3200 to 4800) to (800 mjTo 1600),
                (6400 to 9600) to (1600 mjTo 3200),
                MANKAN,
                MANKAN
            )
        }
        put {
            60 to listOf(
                (2000 to 2900) to (500 mjTo 1000),
                (3900 to 5800) to (1000 mjTo 2000),
                (7700 to 11600) to (2000 mjTo 3900),
                MANKAN,
                MANKAN
            )
        }
        put {
            70 to listOf(
                (2300 to 3400) to (600 mjTo 1200),
                (4500 to 6800) to (1200 mjTo 2300),
                MANKAN,
                MANKAN,
                MANKAN
            )
        }
    }

    /**
     * @param fuuToHan 부수 to 판수
     */
    fun getRonScore(fuuToHan: MjFuuToHanVo, isOya: Boolean): MjScoreVo<MjScoreI.Ron> {
        val (fuu, han) = fuuToHan
        val scoreEnum = MjScore.ofHan(han)
        return when (MjScore.ofHan(han)) {
            MjScore.YAKUMAN -> (han / 13) * 32000
            MjScore.SANBAIMAN -> 24000
            MjScore.BAIMAN -> 16000
            MjScore.HANEMAN -> 12000
            MjScore.MANKAN -> 8000
            MjScore.ELSE -> {
                val (score, _) = scoreTable[fuu] ?: throw IllegalStateException("Unsupport fuu: han=$han to fuu=$fuu")
                val (ronScore, _) = score
                val (koScore, oyaScore) = ronScore
                return (if (isOya) oyaScore else koScore).toRonScoreVo().toScoreVo(scoreEnum, fuu, han)
            }
        }.let { if (isOya) it * 3 / 2 else it }.toRonScoreVo().toScoreVo(scoreEnum, fuu, han)
    }

    /**
     * @param fuuToHan 부수 to 판수
     */
    fun getTsumoScore(fuuToHan: MjFuuToHanVo): MjScoreVo<MjScoreI.Tsumo> {
        val (fuu, han) = fuuToHan
        val scoreEnum = MjScore.ofHan(han)
        return when (scoreEnum) {
            MjScore.YAKUMAN -> MjScoreI.Tsumo.YAKUMAN * (han / 13)
            MjScore.SANBAIMAN -> MjScoreI.Tsumo.SANBAIMAN
            MjScore.BAIMAN -> MjScoreI.Tsumo.BAIMAN
            MjScore.HANEMAN -> MjScoreI.Tsumo.HANEMAN
            MjScore.MANKAN -> MjScoreI.Tsumo.MANKAN
            MjScore.ELSE -> {
                val (score, _) = scoreTable[fuu] ?: throw IllegalStateException("Unsupport fuu: han=$han to fuu=$fuu")
                val (_, tsumoScore) = score
                tsumoScore
            }
        }.toScoreVo(scoreEnum, fuu, han)
    }

    fun List<MjTeHai>.toFuuHan() {

    }


    enum class MjScore(val hanRange: IntRange) {
        YAKUMAN(13..Int.MAX_VALUE),
        SANBAIMAN(11..12),
        BAIMAN(8..10),
        HANEMAN(6..7),
        MANKAN(5..5),
        ELSE(Int.MIN_VALUE..4);

        companion object {
            fun ofHan(han: Int): MjScore = MjScore.entries.first { han in it.hanRange }
        }
    }

    private fun <T, K> MutableMap<T, K>.put(action: () -> Pair<T, K>) {
        val (key, value) = action()
        this[key] = value
    }

    private infix fun Int.mjTo(oyaScore: Int) = MjScoreI.Tsumo(this, oyaScore)
    private fun Int.toRonScoreVo() = MjScoreI.Ron(this)
    private fun <T : MjScoreI> T.toScoreVo(scoreEnum: MjScore, fuu: Int, han: Int) = MjScoreVo(
        scoreEnum = scoreEnum,
        fuu = fuu,
        han = han,
        score = this
    )
}

data class MjFuuToHanVo(val fuu: Int, val han: Int)
data class MjScoreVo<T : MjScoreI>(
    val scoreEnum: MjScoreUtil.MjScore,
    val fuu: Int,
    val han: Int,
    val score: T
)

sealed interface MjScoreI {
    @JvmInline
    value class Ron(val score: Int) : MjScoreI
    data class Tsumo(val koScore: Int, val oyaScore: Int) : MjScoreI {

        operator fun times(times: Int) = Tsumo(koScore * times, oyaScore * times)

        companion object {
            val YAKUMAN = Tsumo(8000, 16000)
            val SANBAIMAN = Tsumo(6000, 12000)
            val BAIMAN = Tsumo(4000, 8000)
            val HANEMAN = Tsumo(3000, 12000)
            val MANKAN = Tsumo(2000, 4000)
        }
    }
}

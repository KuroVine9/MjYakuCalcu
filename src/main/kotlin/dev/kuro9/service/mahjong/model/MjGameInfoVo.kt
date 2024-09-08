package dev.kuro9.service.mahjong.model

import dev.kuro9.service.mahjong.enumuration.MjKaze
import dev.kuro9.service.mahjong.enumuration.MjSeki

data class MjGameInfoVo(
    val gameSeq: MjGameSeq,
    val honba: Int = 0,
    val firstOya: MjSeki,
    val tenhouCount: Int = 0,
    val dora: List<MjPai>,
    val uraDora: List<MjPai>
) {
    /** 자풍패 */
    val zikaze: MjKaze by lazy {
        when (val seq = (-firstOya.ordinal - (gameSeq.num - 1) + 12) % 4) {
            0 -> MjKaze.TOU
            1 -> MjKaze.NAN
            2 -> MjKaze.SHA
            3 -> MjKaze.PEI

            else -> throw IllegalStateException("$seq cannot be reach")
        }
    }

    /** 장풍패 */
    val bakaze: MjKaze = gameSeq.kaze

    val isOya: Boolean = zikaze == MjKaze.TOU
}

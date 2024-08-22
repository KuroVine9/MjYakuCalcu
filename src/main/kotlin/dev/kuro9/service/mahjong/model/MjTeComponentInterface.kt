package dev.kuro9.service.mahjong.model

sealed interface MjTeComponentInterface {
    /** 만, 통, 삭, 자패 구별 */
    fun getPaiType(): PaiType
    /** 해당 몸통 멘젠 여부 */
    fun isMenzen(): Boolean
    /** 해당 몸통 후로 여부 */
    fun isHuro(): Boolean
    /** 도라 카운트 */
    fun getDoraCount(doraPai: List<MjPai>): Int
    /** 요구패 포함 여부(any) */
    fun containsYaoPai(): Boolean
    /** 모든 패 요구패(all) */
    fun isAllYaoPai(): Boolean
    /** 모든 패 노두패(all) */
    fun isAllNoduPai(): Boolean
}
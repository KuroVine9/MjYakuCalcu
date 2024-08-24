package dev.kuro9.service.mahjong.model

enum class MjYaku(val han: Int, val onlyMenzen: Boolean, val kuiSagari: Boolean, val isYakuman: Boolean) {
    RIICHI(1, true, false, false),
    IPPATSU(1, true, false, false),
    TSUMO(1, true, false, false),

    YAKU_BAKASE(1, false, false, false),
    YAKU_ZIKASE(1, false, false, false),
    YAKU_HAKU(1, false, false, false),
    YAKU_HATSU(1, false, false, false),
    YAKU_CHUU(1, false, false, false),

    TANYAO(1, false, false, false),
    PINFU(1, true, false, false),
    IPECO(1, true, false, false),
    CHANKAN(1, false, false, false),
    HAITEI(1, false, false, false),
    HOUTEI(1, false, false, false),


    DOUBLE_RIICHI(2, true, false, false),
    CHANTA(2, false, true, false),
    HONROUTOU(2, false, false, false),
    SANSHOKU_DOUJUU(2, false, true, false),
    SANSHOKU_DOUKOU(2, false, false, false),
    ITTKITSUKAN(2, false, true, false),
    TOITOI(2, false, false, false),
    SANANKOU(2, false, false, false),
    SANKANTSU(2, false, false, false),
    CHITOITSU(2, true, false, false),


    JUNCHANTA(3, false, true, false),
    HONITSU(3, false, true, false),
    RYANPEKO(3, true, false, false),


    SHOUSANGEN(3, false, false, false),
    CHINITSU(6, false, true, false),


}

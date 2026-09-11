package com.escapecrystalnotify;

final class EscapeCrystalNotifyThresholdDefaults {
    static final int DEFAULT_CRYSTAL_THRESHOLD_SECONDS = 4;

    private EscapeCrystalNotifyThresholdDefaults() {}

    static int seconds(EscapeCrystalNotifyRegion encounter) {
        switch (EscapeCrystalNotifyEncounters.canonical(encounter)) {
            case BOSS_ABYSSAL_SIRE:
            case BOSS_ARAXXOR:
            case BOSS_ARTIO:
            case BOSS_CALVARION:
            case BOSS_COMMANDER_ZILYANA:
            case BOSS_CERBERUS:
            case BOSS_CORP:
            case BOSS_CRAZY_ARCHAEOLOGIST:
            case BOSS_DERANGED_ARCHAEOLOGIST:
            case BOSS_DKS:
            case BOSS_DOOM_OF_MOKHAIOTL:
            case BOSS_DUKE_SUCELLUS:
            case BOSS_FORTIS_COLOSSEUM:
            case BOSS_GAUNTLET:
            case BOSS_CORRUPTED_GAUNTLET:
            case BOSS_GAUNTLET_LOBBY:
            case BOSS_GENERAL_GRAARDOR:
            case BOSS_GROTESQUE_GUARDIANS:
            case BOSS_HYDRA:
            case BOSS_INFERNO:
            case BOSS_INFERNO_ENTRANCE:
            case BOSS_KING_BLACK_DRAGON:
            case BOSS_KING_BLACK_DRAGON_ENTRANCE:
            case BOSS_KQ:
            case BOSS_KREEARRA:
            case BOSS_KRIL_TSUTSAROTH:
            case BOSS_MAGGOT_KING:
            case BOSS_MIMIC:
            case BOSS_MIMIC_ENTRANCE:
            case BOSS_MYSTERIOUS_FIGURE:
            case BOSS_NEX:
            case BOSS_NIGHTMARE:
            case BOSS_NIGHTMARE_ENTRANCE:
            case BOSS_PHANTOM_MUSPAH:
            case BOSS_PRIFDDINAS_RABBIT:
            case BOSS_PRIFDDINAS_RABBIT_ENTRANCE:
            case BOSS_SKOTIZO:
            case BOSS_SPINDEL:
            case BOSS_THE_LEVIATHAN:
            case BOSS_THE_LEVIATHAN_ENTRANCE:
            case BOSS_THE_WHISPERER:
            case BOSS_TZHAAR_FIGHT_CAVES:
            case BOSS_TZHAAR_FIGHT_CAVES_ENTRANCE:
            case BOSS_VARDORVIS:
            case BOSS_VORKATH:
            case BOSS_YAMA:
            case BOSS_ZALCANO:
            case BOSS_ZULRAH:
            case BOSS_ZULRAH_ENTRANCE:
            case RAIDS_CHAMBERS_OF_XERIC:
            case RAIDS_THEATRE_OF_BLOOD:
            case RAIDS_TOMBS_OF_AMASCUT:
                return 2;
            case BOSS_PERILOUS_MOONS:
            case BOSS_ROYAL_TITANS:
            case BOSS_SARACHNIS:
            case BOSS_SHELLBANE_GRYPHON:
            case BOSS_SHELLBANE_GRYPHON_ENTRANCE:
                return 3;
            case BOSS_AMOXLIATL:
            case BOSS_BRYOPHYTA:
            case BOSS_GIANT_MOLE:
            case BOSS_HESPORI:
            case BOSS_HUEYCOATL:
            case BOSS_KRAKEN:
            case BOSS_OBOR:
            case BOSS_SCURRIUS:
            case BOSS_SMOKE_DEVIL:
                return 4;
            case BOSS_BARROWS: return 5;
            case BOSS_WINTERTODT:
            case BOSS_WINTERTODT_ENTRANCE:
                return 8;
            default: return DEFAULT_CRYSTAL_THRESHOLD_SECONDS;
        }
    }
}

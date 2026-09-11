package com.escapecrystalnotify;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.api.gameval.ItemID;

/** Panel metadata only; entrance detection continues to use the existing region definitions. */
final class EscapeCrystalNotifyEncounters {
    private static final Map<EscapeCrystalNotifyRegionEntrance, EscapeCrystalNotifyRegion> ENTRANCES = new IdentityHashMap<>();
    private static final Set<EscapeCrystalNotifyRegion> WITH_ENTRANCE = EnumSet.noneOf(EscapeCrystalNotifyRegion.class);
    private static final List<EscapeCrystalNotifyRegion> ALL = Arrays.stream(EscapeCrystalNotifyRegion.values())
        .filter(EscapeCrystalNotifyEncounters::supported).map(EscapeCrystalNotifyEncounters::canonical).distinct()
        .sorted(Comparator.comparing(EscapeCrystalNotifyRegion::getRegionName)).collect(Collectors.toUnmodifiableList());
    static {
        for (EscapeCrystalNotifyRegion region : EscapeCrystalNotifyRegion.values()) {
            if (supported(region) && region.getRegionEntrance() != null) {
                ENTRANCES.put(region.getRegionEntrance(), canonical(region));
                if (!region.getRegionEntrance().isEscapeCrystalDisabled() && region.getRegionEntrance().getOverlayType().canHighlight()) {
                    WITH_ENTRANCE.add(canonical(region));
                }
            }
        }
    }

    static boolean supported(EscapeCrystalNotifyRegion region) {
        return region.getRegionType() == EscapeCrystalNotifyRegionType.BOSSES
            || region.getRegionType() == EscapeCrystalNotifyRegionType.RAIDS;
    }

    static EscapeCrystalNotifyRegion canonical(EscapeCrystalNotifyRegion region) {
        switch (region) {
            case BOSS_GAUNTLET_LOBBY:
            case BOSS_CORRUPTED_GAUNTLET: return EscapeCrystalNotifyRegion.BOSS_GAUNTLET;
            case RAIDS_OSMUMTENS_BURIAL_CHAMBER: return EscapeCrystalNotifyRegion.RAIDS_TOMBS_OF_AMASCUT;
            default:
                return region.name().endsWith("_ENTRANCE")
                    ? EscapeCrystalNotifyRegion.valueOf(region.name().replaceFirst("_ENTRANCE$", "")) : region;
        }
    }

    static List<EscapeCrystalNotifyRegion> all() {
        return ALL;
    }

    static EscapeCrystalNotifyRegion forEntrance(EscapeCrystalNotifyRegionEntrance entrance) {
        return ENTRANCES.get(entrance);
    }

    static boolean hasEntrance(EscapeCrystalNotifyRegion region) {
        return WITH_ENTRANCE.contains(canonical(region));
    }

    static int icon(EscapeCrystalNotifyRegion region) {
        switch (canonical(region)) {
            case BOSS_SHELLBANE_GRYPHON: return ItemID.GRYPHONBOSSPET;
            case BOSS_MAGGOT_KING: return ItemID.MAGGOTKINGPET;
            case BOSS_MAD_ANGEL: return ItemID.MADANGELPET;
            case BOSS_ABYSSAL_SIRE: return ItemID.ABYSSALSIRE_PET;
            case BOSS_AMOXLIATL: return ItemID.AMOXLIATLPET;
            case BOSS_ARAXXOR: return ItemID.ARAXXORPET;
            case BOSS_ARTIO: return ItemID.CALLISTO_PET;
            case BOSS_BARROWS: return ItemID.BARROWS_DHAROK_HEAD;
            case BOSS_BRYOPHYTA: return ItemID.GB_MOSS_ESSENCE;
            case BOSS_CALVARION: return ItemID.VETION_PET;
            case BOSS_CERBERUS: return ItemID.HELL_PET;
            case BOSS_COMMANDER_ZILYANA: return ItemID.SARADOMINPET;
            case BOSS_CORP: return ItemID.COREPET;
            case BOSS_CRAZY_ARCHAEOLOGIST:
            case BOSS_DERANGED_ARCHAEOLOGIST: return ItemID.FEDORA;
            case BOSS_DKS: return ItemID.REXPET;
            case BOSS_DOOM_OF_MOKHAIOTL: return ItemID.DOMPET;
            case BOSS_DUKE_SUCELLUS: return ItemID.DUKESUCELLUSPET;
            case BOSS_GAUNTLET: return ItemID.GAUNTLETPET;
            case BOSS_FORTIS_COLOSSEUM: return ItemID.SOLHEREDITPET;
            case BOSS_GENERAL_GRAARDOR: return ItemID.BANDOSPET;
            case BOSS_GIANT_MOLE: return ItemID.MOLEPET;
            case BOSS_GROTESQUE_GUARDIANS: return ItemID.DAWNPET;
            case BOSS_HESPORI: return ItemID.HESPORI_SEED;
            case BOSS_HUEYCOATL: return ItemID.HUEYPET;
            case BOSS_HYDRA: return ItemID.HYDRAPET;
            case BOSS_INFERNO: return ItemID.INFERNOPET;
            case BOSS_KING_BLACK_DRAGON: return ItemID.KBDPET;
            case BOSS_KQ: return ItemID.KQPET_WALKING;
            case BOSS_KRAKEN: return ItemID.KRAKENPET;
            case BOSS_KREEARRA: return ItemID.ARMADYLPET;
            case BOSS_KRIL_TSUTSAROTH: return ItemID.ZAMORAKPET;
            case BOSS_MIMIC: return ItemID.TRAIL_MIMIC_CASKET;
            case BOSS_MYSTERIOUS_FIGURE: return ItemID.ANCIENT_ICON;
            case BOSS_NEX: return ItemID.NEXPET;
            case BOSS_NIGHTMARE: return ItemID.NIGHTMAREPET;
            case BOSS_OBOR: return ItemID.HILLGIANT_BOSS_CLUB;
            case BOSS_PERILOUS_MOONS: return ItemID.DUAL_MACUAHUITL;
            case BOSS_PHANTOM_MUSPAH: return ItemID.MUSPAHPET;
            case BOSS_PRIFDDINAS_RABBIT: return ItemID.HUNTING_RABBIT_FOOT;
            case BOSS_ROYAL_TITANS: return ItemID.RTBRANDAPET;
            case BOSS_SARACHNIS: return ItemID.SARACHNISPET;
            case BOSS_SCURRIUS: return ItemID.SCURRIUSPET;
            case BOSS_SKOTIZO: return ItemID.SKOTIZOPET;
            case BOSS_SPINDEL: return ItemID.VENENATIS_PET;
            case BOSS_SMOKE_DEVIL: return ItemID.SMOKEPET;
            case BOSS_THE_LEVIATHAN: return ItemID.LEVIATHANPET;
            case BOSS_THE_WHISPERER: return ItemID.WHISPERERPET;
            case BOSS_TZHAAR_FIGHT_CAVES: return ItemID.JAD_PET;
            case BOSS_VARDORVIS: return ItemID.VARDORVISPET;
            case BOSS_VORKATH: return ItemID.VORKATHPET;
            case BOSS_WINTERTODT: return ItemID.PHOENIXPET;
            case BOSS_YAMA: return ItemID.YAMAPET;
            case BOSS_ZALCANO: return ItemID.ZALCANOPET;
            case BOSS_ZULRAH: return ItemID.SNAKEPET;
            case RAIDS_CHAMBERS_OF_XERIC: return ItemID.OLMPET;
            case RAIDS_THEATRE_OF_BLOOD: return ItemID.VERZIKPET;
            case RAIDS_TOMBS_OF_AMASCUT: return ItemID.WARDENPET_TUMEKEN;
            case BOSS_BRUTUS: return 33124;
            default: return ItemID.TOB_TELEPORT;
        }
    }
}

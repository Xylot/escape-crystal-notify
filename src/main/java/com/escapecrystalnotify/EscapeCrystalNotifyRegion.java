package com.escapecrystalnotify;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public enum EscapeCrystalNotifyRegion {

    /*
    Credit to the 'discord' Runelite plugin for many of the region IDs:

    https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/discord/DiscordGameEventType.java
     */

    BOSS_ABYSSAL_SIRE("Abyssal Sire", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.PRIORITIZED_WITH_HIGHLIGHT, List.of(774740, 774742, 780884, 780886), 27048, 27049, 27050, 27051, 27052, 27053), 11851, 11850, 12106, 12363, 12362),
    BOSS_AMOXLIATL("Amoxliatl", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(410803, 410804), 55355), 5446, 6294, 6550),
    BOSS_ARAXXOR("Araxxor", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(935114, 935115, 937162, 937163), 54161), 14489, 14745),
    BOSS_ARTIO("Artio", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.PRIORITIZED_WITH_HIGHLIGHT, List.of(797131), 47141), 7092, 12345),
    BOSS_BARROWS("Barrows", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14131, 14231),
    BOSS_BRYOPHYTA("Bryophyta", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(812245), 32534), 12955),
    BOSS_CALVARION("Calvar'ion", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.PRIORITIZED_WITH_HIGHLIGHT, List.of(813516), 46996), 7604, 12601),
    BOSS_CERBERUS("Cerberus", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(329884, 333982, 340124), 23104), 4883, 5139, 5140, 5395),
    BOSS_COMMANDER_ZILYANA("Commander Zilyana", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.WESTWARD, List.of(744082), 26504, 40420), 11601, 11602, 11858),
    BOSS_CORP("Corporeal Beast", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.EASTWARD, List.of(760339, 760340), 677), 11842, 11844),
    BOSS_CRAZY_ARCHAEOLOGIST("Crazy Archaeologist", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11833),
    BOSS_DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.SOUTHWARD, List.of(942544), 31842), 14650, 14649),
    BOSS_DKS("Dagannoth Kings", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(487969), 3831), 7492, 11588, 11589),
    BOSS_DOOM_OF_MOKHAIOTL("Doom of Mokhaiotl", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(335018, 337066), 57289), 5268, 5269, 13668, 14180),
    BOSS_DUKE_SUCELLUS("Duke Sucellus", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.NORTHWARD, List.of(776996), 49138), 12132),
    BOSS_GAUNTLET_LOBBY("The Gauntlet Lobby", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(776958), 36084, 37340), 12127),
    BOSS_GAUNTLET("The Gauntlet", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7512),
    BOSS_CORRUPTED_GAUNTLET("Corrupted Gauntlet", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7768),
    BOSS_FORTIS_COLOSSEUM("Fortis Colosseum", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(464036), 50751), 7216, 7316),
    BOSS_GENERAL_GRAARDOR("General Graardor", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.EASTWARD, List.of(731805), 26503, 40420), 11347),
    BOSS_GIANT_MOLE("Giant Mole", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6993, 6992),
    BOSS_GROTESQUE_GUARDIANS("Grotesque Guardians", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(876986, 876987), 31673, 31681), 6727, 13623),
    BOSS_HESPORI("Hespori", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(318700, 318701, 320748, 320749), 33729, 34630), 5021),
    BOSS_HUEYCOATL("The Hueycoatl", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.SOUTHWARD, List.of(389530, 391578), 55202, 55401), 5939),
    BOSS_HYDRA("Alchemical Hydra", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.EASTWARD, null, 34553, 34554), 5536),
    BOSS_INFERNO("The Inferno", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 9043),
    BOSS_INFERNO_ENTRANCE("The Inferno Entrance", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(637567, 637568, 637569, 639615, 639616, 639617), 30282, 30352), List.of(637567, 637568, 637569, 639615, 639616, 639617), 9807, 9808, 10063, 10064),
    BOSS_KING_BLACK_DRAGON("King Black Dragon", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9033, 12448),
    BOSS_KING_BLACK_DRAGON_ENTRANCE("King Black Dragon Entrance", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.PRIORITIZED_WITH_HIGHLIGHT, List.of(785665), 1816), List.of(785665, 785666), 12192),
    BOSS_KQ("Kalphite Queen", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(898211), 23609, 40421), 13972),
    BOSS_KRAKEN("Kraken", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(584932), 537), 9116),
    BOSS_KREEARRA("Kree'arra", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.NORTHWARD, List.of(725653), 26502, 40420), 11346),
    BOSS_KRIL_TSUTSAROTH("K'ril Tsutsaroth", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.SOUTHWARD, List.of(748186), 26505, 40420), 11603),
    BOSS_MIMIC("The Mimic", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10819),
    BOSS_MIMIC_ENTRANCE("The Mimic Entrance", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(420286), 34733), List.of(420286), 6455),
    BOSS_NEX("Nex", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(744074), 42940, 42968), 11344, 11345, 11600, 11601),
    BOSS_NIGHTMARE("Nightmare of Ashihama", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(), 29706, 29710), 15515),
    BOSS_NIGHTMARE_ENTRANCE("Nightmare of Ashihama Entrance", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(974019, 974020, 974022, 976067, 976068, 976070), 9460, 29706, 29710), 15256),
    BOSS_OBOR("Obor", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(791757), 29486, 29487), 12441),
    BOSS_PERILOUS_MOONS("Perilous Moons", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(361652, 369847, 375988), 51372, 51373, 51374), 5525, 5526, 5527, 5528, 5782, 5783, 6037, 6038, 6039),
    BOSS_PHANTOM_MUSPAH("Phantom Muspah", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(744713), 46597), 11330, 11681),
    BOSS_PRIFDDINAS_RABBIT("Prifddinas Rabbit", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 13252),
    BOSS_PRIFDDINAS_RABBIT_ENTRANCE("Prifddinas Rabbit Entrance", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(580011), 36598), List.of(580011), 9013),
    BOSS_ROYAL_TITANS("Royal Titans", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(754860), 55986), 11669, 11925),
    BOSS_SARACHNIS("Sarachnis", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.SOUTHWARD, List.of(472279), 34858), 7322),
    BOSS_SCURRIUS("Scurrius", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.EASTWARD, List.of(840913), 14203), 13210),
    BOSS_SKOTIZO("Skotizo", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.PRIORITIZED_WITH_HIGHLIGHT, List.of(), 0), 9048),
    BOSS_SPINDEL("Spindel", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.PRIORITIZED_WITH_HIGHLIGHT, List.of(815572), 47078), 6580, 12602),
    BOSS_SMOKE_DEVIL("Thermonuclear smoke devil", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.PRIORITIZED_WITH_HIGHLIGHT, List.of(609437), 535), 9363, 9619),
    BOSS_THE_LEVIATHAN("The Leviathan", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 8291),
    BOSS_THE_LEVIATHAN_ENTRANCE("The Leviathan Entrance", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(529188), 49212), 8292),
    BOSS_THE_WHISPERER("The Whisperer", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, null, 12203), 10595),
    BOSS_TZHAAR_FIGHT_CAVES("Tzhaar Fight Caves", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 9551),
    BOSS_TZHAAR_FIGHT_CAVES_ENTRANCE("Tzhaar Fight Caves Entrance", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(623237), 11833), List.of(627333, 627334, 627335, 625285, 625285, 625287, 623237, 623238, 623239), 9808),
    BOSS_VARDORVIS("Vardorvis", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(285100), 48740, 49495), 4405, 4661),
    BOSS_VORKATH("Vorkath", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(582138), 31990), 9023),
    BOSS_WINTERTODT("Wintertodt", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6462),
    BOSS_WINTERTODT_ENTRANCE("Wintertodt Entrance", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.PRIORITIZED_WITH_HIGHLIGHT, List.of(416239, 418287), 29322), List.of(416238, 416239, 418287, 418287), 6461),
    BOSS_YAMA("Yama", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(367851, 367852, 369899, 369900), 14185), 5789, 6045),
    BOSS_ZALCANO("Zalcano", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, EscapeCrystalNotifyRegionEntranceDirection.SOUTHWARD, List.of(776949), 36201), 12126),
    BOSS_ZULRAH("Zulrah", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9007, 9008),
    BOSS_ZULRAH_ENTRANCE("Zulrah Entrance", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(565630, 567678), 10068, 46242), List.of(561533, 561534, 563581, 563582, 565630, 567678),8751),
    DUNGEON_ANCIENT_CAVERN("Ancient Cavern", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6483, 6995),
    DUNGEON_ANCIENT_GUTHIXIAN_TEMPLE("Ancient Guthixian Temple", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 15943, 16199, 16455, 16196, 16197, 16452, 16453),
    DUNGEON_APE_ATOLL("Ape Atoll Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11150, 10894),
    DUNGEON_ASGARNIAN_ICE_CAVES("Asgarnian Ice Caves", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11925, 12181),
    DUNGEON_BRIMHAVEN("Brimhaven Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10901, 10900, 10899, 10645, 10644, 10643),
    DUNGEON_BRINE_RAT_CAVERN("Brine Rat Cavern", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10910),
    DUNGEON_CATACOMBS_OF_KOUREND("Catacombs of Kourend", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6557, 6556, 6813, 6812),
    DUNGEON_CHAMPIONS_CHALLENGE("Champions' Challenge", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12696),
    DUNGEON_CHASM_OF_FIRE("Chasm of Fire", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 5789),
    DUNGEON_CORSAIR_COVE("Corsair Cove Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 8076, 8332),
    DUNGEON_CRANDOR("Crandor Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11414),
    DUNGEON_CRASH_SITE_CAVERN("Crash Site Cavern", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 8280, 8536),
    DUNGEON_DARKMEYER("Darkmeyer", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14388),
    DUNGEON_DORGESHKAAN("Dorgesh-Kaan South Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10833),
    DUNGEON_DORGESHUUN_MINES("Dorgeshuun Mines", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12950, 13206),
    DUNGEON_EDGEVILLE("Edgeville Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12441, 12442, 12443, 12698),
    DUNGEON_ELEMENTAL_WORKSHOP("Elemental Workshop", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10906, 7760),
    DUNGEON_ELVEN_RABBIT_CAVE("Elven rabbit cave", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 13252),
    DUNGEON_EVIL_CHICKENS_LAIR("Evil Chicken's Lair", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9796),
    DUNGEON_EXPERIMENT_CAVE("Experiment Cave", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14235, 13979),
    DUNGEON_FORTHOS("Forthos Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7323),
    DUNGEON_FREMENNIK_SLAYER("Fremennik Slayer Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10907, 10908, 11164),
    DUNGEON_FREMENNIK_ISLES("Fremennik Isles", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9276, 9532),
    DUNGEON_GIANTS_DEN("Giant's Den", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 5785),
    DUNGEON_GLARIALS_TOMB("Glarial's Tomb", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10137),
    DUNGEON_GOD_WARS("God Wars Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11578),
    DUNGEON_HEROES_GUILD("Heroes' Guild Mine", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11674),
    DUNGEON_IORWERTH("Iorwerth Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12737, 12738, 12993, 12994),
    DUNGEON_ISLE_OF_SOULS("Isle of Souls Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 8593),
    DUNGEON_JATIZSO_MINES("Jatizso Mines", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9631),
    DUNGEON_JIGGIG_BURIAL_TOMB("Jiggig Burial Tomb", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9875, 9874),
    DUNGEON_JOGRE("Jogre Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11412),
    DUNGEON_KARAMJA("Karamja Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11413),
    DUNGEON_KARUULM("Karuulm Slayer Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 5280, 5279, 5278, 5023, 5535, 5022, 4766, 4510, 4511, 4767, 4768, 4512),
    DUNGEON_KRUK("Kruk's Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9358, 9359, 9360, 9615, 9616, 9871, 10125, 10126, 10127, 10128, 10381, 10382, 10383, 10384, 10637, 10638, 10639, 10640),
    DUNGEON_LEGENDS_GUILD("Legends' Guild Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10904),
    DUNGEON_LIGHTHOUSE("Lighthouse", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10140),
    DUNGEON_LITHKREN("Lithkren", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6223),
    DUNGEON_LIZARDMAN_CAVES("Lizardman Caves", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 5275),
    DUNGEON_LIZARDMAN_TEMPLE("Lizardman Temple", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 5277),
    DUNGEON_LUMBRIDGE_SWAMP_CAVES("Lumbridge Swamp Caves", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12693, 12949),
    DUNGEON_LUNAR_ISLE_MINE("Lunar Isle Mine", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9377),
    DUNGEON_MOS_LE_HARMLESS_CAVES("Mos Le'Harmless Caves", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14994, 14995, 15251),
    DUNGEON_MOURNER_TUNNELS("Mourner Tunnels", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7752, 8008),
    DUNGEON_MYREDITCH_LABORATORIES("Myreditch Laboratories", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14232, 14233, 14487, 14488),
    DUNGEON_MYTHS_GUILD("Myths' Guild Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7564, 7820, 7821),
    DUNGEON_OBSERVATORY("Observatory Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9362),
    DUNGEON_OGRE_ENCLAVE("Ogre Enclave", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10387),
    DUNGEON_OURANIA("Ourania Cave", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12119),
    DUNGEON_RASHILIYIAS_TOMB("Rashiliyta's Tomb", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11668),
    DUNGEON_SHADE_CATACOMBS("Shade Catacombs", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 13975),
    DUNGEON_SHADOW("Shadow Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10575, 10831),
    DUNGEON_SHAYZIEN_CRYPTS("Shayzien Crypts", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6043),
    DUNGEON_SISTERHOOD_SANCTUARY("Sisterhood Sanctuary", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14999, 15000, 15001, 15255, 15256, 15257, 15511, 15512, 15513),
    DUNGEON_SLAYER_TOWER("Slayer Tower", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 13623, 13723),
    DUNGEON_SMOKE("Smoke Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12690, 12946, 13202),
    DUNGEON_SOPHANEM("Sophanem Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 13200),
    DUNGEON_SOURHOG_CAVE("Sourhog Cave", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12695),
    DUNGEON_STRONGHOLD_SECURITY("Stronghold of Security", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7505, 8017, 8530, 9297),
    DUNGEON_STRONGHOLD_SLAYER("Stronghold Slayer Cave", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9624, 9625, 9880, 9881),
    DUNGEON_TARNS_LAIR("Tarn's Lair", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12616, 12615),
    DUNGEON_TAVERLEY("Taverley Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11416, 11417, 11671, 11672, 11673, 11928, 11929),
    DUNGEON_TEMPLE_OF_IKOV("Temple of Ikov", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10649, 10905, 10650),
    DUNGEON_TEMPLE_OF_LIGHT("Temple of Light", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7496),
    DUNGEON_TEMPLE_OF_MARIMBO("Temple of Marimbo", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11151),
    DUNGEON_TOLNA("Dungeon of Tolna", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 13209),
    DUNGEON_UNDERGROUND_PASS("Underground Pass", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9369, 9370),
    DUNGEON_VIYELDI_CAVES("Viyeldi Caves", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9545, 11153),
    DUNGEON_WARRIORS_GUILD("Warriors' Guild Basement", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11675),
    DUNGEON_WATERBIRTH("Waterbirth Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9886, 10142, 7492, 7748),
    DUNGEON_WATERFALL("Waterfall Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10394),
    DUNGEON_WHITE_WOLF_MOUNTAIN_CAVES("White Wolf Mountain Caves", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11418, 11419),
    DUNGEON_WITCHAVEN_SHRINE("Witchhaven Shrine Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10903),
    DUNGEON_WOODCUTTING_GUILD("Woodcutting Guild Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6298),
    DUNGEON_WYVERN_CAVE("Wyvern Cave", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14495, 14496),
    DUNGEON_YANILLE_AGILITY("Yanille Agility Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10388),
    DUNGEON_ZEMOUREGALS_BASE("Zemouregal's Base", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14151, 14407),
    RAIDS_CHAMBERS_OF_XERIC("Chambers of Xeric", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 12889, 13136, 13137, 13138, 13139, 13140, 13141, 13145, 13393, 13394, 13395, 13396, 13397, 13401),
    RAIDS_CHAMBERS_OF_XERIC_ENTRANCE("Chambers of Xeric Entrance", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(315838), 29777), List.of(313788, 313789, 313790, 315836, 315837, 315838, 317883, 317884, 317885), 4919),
    RAIDS_THEATRE_OF_BLOOD("Theatre of Blood", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12611, 12612, 12613, 12867, 12869, 13122, 13123, 13125, 13379),
    RAIDS_THEATRE_OF_BLOOD_ENTRANCE("Theatre of Blood Entrance", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(940434), 32653), List.of(938385, 938386, 938387, 940433, 940434, 940435), 14642),
    RAIDS_TOMBS_OF_AMASCUT("Tombs of Amascut", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14160, 14162, 14164, 14674, 14676, 15184, 15186, 15188, 15696, 15698, 15700),
    RAIDS_TOMBS_OF_AMASCUT_ENTRANCE("Tombs of Amascut Entrance", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE, new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, List.of(859251, 861299), 46089), 13454),
    RAIDS_OSMUMTENS_BURIAL_CHAMBER("Osmumten's Burial Chamber", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14672),
    MG_BARBARIAN_ASSAULT("Barbarian Assault", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 7508, 7509, 10322),
    MG_NIGHTMARE_ZONE("Nightmare Zone", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 9033),
    MG_PEST_CONTROL("Pest Control", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 10536),
    MG_PYRAMID_PLUNDER("Pyramid Plunder", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7749),
    MG_TEMPLE_TREKKING("Temple Trekking", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 8014, 8270, 8256, 8782, 9038, 9294, 9550, 9806),
    MG_VOLCANIC_MINE("Volcanic Mine", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 15263, 15262);

    @Getter
    private String regionName;
    @Getter
    private EscapeCrystalNotifyRegionType regionType;
    @Getter
    private int[] regionIds;
    @Getter
    private List<Integer> chunkIds;
    @Getter
    private EscapeCrystalNotifyRegionDeathType regionDeathType;
    @Getter
    private EscapeCrystalNotifyRegionEntrance regionEntrance;

    EscapeCrystalNotifyRegion(String regionName, EscapeCrystalNotifyRegionType regionType, EscapeCrystalNotifyRegionDeathType regionDeathType, int... regionIds) {
        this.regionName = regionName;
        this.regionType = regionType;
        this.regionDeathType = regionDeathType;
        this.regionEntrance = null;
        this.regionIds = regionIds;
        this.chunkIds = null;
    }

    EscapeCrystalNotifyRegion(String regionName, EscapeCrystalNotifyRegionType regionType, EscapeCrystalNotifyRegionDeathType regionDeathType, List<Integer> chunkIds, int... regionIds) {
        this.regionName = regionName;
        this.regionType = regionType;
        this.regionDeathType = regionDeathType;
        this.regionEntrance = null;
        this.regionIds = regionIds;
        this.chunkIds = chunkIds;
    }

    EscapeCrystalNotifyRegion(String regionName, EscapeCrystalNotifyRegionType regionType, EscapeCrystalNotifyRegionDeathType regionDeathType, EscapeCrystalNotifyRegionEntrance regionEntrance, int... regionIds) {
        this.regionName = regionName;
        this.regionType = regionType;
        this.regionDeathType = regionDeathType;
        this.regionEntrance = regionEntrance;
        this.regionIds = regionIds;
        this.chunkIds = null;
    }

    EscapeCrystalNotifyRegion(String regionName, EscapeCrystalNotifyRegionType regionType, EscapeCrystalNotifyRegionDeathType regionDeathType, EscapeCrystalNotifyRegionEntrance regionEntrance, List<Integer> chunkIds, int... regionIds) {
        this.regionName = regionName;
        this.regionType = regionType;
        this.regionDeathType = regionDeathType;
        this.regionEntrance = regionEntrance;
        this.regionIds = regionIds;
        this.chunkIds = chunkIds;
    }

    public static List<Integer> getAllRegionIds() {
        return Arrays.stream(EscapeCrystalNotifyRegion.values())
                .flatMap(regionType -> Arrays.stream(regionType.getRegionIds()).boxed())
                .collect(Collectors.toList());
    }

    public static List<Integer> getAllEntranceIds() {
        return Arrays.stream(EscapeCrystalNotifyRegion.values())
                .map(EscapeCrystalNotifyRegion::getRegionEntrance)
                .filter(Objects::nonNull)
                .flatMap(entrance -> Arrays.stream(entrance.getEntranceIds()).boxed())
                .collect(Collectors.toList());
    }

    public static List<Integer> getRegionIdsFromTypes(List<EscapeCrystalNotifyRegionType> selectedRegionTypes, List<EscapeCrystalNotifyRegionDeathType> selectedRegionDeathTypes) {
        return Arrays.stream(EscapeCrystalNotifyRegion.values())
                .filter(region -> selectedRegionTypes.contains(region.getRegionType()))
                .filter(region -> selectedRegionDeathTypes.contains(region.getRegionDeathType()))
                .flatMap(region -> Arrays.stream(region.getRegionIds()).boxed())
                .collect(Collectors.toList());
    }

    public static List<Integer> getRegionIdsFromRegions(List<EscapeCrystalNotifyRegion> selectedRegions) {
        return selectedRegions.stream()
                .flatMap(subRegionType -> Arrays.stream(subRegionType.getRegionIds()).boxed())
                .collect(Collectors.toList());
    }

    public static Map<Integer, EscapeCrystalNotifyRegionEntrance> getRegionEntranceMap() {
        Map<Integer, EscapeCrystalNotifyRegionEntrance> regionEntranceMap = new HashMap<>();
        for (EscapeCrystalNotifyRegion e : values()) {
            for (int regionId : e.regionIds) {
                if (e.regionEntrance != null) {
                    regionEntranceMap.put(regionId, e.regionEntrance);
                }
            }
        }
        return regionEntranceMap;
    }

    public static Map<Integer, List<Integer>> getRegionChunkRequirementsMap() {
        Map<Integer, List<Integer>> regionChunkRequirementsMap = new HashMap<>();
        for (EscapeCrystalNotifyRegion e : values()) {
            for (int regionId : e.regionIds) {
                if (e.chunkIds != null) {
                    regionChunkRequirementsMap.put(regionId, e.chunkIds);
                }
            }
        }
        return regionChunkRequirementsMap;
    }

    public static Map<Integer, EscapeCrystalNotifyRegionEntrance> getChunkEntranceMap() {
        Map<Integer, EscapeCrystalNotifyRegionEntrance> chunkEntranceMap = new HashMap<>();
        for (EscapeCrystalNotifyRegion e : values()) {
            if (e.regionEntrance != null && e.regionEntrance.chunkIds != null) {
                for (int chunkId : e.regionEntrance.getChunkIds()) {
                    chunkEntranceMap.put(chunkId, e.regionEntrance);
                }
            }
        }
        return chunkEntranceMap;
    }
}

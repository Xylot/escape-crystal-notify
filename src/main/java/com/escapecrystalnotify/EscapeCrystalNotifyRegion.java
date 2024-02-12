package com.escapecrystalnotify;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EscapeCrystalNotifyRegion {

    /*
    Credit to the 'discord' Runelite plugin for most of this information:

    https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/discord/DiscordGameEventType.java
     */

    BOSS_ABYSSAL_SIRE("Abyssal Sire", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11851, 11850, 12363, 12362),
    BOSS_BARROWS("Barrows", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14131, 14231),
    BOSS_CERBERUS("Cerberus", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 4883, 5140, 5395),
    BOSS_COMMANDER_ZILYANA("Commander Zilyana", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11602),
    BOSS_CORP("Corporeal Beast", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11842, 11844),
    BOSS_DKS("Dagannoth Kings", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11588, 11589),
    BOSS_DUKE_SUCELLUS("Duke Sucellus", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12132),
    BOSS_GAUNTLET("The Gauntlet", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12127, 7512),
    BOSS_CORRUPTED_GAUNTLET("Corrupted Gauntlet", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7768),
    BOSS_GENERAL_GRAARDOR("General Graardor", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11347),
    BOSS_GIANT_MOLE("Giant Mole", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6993, 6992),
    BOSS_GROTESQUE_GUARDIANS("Grotesque Guardians", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6727),
    BOSS_HESPORI("Hespori", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 5021),
    BOSS_HYDRA("Alchemical Hydra", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 5536),
    BOSS_INFERNO("The Inferno", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 9043),
    BOSS_KQ("Kalphite Queen", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 13972),
    BOSS_KRAKEN("Kraken", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9116),
    BOSS_KREEARRA("Kree'arra", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11346),
    BOSS_KRIL_TSUTSAROTH("K'ril Tsutsaroth", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11603),
    BOSS_NEX("Nex", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11601),
    BOSS_NIGHTMARE("Nightmare of Ashihama", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 15515),
    BOSS_PHANTOM_MUSPAH("Phantom Muspah", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11330),
    BOSS_SARACHNIS("Sarachnis", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7322),
    BOSS_SKOTIZO("Skotizo", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6810),
    BOSS_SMOKE_DEVIL("Thermonuclear smoke devil", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9363, 9619),
    BOSS_THE_LEVIATHAN("The Leviathan", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 8291),
    BOSS_THE_WHISPERER("The Whisperer", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10595),
    BOSS_TZHAAR_FIGHT_CAVES("Tzhaar Fight Caves", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 9551),
    BOSS_VARDORVIS("Vardorvis", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 4405),
    BOSS_VORKATH("Vorkath", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9023),
    BOSS_WINTERTODT("Wintertodt", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6462),
    BOSS_ZALCANO("Zalcano", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12126),
    BOSS_ZULRAH("Zulrah", EscapeCrystalNotifyRegionType.BOSSES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9007),
    DUNGEON_ANCIENT_CAVERN("Ancient Cavern", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 6483, 6995),
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
    DUNGEON_DORGESHKAAN("Dorgesh-Kaan South Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10833),
    DUNGEON_DORGESHUUN_MINES("Dorgeshuun Mines", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12950, 13206),
    DUNGEON_EDGEVILLE("Edgeville Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12441, 12442, 12443, 12698),
    DUNGEON_ELEMENTAL_WORKSHOP("Elemental Workshop", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10906, 7760),
    DUNGEON_ELVEN_RABBIT_CAVE("Elven rabbit cave", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 13252),
    DUNGEON_EVIL_CHICKENS_LAIR("Evil Chicken's Lair", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9796),
    DUNGEON_EXPERIMENT_CAVE("Experiment Cave", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14235, 13979),
    DUNGEON_FORTHOS("Forthos Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7323),
    DUNGEON_FREMENNIK_SLAYER("Fremennik Slayer Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10907, 10908, 11164),
    DUNGEON_GLARIALS_TOMB("Glarial's Tomb", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10137),
    DUNGEON_HEROES_GUILD("Heroes' Guild Mine", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11674),
    DUNGEON_IORWERTH("Iorwerth Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12737, 12738, 12993, 12994),
    DUNGEON_ISLE_OF_SOULS("Isle of Souls Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 8593),
    DUNGEON_JATIZSO_MINES("Jatizso Mines", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9631),
    DUNGEON_JIGGIG_BURIAL_TOMB("Jiggig Burial Tomb", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9875, 9874),
    DUNGEON_JOGRE("Jogre Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11412),
    DUNGEON_KARAMJA("Karamja Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 11413),
    DUNGEON_KARUULM("Karuulm Slayer Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 5280, 5279, 5023, 5535, 5022, 4766, 4510, 4511, 4767, 4768, 4512),
    DUNGEON_KRUK("Kruk's Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 9358, 9359, 9360, 9615, 9616, 9871, 10125, 10126, 10127, 10128, 10381, 10382, 10383, 10384, 10637, 10638, 10639, 10640),
    DUNGEON_LEGENDS_GUILD("Legends' Guild Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10904),
    DUNGEON_LIGHTHOUSE("Lighthouse", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 10140),
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
    DUNGEON_SMOKE("Smoke Dungeon", EscapeCrystalNotifyRegionType.DUNGEONS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12946, 13202),
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
    RAIDS_CHAMBERS_OF_XERIC("Chambers of Xeric", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 12889, 13136, 13137, 13138, 13139, 13140, 13141, 13145, 13393, 13394, 13395, 13396, 13397, 13401),
    RAIDS_THEATRE_OF_BLOOD("Theatre of Blood", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 12611, 12612, 12613, 12867, 12869, 13122, 13123, 13125, 13379),
    RAIDS_TOMBS_OF_AMASCUT("Tombs of Amascut", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14160, 14162, 14164, 14674, 14676, 15184, 15186, 15188, 15696, 15698, 15700),
    RAIDS_JALTEVAS_PYRAMID("Jaltevas Pyramid", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 13454),
    RAIDS_OSMUMTENS_BURIAL_CHAMBER("Osmumten's Burial Chamber", EscapeCrystalNotifyRegionType.RAIDS, EscapeCrystalNotifyRegionDeathType.UNSAFE, 14672),
    MG_BARBARIAN_ASSAULT("Barbarian Assault", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 7508, 7509, 10322),
    MG_NIGHTMARE_ZONE("Nightmare Zone", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 9033),
    MG_PEST_CONTROL("Pest Control", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM, 10536),
    MG_PYRAMID_PLUNDER("Pyramid Plunder", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 7749),
    MG_TEMPLE_TREKKING("Temple Trekking", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 8014, 8270, 8256, 8782, 9038, 9294, 9550, 9806),
    MG_VOLCANIC_MINE("Volcanic Mine", EscapeCrystalNotifyRegionType.MINIGAMES, EscapeCrystalNotifyRegionDeathType.UNSAFE, 15263, 15262);

    private String regionName;
    private EscapeCrystalNotifyRegionType regionType;
    private int[] regionIds;
    private EscapeCrystalNotifyRegionDeathType regionDeathType;

    EscapeCrystalNotifyRegion(String regionName, EscapeCrystalNotifyRegionType regionType, EscapeCrystalNotifyRegionDeathType regionDeathType, int... regionIds) {
        this.regionName = regionName;
        this.regionType = regionType;
        this.regionDeathType = regionDeathType;
        this.regionIds = regionIds;
    }

    public int[] getRegionIds() {
        return this.regionIds;
    }

    public String getAreaName() {
        return this.regionName;
    }

    public EscapeCrystalNotifyRegionType getRegionType() {
        return this.regionType;
    }

    public EscapeCrystalNotifyRegionDeathType getRegionDeathType() {
        return this.regionDeathType;
    }

    public static List<Integer> getAllRegionIds() {
        return Arrays.stream(EscapeCrystalNotifyRegion.values())
                .flatMap(regionType -> Arrays.stream(regionType.getRegionIds()).boxed())
                .collect(Collectors.toList());
    }

    public static List<Integer> getRegionIdsFromTypes(List<EscapeCrystalNotifyRegionType> selectedRegionTypes) {
        return Arrays.stream(EscapeCrystalNotifyRegion.values())
                .filter(subRegionType -> selectedRegionTypes.contains(subRegionType.getRegionType()))
                .flatMap(subRegionType -> Arrays.stream(subRegionType.getRegionIds()).boxed())
                .collect(Collectors.toList());
    }
}

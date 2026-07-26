package dev.meowmayo.mmcore.utils.tracker;

public enum Events {
    // Kuudra Events
    RUN_START,
    SUPPLIES_START,
    NO_SUPPLY,
    SUPPLY_GRABBED,
    BUILD_START,
    FRESH_PROC,
    CANNON_START,
    EATEN_START,
    STUNNED_START,
    STUN_PING,
    DPS_DONE,
    SKIP_DONE,
    RUN_END_SUCCESS,
    RUN_END_FAILURE,

    // Catacombs Events
    DUNGEON_START,
    WITHER_DOOR,
    BLOOD_OPEN,
    BLOOD_DONE,
    BOSS_ENTER, // this is technically off the first boss spawn message so
    DUNGEON_END,

    DUNGEON_BOSS_DEAD,

    // per floor boss events
    // fm1

    // fm2
    SCARF_CRYPTS_DEAD,

    // fm3
    GUARDIANS_DEAD,
    PROFESSOR_1_DEAD,

    // fm4

    // fm5

    // fm6
    TERRACOTTAS_DEAD,
    GIANTS_DEAD,

    // fm7
    CRYSTAL_PLACED, // reggie
    MAXOR_DEAD,
    STORM_LIGHTNING,
    STORM_DEAD, // terminals track which phase they are on separately
    CORE_OPEN,
    GOLDOR_DEAD,
    NECRON_DEAD,
    RELICS_DOWN,

    // Expandable to more instanced things!
    // theoretiaclly this can track anything?
}
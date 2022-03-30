package components

/*
Edge is a super obstacle, that cannot be erased during cellular automata stage
Similarly road is a super empty cell
 */
enum class CellType {
    OBSTACLE,
    EDGE,
    EMPTY,
    ROAD,
    MINE,
    MOB,
    RESOURCE,
    BUILDING
}
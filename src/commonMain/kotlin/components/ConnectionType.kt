package components

/**
 * It is recommended to use regular roads only - it never gives an error
 *
 * REGULAR: will try to create a road between 2 zones, if not possible a pair of interconnected portals will appear
 * ROAD: an obligatory road. Are resolved first.
 * PORTAL: an obligatory portal. Are resolved last.
 */
enum class ConnectionType {
    ROAD,
    REGULAR,
    PORTAL
}
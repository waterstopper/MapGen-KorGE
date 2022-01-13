import com.soywiz.korge.view.Circle
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Line
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.distanceTo
import kotlin.math.*

object GeometryExtensions {
    /**
     * rotate counterclockwise
     */
    fun Line.rotateDegrees(degrees: Int): Line {
        val radians = degrees * PI / 180
        val x2Temp = x1 + (x2 - x1) * cos(radians) + (y2 - y1) * sin(radians)
        this.y2 = y1 - (x2 - x1) * sin(radians) + (y2 - y1) * cos(radians)
        this.x2 = x2Temp
        return this
    }

    /**
     * returns angle from (1, 0) counterclockwise
     */
    fun Line.getDegrees(center: Point): Int {
        return if (center == pos)
            (360 - ((atan2(y2 - y1, x2 - x1) * 180 / PI).roundToInt())) % 360
        else (360 - ((atan2(y1 - y2, x1 - x2) * 180 / PI).roundToInt())) % 360
    }

    private fun Line.hasSamePoint(other: Line): Boolean {
        for (i in points()) {
            for (j in other.points()) {
                if (i == j)
                    return true
            }
        }
        return false
    }

    fun Line.intersects(other: Line): Boolean {
        if (hasSamePoint(other))
            return false
        val (a, b) = this.points()
        val (c, d) = other.points()
        return intersect1(a.x, b.x, c.x, d.x)
                && intersect1(a.y, b.y, c.y, d.y)
                && area(a, b, c).sign * area(a, b, d).sign <= 0
                && area(c, d, a).sign * area(c, d, b).sign <= 0
    }

    private fun area(a: Point, b: Point, c: Point): Double {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
    }

    private fun intersect1(a: Double, b: Double, c: Double, d: Double): Boolean {
        var (k, l, m, n) = listOf(a, b, c, d)
        if (k > l) k = l.also { l = k }
        if (m > n) m = n.also { n = m }
        return max(k, m) <= min(l, n)
    }

    fun Circle.intersects(other: Circle): Boolean {
        return this.pos.distanceTo(other.pos) < this.radius + other.radius
    }

    /**
     * smaller metric means better position (fewer circles around)
     */
    fun Circle.getIntersectMetric(circles: Container): Double {
        var res = 0.0
        circles.forEachChild { i -> res += radius + (i as Circle).radius - this.distanceTo(i) }
        return res
    }

    /*
    return points of this line
     */
    fun Line.points(): List<Point> {
        return listOf(Point(this.x1, this.y1), Point(this.x2, this.y2))
    }
}
package components

import com.soywiz.korge.render.testRenderContext
import external.Template

class Connection(private val tConnection: Template.TemplateConnection, val z1: Zone, val z2: Zone) {
    var resolved: Boolean = false
    val guardLevel: Int
        get() = tConnection.guardLevel
}
package com.github.chriskn.structurizrextension.plantuml.includes

import com.github.chriskn.structurizrextension.plantuml.AWS_ICON_COMMONS
import com.github.chriskn.structurizrextension.plantuml.AWS_ICON_URL
import com.github.chriskn.structurizrextension.plantuml.IconRegistry
import com.structurizr.io.plantuml.PlantUMLWriter
import com.structurizr.view.View
import java.net.URI

/**
 * Includes sprites and libraries using urls.
 *
 * Always the latest versions are used with this handler but an internet connection is required to generate diagrams.
 * @see https://github.com/plantuml-stdlib/C4-PlantUML#including-the-c4-plantuml-library
 */
class UrlIncludeHandler : AbstractIncludeHandler() {

    companion object {
        private const val C4_PLANT_UML_STDLIB_URL = "https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master"
    }

    override fun addIncludes(view: View, writer: PlantUMLWriter) {
        val spriteIncludesForElements = collectUsedIcons(view)
            .mapNotNull { IconRegistry.iconUrlFor(it) }
            .toMutableList()
        if (spriteIncludesForElements.any { it.startsWith(AWS_ICON_URL) }) {
            spriteIncludesForElements.add(0, AWS_ICON_COMMONS)
        }
        spriteIncludesForElements.forEach { writer.addIncludeURL(URI(it)) }
        val c4PumlIncludeURI = URI("$C4_PLANT_UML_STDLIB_URL/${c4IncludesForView[view.javaClass]}.puml")
        writer.addIncludeURL(c4PumlIncludeURI)
    }
}

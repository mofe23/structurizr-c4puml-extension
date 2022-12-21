package com.github.chriskn.structurizrextension.plantuml.includes

import com.github.chriskn.structurizrextension.plantuml.AWS_ICON_URL
import com.github.chriskn.structurizrextension.plantuml.IconRegistry
import com.structurizr.io.plantuml.PlantUMLWriter
import com.structurizr.view.View

/**
 * Includes sprites and libraries using files delivered with the PlantUML.
 *
 * No internet connection is needed when using this handler.
 * @see https://github.com/plantuml-stdlib/C4-PlantUML#including-the-c4-plantuml-library
 */
class FileIncludeHandler : AbstractIncludeHandler() {

    companion object {
        private const val C4_PLANT_UML_FILE_NAME = "<C4/%s>"
    }

    override fun addIncludes(view: View, writer: PlantUMLWriter) {
        val iconsForElements = collectUsedIcons(view)
        val iconUrls = iconsForElements
            .mapNotNull { iconName -> IconRegistry.iconUrlFor(iconName) }
            .sorted()
        if (iconUrls.any { it.startsWith(AWS_ICON_URL) }) {
            writer.addIncludeFile("<awslib/AWSCommon>")
        }
        iconUrls
            .filter { it.startsWith(AWS_ICON_URL) }
            .forEach { url ->
                val parts = url.split("/")
                val category = parts[parts.size - 2]
                val name = parts.last()
                writer.addIncludeFile("<awslib/$category/${name.replace(".puml", "")}>")
            }
        iconUrls
            .filter { !it.startsWith(AWS_ICON_URL) }
            .forEach {
                writer.addIncludeFile("<logos/${it.split("/").last()}>")
            }
        val c4PumlLibFile = String.format(C4_PLANT_UML_FILE_NAME, c4IncludesForView[view.javaClass])
        writer.addIncludeFile(c4PumlLibFile)
    }
}

package com.github.chriskn.structurizrextension

import com.github.chriskn.structurizrextension.plantuml.includes.AbstractIncludeHandler
import com.github.chriskn.structurizrextension.plantuml.includes.UrlIncludeHandler
import com.structurizr.Workspace
import com.structurizr.io.plantuml.ExtendedC4PlantUmlWriter
import com.structurizr.io.plantuml.PlantUMLDiagram
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Writes all views of the workspace as C4PlantUML diagrams to the given output folder.
 *
 * Diagrams files are named after their diagram key
 *
 * @param outputFolder      target folder for diagrams
 * @param includeHandler    handler which adds includes to the diagram based on urls or files. Default is [UrlIncludeHandler].
 * @throws IOException if writing fails.
 */
fun Workspace.writeDiagrams(outputFolder: File, includeHandler: AbstractIncludeHandler = UrlIncludeHandler()) {
    outputFolder.mkdirs()
    val plantUMLWriter = ExtendedC4PlantUmlWriter(includeHandler)
    val diagrams = plantUMLWriter.toPlantUMLDiagrams(this)
    for (diagram in diagrams) {
        writeDiagram(diagram, File(outputFolder, "${diagram.key}.puml"))
    }
}

private fun writeDiagram(diagram: PlantUMLDiagram, out: File) {
    val writer = BufferedWriter(FileWriter(out))
    writer.write(diagram.definition)
    writer.close()
}

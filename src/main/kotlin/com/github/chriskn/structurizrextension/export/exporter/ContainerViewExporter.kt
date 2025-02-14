package com.github.chriskn.structurizrextension.export.exporter

import com.github.chriskn.structurizrextension.export.createC4Diagram
import com.github.chriskn.structurizrextension.export.writer.BoundaryWriter
import com.github.chriskn.structurizrextension.export.writer.ElementWriter
import com.github.chriskn.structurizrextension.export.writer.FooterWriter
import com.github.chriskn.structurizrextension.export.writer.HeaderWriter
import com.github.chriskn.structurizrextension.export.writer.RelationshipWriter
import com.structurizr.export.Diagram
import com.structurizr.export.IndentingWriter
import com.structurizr.model.Container
import com.structurizr.model.SoftwareSystem
import com.structurizr.view.ContainerView
import com.structurizr.view.ElementView
import com.structurizr.view.ModelView

class ContainerViewExporter(
    private val boundaryWriter: BoundaryWriter,
    private val footerWriter: FooterWriter,
    private val headerWriter: HeaderWriter,
    private val elementWriter: ElementWriter,
    private val relationshipWriter: RelationshipWriter
) {

    internal fun exportContainerView(view: ContainerView): Diagram {
        val writer = IndentingWriter()
        headerWriter.writeHeader(view, writer)

        var elementsWritten = false
        for (elementView in view.elements) {
            if (elementView.element !is Container) {
                elementWriter.writeElement(view, elementView.element, writer)
                elementsWritten = true
            }
        }
        if (elementsWritten) {
            writer.writeLine()
        }

        val softwareSystems: List<SoftwareSystem> = getBoundarySoftwareSystems(view)
        for (softwareSystem in softwareSystems) {
            val showSoftwareSystemBoundary =
                softwareSystem == view.softwareSystem || view.externalSoftwareSystemBoundariesVisible
            if (showSoftwareSystemBoundary) {
                boundaryWriter.startSoftwareSystemBoundary(softwareSystem, writer)
            }
            for (elementView in view.elements) {
                if (elementView.element.parent === softwareSystem) {
                    elementWriter.writeElement(view, elementView.element, writer)
                }
            }
            if (showSoftwareSystemBoundary) {
                boundaryWriter.endSoftwareSystemBoundary(writer)
            } else {
                writer.writeLine()
            }
        }

        relationshipWriter.writeRelationships(view, writer)

        footerWriter.writeFooter(view, writer)

        return createC4Diagram(view, writer.toString())
    }

    private fun getBoundarySoftwareSystems(view: ModelView): List<SoftwareSystem> {
        val softwareSystems = view.elements
            .map { obj: ElementView -> obj.element }
            .filterIsInstance<Container>()
            .map { it.softwareSystem }
            .toSet()
        return softwareSystems.sortedBy { it.id }
    }
}

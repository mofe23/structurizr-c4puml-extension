package com.github.chriskn.structurizrextension.plantuml.includes

import com.github.chriskn.structurizrextension.model.icon
import com.structurizr.io.plantuml.PlantUMLWriter
import com.structurizr.model.DeploymentNode
import com.structurizr.model.ModelItem
import com.structurizr.view.ComponentView
import com.structurizr.view.ContainerView
import com.structurizr.view.DeploymentView
import com.structurizr.view.DynamicView
import com.structurizr.view.SystemContextView
import com.structurizr.view.SystemLandscapeView
import com.structurizr.view.View
import java.util.SortedSet

abstract class AbstractIncludeHandler {

    abstract fun addIncludes(view: View, writer: PlantUMLWriter)

    protected val c4IncludesForView = mapOf(
        DynamicView::class.java to "C4_Dynamic",
        DeploymentView::class.java to "C4_Deployment",
        ComponentView::class.java to "C4_Component",
        ContainerView::class.java to "C4_Container",
        SystemLandscapeView::class.java to "C4_Context",
        SystemContextView::class.java to "C4_Context"
    )

    protected fun collectUsedIcons(view: View): SortedSet<String> {
        val elements: MutableSet<ModelItem> = view.elements.map { it.element }.toMutableSet()
        if (view is DeploymentView) {
            val children = elements
                .filterIsInstance<DeploymentNode>()
                .flatMap { collectDeploymentNodeChildElements(it, elements) }
            elements.addAll(children)
        }
        elements += view.relationships.map { it.relationship }
        return elements.asSequence().mapNotNull { it.icon }.toSortedSet()
    }
    private fun collectDeploymentNodeChildElements(
        deploymentNode: DeploymentNode,
        elements: MutableSet<ModelItem>
    ): MutableSet<ModelItem> {
        elements.addAll(deploymentNode.infrastructureNodes)
        elements.addAll(deploymentNode.softwareSystemInstances.map { it.softwareSystem })
        elements.addAll(deploymentNode.containerInstances.map { it.container })
        deploymentNode.children.forEach { collectDeploymentNodeChildElements(it, elements) }
        return elements
    }
}

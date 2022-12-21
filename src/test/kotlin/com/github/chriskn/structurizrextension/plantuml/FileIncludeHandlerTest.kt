package com.github.chriskn.structurizrextension.plantuml

import com.github.chriskn.structurizrextension.assertExpectedDiagramWasWrittenForView
import com.github.chriskn.structurizrextension.model.C4Properties
import com.github.chriskn.structurizrextension.model.C4Type
import com.github.chriskn.structurizrextension.model.Dependency
import com.github.chriskn.structurizrextension.model.c4Type
import com.github.chriskn.structurizrextension.model.container
import com.github.chriskn.structurizrextension.model.deploymentNode
import com.github.chriskn.structurizrextension.model.icon
import com.github.chriskn.structurizrextension.model.infrastructureNode
import com.github.chriskn.structurizrextension.model.person
import com.github.chriskn.structurizrextension.model.softwareSystem
import com.github.chriskn.structurizrextension.plantuml.includes.FileIncludeHandler
import com.github.chriskn.structurizrextension.view.containerView
import com.github.chriskn.structurizrextension.view.deploymentView
import com.structurizr.Workspace
import com.structurizr.model.Container
import com.structurizr.model.InteractionStyle
import com.structurizr.model.Location
import org.junit.jupiter.api.Test

class FileIncludeHandlerTest {

    @Test
    fun `produces expected deployment diagram`() {
        val diagramKey = "Deployment_FileIncludes"
        val workspace = Workspace(
            "My deployment",
            "An example deployment description"
        )
        val model = workspace.model
        val views = workspace.views

        val mySystem = model.addSoftwareSystem(
            Location.Internal,
            "System container",
            "Example System"
        )
        val iosApp = model.softwareSystem(
            location = Location.External,
            name = "iOS App",
            description = "iOS Application"
        )
        val webApplication: Container = mySystem.container(
            "Web Application",
            "Spring Boot web application",
            technology = "Java and Spring MVC",
            icon = "springboot"
        )
        val database: Container = mySystem.container(
            "Database",
            "Stores data",
            technology = "PostgreSql",
            icon = "postgresql",
            c4Type = C4Type.DATABASE,
            properties = C4Properties(values = listOf(listOf("region", "eu-central-1"))),
            usedBy = listOf(Dependency(webApplication, "stores data in", "JDBC"))
        )
        val failoverDatabase: Container = mySystem.container(
            "Failover Database",
            database.description,
            technology = database.technology,
            icon = database.icon,
            c4Type = database.c4Type,
            properties = C4Properties(values = listOf(listOf("region", "eu-west-1"))),
            usedBy = listOf(Dependency(database, "replicates data to"))
        )
        val aws = model.deploymentNode(
            "AWS",
            "Production AWS environment",
            icon = "aws",
            properties = C4Properties(
                header = listOf("Property", "Value", "Description"),
                values = listOf(
                    listOf("Property1", "Value1", "Description1"),
                    listOf("Property2", "Value2", "Description2"),
                )
            )
        )
        aws.deploymentNode(
            "AWS RDS",
            icon = "rds",
            hostsContainers = listOf(failoverDatabase, database)
        )
        val eks = aws.deploymentNode(
            "EKS cluster",
            icon = "awsEKSCloud",
        )

        val webAppPod = eks.deploymentNode(
            "my.web.app",
            "Web App POD"
        ).deploymentNode(
            "Web App container",
            icon = "docker",
            hostsContainers = listOf(webApplication)
        )
        val jaegerSidecar = webAppPod.infrastructureNode(
            "Jaeger Sidecar",
            "Jaeger sidecar sending Traces"
        )
        model.deploymentNode(
            "Another AWS Account",
            icon = "aws"
        ).deploymentNode(
            "Jaeger Container",
            usedBy = listOf(
                Dependency(
                    jaegerSidecar,
                    "writes traces to",
                    interactionStyle = InteractionStyle.Asynchronous,
                    icon = "kafka",
                    link = "https://www.jaegertracing.io/",
                    properties = C4Properties(
                        header = listOf("key", "value"),
                        values = listOf(listOf("ip", "10.234.12.13"))
                    )
                )
            )
        ).infrastructureNode("Jaeger")
        val appleDevice = model.deploymentNode(
            "Apple Device",
            icon = "apple",
            hostsSystems = listOf(iosApp)
        )

        val loadBalancer = eks.infrastructureNode(
            name = "Load Balancer",
            description = "Nginx Load Balancer",
            technology = "nginx",
            icon = "nginx",
            link = "https://www.google.de",
            uses = listOf(Dependency(webAppPod, "forwards requests to")),
            usedBy = listOf(Dependency(appleDevice, "requests data from")),
            properties = C4Properties(
                header = listOf("Property", "value"),
                values = listOf(listOf("IP", "10.234.234.132"))
            )
        )

        val deploymentView =
            views.deploymentView(
                mySystem,
                diagramKey,
                "A deployment diagram showing the environment.",
                C4PlantUmlLayout(
                    nodeSep = 50,
                    rankSep = 50,
                    layout = Layout.LeftToRight,
                    dependencyConfigurations =
                    listOf(
                        DependencyConfiguration(
                            filter = {
                                it.source == loadBalancer || it.destination.name == failoverDatabase.name
                            },
                            direction = Direction.Right
                        )
                    )
                )
            )
        deploymentView.addDefaultElements()
        assertExpectedDiagramWasWrittenForView(workspace, diagramKey, FileIncludeHandler())
    }

    @Test
    fun `produces expected container diagram`() {
        val diagramKey = "ContainerWithBoundary_FileIncludes"
        val workspace = Workspace("My Workspace", "")
        val model = workspace.model
        val properties = C4Properties(values = listOf(listOf("prop 1", "value 1")))
        val softwareSystem = model.softwareSystem(
            name = "My Software System",
            description = "system description",
            link = "https://www.google.de"
        )
        val backendApplication = softwareSystem.container(
            name = "Backend App",
            description = "some backend app",
            technology = "Kotlin",
            tags = listOf("Tag2"),
            icon = "docker",
            link = "https://www.google.de",
            properties = properties
        )
        val app = softwareSystem.container(
            name = "App",
            description = "android app",
            technology = "Android",
            icon = "android",
        )
        val database = softwareSystem.container(
            name = "Database",
            description = "some database",
            c4Type = C4Type.DATABASE,
            technology = "postgres",
            icon = "postgresql",
            usedBy = listOf(Dependency(backendApplication, "CRUD", "JPA"))
        )
        val maintainer = model.person(
            name = "Maintainer",
            description = "some employee",
            location = Location.Internal,
            link = "https://www.google.de",
            uses = listOf(
                Dependency(backendApplication, "Admin UI", "REST")
            ),
            properties = properties
        )
        val broker = model.softwareSystem(
            name = "Broker",
            description = "Message Broker",
            location = Location.External,
            c4Type = C4Type.QUEUE,
            icon = "kafka",
        )
        val topic = broker.container(
            "my.topic",
            "external topic",
            c4Type = C4Type.QUEUE,
            icon = "kafka",
            usedBy = listOf(
                Dependency(backendApplication, "reads topic", "Avro", interactionStyle = InteractionStyle.Asynchronous)
            )
        )
        val graphql = model.softwareSystem(
            name = "GraphQL",
            description = "Federated GraphQL",
            location = Location.External,
            icon = "graphql"
        )
        val internalSchema = graphql.container(
            name = "Internal Schema",
            description = "Schema provided by our app",
            location = Location.Internal,
            usedBy = listOf(
                Dependency(backendApplication, "provides subgraph to"),
                Dependency(app, "reuqest data using", "GraphQL", icon = "graphql", link = "https://graphql.org/")
            )
        )
        val externalSchema = graphql.container(
            name = "External Schema",
            description = "Schema provided by another team",
            uses = listOf(Dependency(internalSchema, "extends schema"))
        )
        val androidUser = model.person(
            name = "Android User",
            description = "some Android user",
            location = Location.External,
            icon = "android",
            uses = listOf(Dependency(app, "uses app"))
        )

        val containerView = workspace.views.containerView(
            softwareSystem,
            diagramKey,
            "Example container view",
            C4PlantUmlLayout(
                legend = Legend.ShowLegend,
                layout = Layout.TopDown,
                lineType = LineType.Ortho,
                nodeSep = 100,
                rankSep = 130,
                dependencyConfigurations = listOf(
                    DependencyConfiguration(filter = { it.destination == database }, direction = Direction.Right),
                    DependencyConfiguration(filter = { it.destination == topic }, direction = Direction.Up)
                )
            )
        )
        containerView.addAllContainers()
        containerView.externalSoftwareSystemBoundariesVisible = true
        containerView.add(topic)
        containerView.add(internalSchema)
        containerView.add(externalSchema)

        containerView.addDependentSoftwareSystems()
        containerView.addAllPeople()

        assertExpectedDiagramWasWrittenForView(workspace, diagramKey, FileIncludeHandler())
    }
}

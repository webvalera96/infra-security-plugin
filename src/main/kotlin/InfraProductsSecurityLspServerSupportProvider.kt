package ru.webvalera96

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor

internal class InfraProductsSecurityLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
        if (file.extension == "foo") {
            serverStarter.ensureServerStarted(InfraProductsSecurityLspServerDescriptor(project))
        }
    }
}

private class InfraProductsSecurityLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Foo") {
    override fun isSupportedFile(file: VirtualFile) = file.extension == "foo"
    override fun createCommandLine() = GeneralCommandLine("foo", "--stdio")
}


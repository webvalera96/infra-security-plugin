package ru.webvalera96

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import com.intellij.openapi.diagnostic.Logger

import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor

internal class FileUtils {
    companion object {
        fun isDockerFile(file: VirtualFile): Boolean {
            return file.name.equals("Dockerfile", ignoreCase = true) ||
                    file.name.equals("Containerfile", ignoreCase = true);
        }
    }
}

internal class InfraProductsSecurityLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        // True, if Docker
        if (FileUtils.isDockerFile(file)) {
            serverStarter.ensureServerStarted(InfraProductsSecurityLspServerDescriptor(project))
        }
    }
}

private class InfraProductsSecurityLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "ISP") {
    override fun isSupportedFile(file: VirtualFile) = FileUtils.isDockerFile(file)
    override fun createCommandLine(): GeneralCommandLine {
        val ispPath = findBinary()
        return GeneralCommandLine(ispPath, "--stdio")
    }

    private val LOG = Logger.getInstance(InfraProductsSecurityLspServerDescriptor::class.java)
    // Find path to binary, where lsp server exist
    private fun findBinary(): String {
        LOG.info("IPS_PATH env: ${System.getenv("IPS_PATH")}")
        // If IPS_PATH defined
        System.getenv("IPS_PATH")?.let {
//            path -> if (File(path).exists()) return path
            path -> return path
        }

        // set ips binary
        return "ips"
    }
}


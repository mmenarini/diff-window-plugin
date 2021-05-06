package edu.ucsd.mmenarini.getty

import edu.ucsd.ClassMethod
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

fun cloneRepo(destPath: Path, srcPath: Path, hash: String) {
    Git.cloneRepository()
            .setURI( "file://${srcPath.toAbsolutePath()}" )
            .setDirectory(destPath.toFile())
            .setCloneAllBranches(true)
            .call()
            .use {
                it
                        .checkout()
                        .setName(hash)
                        .call()
            }
}

fun createSignature(cm : ClassMethod):String {
    return "<${cm.qualifiedClassName}: " +
            "${cm.returnType} " +
            "${cm.methodName}" +
            "(${cm.parameterTypes.joinToString(",")})>"
}

const val strPluginRef = "includeBuild '../InvariantsPluginGradle'"
fun cloneGitHead(repoDir: Path):Path {
    val repoBuilder = FileRepositoryBuilder()
    try {
        val repository: Repository = repoBuilder
                .setMustExist(true)
                .setWorkTree(repoDir.toFile())
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()

        val workDir =
            repoDir.parent.resolve(repoDir.fileName.toString() + ".___work")
        Files.createDirectories(workDir)

        val hash0 = repository.resolve("HEAD").name()
        val hash0Path = workDir.resolve("git_$hash0")
        if (Files.notExists(hash0Path)) {
            cloneRepo(hash0Path, repoDir, hash0)
            //TMP Fix for plugin not in repo
            val settingsFile = hash0Path.resolve("settings.gradle").toFile()
            val inLines = settingsFile.bufferedReader().readLines()
            if (inLines.contains(strPluginRef)) {
                val outArr = inLines.toTypedArray()
                outArr[inLines.indexOf(strPluginRef)] = "includeBuild '../../InvariantsPluginGradle'"
                settingsFile.bufferedWriter().use { it.write(outArr.joinToString(System.lineSeparator())) }
            }
            //buildRepo(hash0Path, hash0)
        }
        repository.close()
        return hash0Path
    } catch (ex: IOException) {
        throw Exception("Defined Git Directory $repoDir does not contain a valid git repository")
    } catch (ex: Throwable) {
        throw Exception(ex.message)
    }
}
package edu.ucsd.mmenarini.getty

import edu.ucsd.ClassMethod
import org.apache.commons.cli.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.measureNanoTime


fun buildRepo(path: Path, hash: String) {
    val builder = ProcessBuilder()
    if(System.getProperty("os.name")
            .toLowerCase().startsWith("windows")){
        builder.command(listOf("gradlew.bat", "assemble", "testClasses"))
        //builder.command(listOf("gradlew.bat", "build", "testClasses", "-x", "test"))
    } else {
        //builder.command(listOf("/bin/sh", "gradlew", "assemble", "testClasses"))
        //builder.command(listOf("/bin/sh", "gradlew", "build", "testClasses", "-x", "test"))
        builder.command(listOf("/bin/sh", "-c", "exec /bin/sh gradlew build testClasses -x test"))
    }
    builder.directory(path.toFile())
    val process = builder.start()
    val br = BufferedReader(InputStreamReader(process.inputStream))
    val bw = BufferedWriter(FileWriter(path.parent.resolve("gradle_$hash").toFile()))
    var line: String? =""
    while(line!=null){
        line=br.readLine()
        if (line!=null)
            bw.write(line)
    }
    bw.close()
    val exitCode = process.waitFor()
    if (exitCode!=0){
        throw Exception("Could not restore gradle")
    }

}

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


fun main(args: Array<String>) {
    val execTime = measureNanoTime {
        val options = Options()
        val operation2Hash = Option.builder("2")
                .longOpt("two-hashes")
                .desc("""You need to specify two commit hashes
               |Getty will extract invariant differences for these 2 commits
               |""".trimMargin())
                .hasArgs().numberOfArgs(2)
                .type(String::class.java)
                .build()
        val operation1Hash = Option.builder("1")
                .longOpt("one-hash")
                .desc("""You need to specify one commit hash
               |Getty will extract invariant differences between the current code state the given hash
               |If the selected hash is HEAD you can use the 0 option (no-hash)
               |""".trimMargin())
                .hasArg()
                .type(String::class.java)
                .build()
        val operationNoHash = Option.builder("0")
                .longOpt("no-hash")
                .desc("""Getty will extract invariant differences between the current code state the HEAD
               |""".trimMargin())
                .hasArg()
                .type(String::class.java)
                .build()
        val typeOfOperation = OptionGroup()
                .addOption(operation2Hash)
                .addOption(operation1Hash)
                .addOption(operationNoHash)
        typeOfOperation.setSelected(operation2Hash)
        typeOfOperation.isRequired = true

        val focusSetFile = Option.builder("F")
                .longOpt("focus-file")
                .desc("""Specify the file that contains the list of methods to compute the invariant diff for""")
                .hasArg()
                .type(String::class.java)
                .build()
        val focusSet = Option.builder("f")
                .longOpt("focus-method")
                .desc("""Specify the fully qualified name of the methods to compute the invariant diff for
               |If more than one method they need to be comma separated and without spaces""".trimMargin())
                .hasArg()
                .type(String::class.java)
                .build()
        val focusOptionGroup = OptionGroup()
                .addOption(focusSetFile)
                .addOption(focusSet)

        val impactAnalysis = Option.builder("i")
                .longOpt("impact-analysis")
                .desc("""If specified will run test impact analysis using old/new tests""")
                .build()

        val uiHtml = Option.builder("u")
                .longOpt("html-ui")
                .desc("""If specified will generate the html UI for code review""")
                .build()

        val repoDirectory = Option.builder("g")
                .longOpt("git-dir")
                .desc("""Can specify the directory where the main git repository is located
                |If not specified the current directory is used
            """.trimMargin())
                .hasArg()
                .type(String::class.java)
                .build()

        val workDirectory = Option.builder("w")
                .longOpt("work-dir")
                .desc("""Can specify the work directory where different versions of the repo are checked out,
                |Daikon is executed, and data is cached
                |If not specified a the dierectory ../<current dir name>.___work is created and used
            """.trimMargin())
                .hasArg()
                .type(String::class.java)
                .build()


        options
                .addOptionGroup(typeOfOperation)
                .addOptionGroup(focusOptionGroup)
                .addOption(impactAnalysis)
                .addOption(uiHtml)
                .addOption(repoDirectory)
                .addOption(workDirectory)

        val parser = DefaultParser()
        val cmd = parser.parse(options, args)

        val doImpactAnalysis = cmd.hasOption('i')
        val doUiHtml = cmd.hasOption('u')

        val focusMethods: MutableSet<String> = mutableSetOf()
        if (cmd.hasOption('F'))
            focusMethods.addAll(File(cmd.getOptionValue('F')).readLines())
        if (cmd.hasOption('f'))
            focusMethods.addAll(cmd.getOptionValue('f').split(','))

        val repoDir: Path

        if (cmd.hasOption('g')) {
            repoDir = Paths.get(cmd.getOptionValue('g')).toAbsolutePath()
        } else {
            repoDir = Paths.get("").toAbsolutePath()
        }

        if (Files.notExists(repoDir))
            throw ParseException("Git Directory $repoDir does not exist")

        if (!Files.isDirectory(repoDir))
            throw ParseException("Defined Git Directory $repoDir is not a directory")

        buildRepo(repoDir, cmd)
    }
    System.out.println("Execution Time ${execTime/1000000000.0} seconds")
}

fun buildRepo(repoDir: Path, cmd: CommandLine) {
    val repoBuilder = FileRepositoryBuilder()

    try {
        val repository: Repository = repoBuilder
                .setMustExist(true)
                .setWorkTree(repoDir.toFile())
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()

        var workDir: Path

        workDir = if (cmd.hasOption('w')) {
            Paths.get(cmd.getOptionValue('w')).toAbsolutePath()
        } else {
            repoDir.parent.resolve(repoDir.fileName.toString() + ".___work")
        }

        if (Files.notExists(workDir))
            Files.createDirectory(workDir)

        val dir0: File
        val dir1: File

        when {
            cmd.hasOption('2') -> {
                //We specify 2 hashes
                val hash0 = repository.resolve(cmd.getOptionValues('2')[0]).name()
                val hash1 = repository.resolve(cmd.getOptionValues('2')[1]).name()
                val hash0Path = workDir.resolve("git_$hash0")
                val hash1Path = workDir.resolve("git_$hash1")
                if (Files.notExists(hash0Path))
                    cloneRepo(hash0Path, repoDir, hash0)
                if (Files.notExists(hash1Path))
                    cloneRepo(hash1Path, repoDir, hash1)
                dir0 = hash0Path.toFile()
                dir1 = hash1Path.toFile()
            }
            cmd.hasOption('1') -> {
                //We specify 1 hash
                val hash = repository.resolve(cmd.getOptionValue('1')).name()
                val hashPath = workDir.resolve("git_$hash")
                if (Files.notExists(hashPath))
                    cloneRepo(hashPath, repoDir, hash)
                dir0 = hashPath.toFile()
                dir1 = repoDir.toFile()
            }
            else -> {
                //We specify no hash, assume HEAD
                val hash = repository.resolve("refs/heads/master").name()
                val hashPath = workDir.resolve("git_$hash")
                if (Files.notExists(hashPath))
                    cloneRepo(hashPath, repoDir, hash)
                dir0 = hashPath.toFile()
                dir1 = repoDir.toFile()
            }
        }
        repository.close()
    } catch (ex: IOException) {
        throw ParseException("Defined Git Directory $repoDir does not contain a valid git repository")
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
        throw ParseException("Defined Git Directory $repoDir does not contain a valid git repository")
    } catch (ex: Throwable) {
        throw ParseException(ex.message)
    }
}
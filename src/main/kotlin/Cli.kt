import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

open class Cli {

    protected var currentDirPath: String = System.getProperty("user.dir")

    open fun handle(command: String) {
        when {
            EXIT in command -> handleExit()
            LIST_OF_FILES in command -> handleListOfFiles()
            CHANGE_DIRECTORY in command -> handleChangeDirectory(command)
            CREATE_FILE in command -> handleCreate(command, false)
            CREATE_DIRECTORY in command -> handleCreate(command, true)
            DELETE in command -> handleDeleteFile(command)
            else -> handleUnknownCommand(command)
        }
    }

    private fun handleDeleteFile(command: String) {
        val args = listOf(*command.split(" ").toTypedArray())
        if (args.size < 2) {
            println("touch requires at least 1 argument")
            return
        }
        val fileName = args[1]
        val filePath = currentDirPath + File.separator + fileName
        val file = File(filePath)
        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                println("Directory\\File \"$fileName\" successfully deleted")
            } else {
                println("Error")
            }
        } else {
            println("Directory\\File \"$fileName\" does not exists")
        }
    }

    private fun handleCreate(command: String, isDirectory: Boolean) {
        val args = listOf(*command.split(" ").toTypedArray())
        if (args.size < 2) {
            println("${args[0]} requires at least 1 argument")
            return
        }
        val fileName = args[1]
        val currentDir = File(currentDirPath)
        val file = File(currentDir, fileName)
        if (isDirectory) {
            handleCreateDirectory(file, fileName)
        } else {
            handleCreateFile(file, fileName)
        }
    }

    private fun handleCreateFile(file: File, fileName: String) {
        val created = try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        if (created) {
            println("File \"$fileName\" successfully created")
        } else {
            println("Error")
        }
    }

    private fun handleCreateDirectory(file: File, fileName: String) {
        val created = file.mkdir()
        if (created) {
            println("Directory\\File \"$fileName\" successfully created")
        } else {
            println("Error")
        }
    }

    private fun handleChangeDirectory(command: String) {
        val args = listOf(*command.split(" ").toTypedArray())
        if (args.size < 2) {
            println("cd requires at least 1 argument")
            return
        }
        val dirName = args[1]
        if (dirName == "..") {
            val dir = File(currentDirPath)
            if (currentDirPath.length > 1) {
                currentDirPath =
                    if (currentDirPath.lastIndexOf(File.separatorChar) == currentDirPath.indexOf(File.separatorChar)) {
                        currentDirPath.replace(dir.name, "")
                    } else {
                        currentDirPath.replace(File.separatorChar.toString() + dir.name, "")
                    }
            }
        } else {
            val dirPath: String = if (currentDirPath.lastIndexOf(File.separatorChar) == currentDirPath.length - 1) {
                currentDirPath + dirName
            } else {
                currentDirPath + File.separator + dirName
            }
            val dir = File(dirPath)
            if (dir.exists()) {
                if (dir.isDirectory) {
                    currentDirPath = dirPath
                }
            } else {
                println("Dir \"$dirName\" does not exists")
            }
        }
        println("Current Dir: $currentDirPath")
    }

    private fun handleExit() {
        exitProcess(0)
    }

    private fun handleListOfFiles() {
        val currentDirFile = File(currentDirPath)
        val listFiles = currentDirFile.listFiles()
        if (!currentDirFile.isDirectory) {
            return
        }
        if (listFiles == null) {
            return
        }
        System.out.printf(LS_PATTERN, "Name", "Directory")
        System.out.printf(LS_PATTERN, "----", "---------")
        for (file in listFiles) {
            System.out.printf(LS_PATTERN, file.name, file.isDirectory)
        }
        File(currentDirPath)
    }

    private fun handleUnknownCommand(command: String) {
        println("Unknown command: $command")
    }

    companion object {
        const val EXIT = "exit"
        const val LIST_OF_FILES = "ls"
        const val CHANGE_DIRECTORY = "cd"
        const val CREATE_FILE = "touch"
        const val CREATE_DIRECTORY = "dir"
        const val DELETE = "rm"
        private const val LS_PATTERN = "%-20s %-10s\n"
    }
}
package de.mmuth

import com.github.ajalt.clikt.core.CliktError
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.PlainTextMessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.KProperty
import kotlin.time.measureTime

const val INPUT_FILE_BUILD_DIR = "build/classes/input"
const val AGAINST_INPUT_FILE_BUILD_DIR = "build/classes/against-input"

class ExternalClassLoader(
    private val inputFilePath: String,
    private val againstInputFilePath: String,
    private val mainClassName: String? = null
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun load(): Pair<KotlinValidatableDataClassDescription, KotlinValidatableDataClassDescription> {
        val inputClassLoader = compileKotlinClassFromRawFile(inputFilePath, INPUT_FILE_BUILD_DIR)
        val inputClass = loadMainClass(inputFilePath, inputClassLoader, mainClassName)

        val againstInputClassLoader = compileKotlinClassFromRawFile(againstInputFilePath, AGAINST_INPUT_FILE_BUILD_DIR)
        val againstInputClass = loadMainClass(againstInputFilePath, againstInputClassLoader, mainClassName)
        return Pair(inputClass, againstInputClass)
    }

    private fun loadMainClass(filePath: String, classLoader: ClassLoader, specificMainClassName: String?): KotlinValidatableDataClassDescription {
        val mainClassName = specificMainClassName ?: File(filePath).nameWithoutExtension
        val packageName = File(filePath).readText().substringAfter("package ").substringBefore("\n").trim()
        logger.info("Loading class $packageName.$mainClassName")

        val loadedClass = loadClassFile("$packageName.$mainClassName", classLoader)
        if (loadedClass !is KotlinValidatableDataClassDescription)
            throw CliktError("Sorry, on top level, only data classes are supported!")
        return loadedClass
    }

    private fun loadClassFile(fullyQualifiedClassName: String, classLoader: ClassLoader): KotlinValidatableTypeReference {
        val clazz = classLoader.loadClass(fullyQualifiedClassName)

        val kClass = clazz.kotlin
        val className = kClass.simpleName!! // we can't support anonymous classes in our whole use case
        val classPackage = clazz.`package`.name

        if (kClass.isData) {
            val properties = kClass.members.filter { it is KProperty }.map { KotlinMemberDescription(it.name, it.returnType.toString()) }

            val referencedClassesToBeLoaded = properties.filter { it.type.contains(classPackage) }.map {
                // those could be plain references or wrapped in a Collection or even multiple references for Maps
                Regex(pattern = "(${classPackage}[\\w.]+)").findAll(it.type).map { it.value }.toSet()
            }.toSet().flatten()

            val loadedReferencedClasses = referencedClassesToBeLoaded.map { loadClassFile(it, classLoader) }.toSet()
            return KotlinValidatableDataClassDescription(className, classPackage.toString(), properties, loadedReferencedClasses)
        } else if (clazz.isEnum) {
            val enumValues = clazz.enumConstants.map { it.toString() }
            return KotlinEnumDescripton(className, enumValues)
        } else {
            throw CliktError("SORRY unsupported class in file found!: ${kClass.qualifiedName}. Currently only data classes and enums are supported!")
        }
    }

    /**
     * @return a classloader to get the compiled class
     */
    private fun compileKotlinClassFromRawFile(filePath: String, targetPath: String): ClassLoader {
        val file = File(filePath)
        val outputDir = File(targetPath)
        outputDir.mkdirs()

        val compiler = K2JVMCompiler()
        val args = K2JVMCompilerArguments().apply {
            freeArgs = listOf(file.absolutePath)
            classpath = System.getProperty("java.class.path")
            destination = outputDir.absolutePath
            noStdlib = true
        }

        val duration = measureTime {
            val exitCode = compiler.exec(PrintingMessageCollector(System.err, PlainTextMessageRenderer.PLAIN_FULL_PATHS, false), Services.EMPTY, args)
            compiler.exec(PrintingMessageCollector(System.err, PlainTextMessageRenderer.PLAIN_FULL_PATHS, false), Services.EMPTY, args)
            if (exitCode.code != 0) {
                throw CliktError("Compilation failed with exit code: $exitCode")
            }
        }
        logger.info("Compilation took ${duration.inWholeMilliseconds} ms")

        return URLClassLoader(arrayOf(outputDir.toURI().toURL()))
    }

}

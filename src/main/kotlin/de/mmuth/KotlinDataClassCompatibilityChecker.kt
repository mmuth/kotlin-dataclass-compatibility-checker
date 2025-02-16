package de.mmuth

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import org.slf4j.LoggerFactory

open class KotlinDataClassCompatibilityChecker : CliktCommand() {

    override val printHelpOnEmptyArgs: Boolean = true

    val inputFilePath by option(
        "--input",
        help = "file path to the base file to be validated"
    ).prompt("Please provide the path to the input file (`producer`)")

    val againstInputFilePath by option(
        "--against-input",
        help = "other file to be validated against breaking changes"
    ).prompt("Please provide the path to the input file, this is the `baseline` (`consumer`)")

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run() {
        logger.info("Validating ${inputFilePath} for breaking changes against the baseline in $againstInputFilePath")
        val loadedClasses = ExternalClassLoader(inputFilePath, againstInputFilePath).load()

        logger.info("Main data class ${loadedClasses.first.fullyQualifiedName()} is validated against ${loadedClasses.second.fullyQualifiedName()}")
        val result = Validator().check(loadedClasses.first, loadedClasses.second)

        if (result.isEmpty()) {
            logger.info("Validation succeeded - no breaking changes found.")
        } else {
            logger.error("Breaking changes found:")
            result.forEach { logger.error(it) }
            throw CliktError("Validation failed.")
        }
    }
}

fun main(args: Array<String>) {
    KotlinDataClassCompatibilityChecker().main(args)
}



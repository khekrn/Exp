package com.beam.core.exception

/**
 * @author  KK
 * @version 1.0
 */


class JsonException(message: String?) : RuntimeException(message)

class WorkflowParserError(message: String?, cause: Throwable? = null) : RuntimeException(message, cause)
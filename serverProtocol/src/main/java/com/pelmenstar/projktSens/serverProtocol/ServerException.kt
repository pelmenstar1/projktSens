package com.pelmenstar.projktSens.serverProtocol

import java.lang.RuntimeException

/**
 * Server exception
 */
class ServerException(error: Int) : RuntimeException("Server returned error (${Errors.toString(error)})")
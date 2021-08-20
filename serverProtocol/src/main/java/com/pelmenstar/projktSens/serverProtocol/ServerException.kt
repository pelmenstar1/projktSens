package com.pelmenstar.projktSens.serverProtocol

/**
 * Server exception
 */
class ServerException(error: Int) :
    RuntimeException("Server returned error (${Errors.toString(error)})")
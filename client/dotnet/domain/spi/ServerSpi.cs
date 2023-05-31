// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

namespace App.domain.spi
{
    /// <summary>
    /// The ServerSpi interface represents the server service provider interface (SPI).
    /// It provides a method for transmitting requests to the server.
    /// </summary>
    public interface ServerSpi
    {

        /// <summary>
        /// Transmits a JSON-formatted request string to the server.
        /// </summary>
        /// <param name="jsonRequest">The JSON-formatted string that contains the request to be sent to the server.</param>
        /// <returns>
        /// A JSON string representing the server's response.
        /// </returns>
        /// <exception cref="ServerIOException">If the communication with the server has failed.</exception>
        string transmitRequest(string jsonRequest);
    }
}

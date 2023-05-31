// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

using App.domain.spi;

namespace App.domain.api
{
    /// <summary>
    /// Provider class for creating an instance of the MainServiceApi.
    /// </summary>
    internal class MainServiceApiProvider
    {
        /// <summary>
        /// Creates and returns an instance of the MainServiceApi.
        /// </summary>
        /// <param name="readerSpi">The ReaderSpi implementation to use.</param>
        /// <param name="readerName">The name of the reader to use.</param>
        /// <param name="serverSpi">The ServerSpi implementation to use.</param>
        /// <returns>An instance of the MainServiceApi.</returns>
        public static MainServiceApi getService(ReaderSpi readerSpi, string readerName, ServerSpi serverSpi)
        {
            return new MainServiceApiAdapter(readerSpi, readerName, serverSpi);
        }
    }
}

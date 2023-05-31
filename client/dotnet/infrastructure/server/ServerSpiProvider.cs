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

namespace App.infrastructure.server
{
    /// <summary>
    /// Provides a singleton instance of ServerSpi implemented by ServerSpiAdapter.
    /// </summary>
    public class ServerSpiProvider
    {
        private static ServerSpiAdapter? s_instance;
        private static readonly object s_lock = new object();

        private ServerSpiProvider() { }

        /// <summary>
        /// Gets the singleton instance of ServerSpiAdapter.
        /// </summary>
        public static ServerSpi getInstance(string baseUrl, int port, string endpoint)
        {
            if (s_instance == null)
            {
                lock (s_lock)
                {
                    if (s_instance == null)
                    {
                        s_instance = new ServerSpiAdapter(baseUrl, port, endpoint);
                    }
                }
            }
            return s_instance;
        }
    }
}

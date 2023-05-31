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

namespace App.infrastructure.pcscreader
{
    /// <summary>
    /// Provides a singleton instance of PcscReaderSpi implemented by PcscReaderSpiAdapter.
    /// </summary>
    public class PcscReaderSpiProvider
    {
        private static PcscReaderSpiAdapter? s_instance;
        private static readonly object s_lock = new object();

        private PcscReaderSpiProvider() { }

        /// <summary>
        /// Gets the singleton instance of PcscReaderSpiAdapter.
        /// </summary>
        public static ReaderSpi getInstance()
        {
            if (s_instance == null)
            {
                lock (s_lock)
                {
                    if (s_instance == null)
                    {
                        s_instance = new PcscReaderSpiAdapter();
                    }
                }
            }
            return s_instance;
        }
    }
}

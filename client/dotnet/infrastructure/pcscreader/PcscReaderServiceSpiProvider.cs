using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using App.domain.spi;

namespace App.infrastructure.pcscreader
{
    /// <summary>
    /// Provides a singleton instance of PcscReaderSpi implemented by PcscReaderServiceSpiAdapter.
    /// </summary>
    public class PcscReaderServiceSpiProvider
    {
        private static PcscReaderServiceSpiAdapter? s_instance;
        private static readonly object s_lock = new object();

        private PcscReaderServiceSpiProvider() { }

        /// <summary>
        /// Gets the singleton instance of PcscReaderServiceSpiAdapter.
        /// </summary>
        public static ReaderServiceSpi getInstance()
        {
            if (s_instance == null)
            {
                lock (s_lock)
                {
                    if (s_instance == null)
                    {
                        s_instance = new PcscReaderServiceSpiAdapter();
                    }
                }
            }
            return s_instance;
        }
    }
}

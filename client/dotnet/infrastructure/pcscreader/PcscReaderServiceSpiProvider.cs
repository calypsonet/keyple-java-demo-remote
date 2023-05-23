using App.domain.spi;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace App.infrastructure.pcscreader {
    /// <summary>
    /// Provides a singleton instance of PcscReaderSpi implemented by PcscReaderServiceSpiAdapter.
    /// </summary>
    public class PcscReaderServiceSpiProvider {
        private static PcscReaderServiceSpiAdapter? _instance;
        private static readonly object _lock = new object ();

        private PcscReaderServiceSpiProvider ( ) { }

        /// <summary>
        /// Gets the singleton instance of PcscReaderServiceSpiAdapter.
        /// </summary>
        public static ReaderServiceSpi getInstance ( )
        {
            if (_instance == null)
            {
                lock (_lock)
                {
                    if (_instance == null)
                    {
                        _instance = new PcscReaderServiceSpiAdapter ();
                    }
                }
            }
            return _instance;
        }
    }
}

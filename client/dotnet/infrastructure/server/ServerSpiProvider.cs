using App.domain.spi;
using App.infrastructure.pcscreader;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace App.infrastructure.server {
    /// <summary>
    /// Provides a singleton instance of ServerSpi implemented by ServerSpiAdapter.
    /// </summary>
    public class ServerSpiProvider {
        private static ServerSpiAdapter? _instance;
        private static readonly object _lock = new object ();

        private ServerSpiProvider ( ) { }

        /// <summary>
        /// Gets the singleton instance of ServerSpiAdapter.
        /// </summary>
        public static ServerSpi getInstance ( string baseUrl, int port, string endPoint )
        {
            if (_instance == null)
            {
                lock (_lock)
                {
                    if (_instance == null)
                    {
                        _instance = new ServerSpiAdapter ( baseUrl, port, endPoint );
                    }
                }
            }
            return _instance;
        }
    }
}

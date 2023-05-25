using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using App.domain.spi;
using App.infrastructure.pcscreader;

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
        public static ServerSpi getInstance(string baseUrl, int port, string endPoint)
        {
            if (s_instance == null)
            {
                lock (s_lock)
                {
                    if (s_instance == null)
                    {
                        s_instance = new ServerSpiAdapter(baseUrl, port, endPoint);
                    }
                }
            }
            return s_instance;
        }
    }
}

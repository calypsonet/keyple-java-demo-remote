// ReaderIOException.cs
using System;

namespace App.domain.spi {
    /// <summary>
    /// An exception that is thrown when there is a communication failure with the server.
    /// </summary>
    public class ServerIOException : Exception {
        public ServerIOException ( ) { }

        public ServerIOException ( string message ) : base ( message ) { }

        public ServerIOException ( string message, Exception innerException ) : base ( message, innerException ) { }
    }
}

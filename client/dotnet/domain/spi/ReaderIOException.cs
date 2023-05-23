// ReaderIOException.cs
using System;

namespace App.domain.spi {
    /// <summary>
    /// An exception that is thrown when there is a communication failure with the reader.
    /// </summary>
    public class ReaderIOException : Exception {
        public ReaderIOException ( ) { }

        public ReaderIOException ( string message ) : base ( message ) { }

        public ReaderIOException ( string message, Exception innerException ) : base ( message, innerException ) { }
    }
}

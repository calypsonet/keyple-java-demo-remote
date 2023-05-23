// CardIOException.cs
using System;

namespace App.domain.spi {
    /// <summary>
    /// An exception that is thrown when there is a communication failure with the card.
    /// </summary>
    public class CardIOException : Exception {
        public CardIOException ( ) { }

        public CardIOException ( string message ) : base ( message ) { }

        public CardIOException ( string message, Exception innerException ) : base ( message, innerException ) { }
    }
}

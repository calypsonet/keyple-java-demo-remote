// UnexpectedStatusWordException.cs
using System;

namespace App.domain.spi {
    /// <summary>
    /// An exception that is thrown when the card sent a unexpected status word.
    /// </summary>
    public class UnexpectedStatusWordException : Exception {
        public UnexpectedStatusWordException ( ) { }

        public UnexpectedStatusWordException ( string message ) : base ( message ) { }

        public UnexpectedStatusWordException ( string message, Exception innerException ) : base ( message, innerException ) { }
    }
}

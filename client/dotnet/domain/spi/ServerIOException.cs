using System;

namespace App.domain.spi
{
    /// <summary>
    /// An exception that is thrown when there is a communication failure with the server.
    /// </summary>
    public class ServerIOException : Exception
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="ServerIOException"/> class.
        /// </summary>
        public ServerIOException() { }

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerIOException"/> class with the specified error message.
        /// </summary>
        /// <param name="message">The error message that explains the reason for the exception.</param>
        public ServerIOException(string message) : base(message) { }

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerIOException"/> class with the specified error message and inner exception.
        /// </summary>
        /// <param name="message">The error message that explains the reason for the exception.</param>
        /// <param name="innerException">The exception that is the cause of the current exception, or a null reference if no inner exception is specified.</param>
        public ServerIOException(string message, Exception innerException) : base(message, innerException) { }
    }
}

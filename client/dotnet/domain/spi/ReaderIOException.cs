namespace App.domain.spi
{
    /// <summary>
    /// An exception that is thrown when there is a communication failure with the reader.
    /// </summary>
    public class ReaderIOException : Exception
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="ReaderIOException"/> class.
        /// </summary>
        public ReaderIOException() { }

        /// <summary>
        /// Initializes a new instance of the <see cref="ReaderIOException"/> class with the specified error message.
        /// </summary>
        /// <param name="message">The error message that explains the reason for the exception.</param>
        public ReaderIOException(string message) : base(message) { }

        /// <summary>
        /// Initializes a new instance of the <see cref="ReaderIOException"/> class with the specified error message and inner exception.
        /// </summary>
        /// <param name="message">The error message that explains the reason for the exception.</param>
        /// <param name="innerException">The exception that is the cause of the current exception, or a null reference if no inner exception is specified.</param>
        public ReaderIOException(string message, Exception innerException) : base(message, innerException) { }
    }
}

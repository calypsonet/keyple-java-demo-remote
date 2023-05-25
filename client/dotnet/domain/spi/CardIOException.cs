namespace App.domain.spi
{
    /// <summary>
    /// An exception that is thrown when there is a communication failure with the card.
    /// </summary>
    public class CardIOException : Exception
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="CardIOException"/> class.
        /// </summary>
        public CardIOException() { }

        /// <summary>
        /// Initializes a new instance of the <see cref="CardIOException"/> class with the specified error message.
        /// </summary>
        /// <param name="message">The error message that explains the reason for the exception.</param>
        public CardIOException(string message) : base(message) { }

        /// <summary>
        /// Initializes a new instance of the <see cref="CardIOException"/> class with the specified error message and inner exception.
        /// </summary>
        /// <param name="message">The error message that explains the reason for the exception.</param>
        /// <param name="innerException">The exception that is the cause of the current exception, or a null reference if no inner exception is specified.</param>
        public CardIOException(string message, Exception innerException) : base(message, innerException) { }
    }
}

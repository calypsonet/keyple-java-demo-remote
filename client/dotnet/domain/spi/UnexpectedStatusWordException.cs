using System;

namespace App.domain.spi
{
    /// <summary>
    /// An exception that is thrown when the card sent an unexpected status word.
    /// </summary>
    public class UnexpectedStatusWordException : Exception
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="UnexpectedStatusWordException"/> class.
        /// </summary>
        public UnexpectedStatusWordException() { }

        /// <summary>
        /// Initializes a new instance of the <see cref="UnexpectedStatusWordException"/> class with the specified error message.
        /// </summary>
        /// <param name="message">The error message that explains the reason for the exception.</param>
        public UnexpectedStatusWordException(string message) : base(message) { }

        /// <summary>
        /// Initializes a new instance of the <see cref="UnexpectedStatusWordException"/> class with the specified error message and inner exception.
        /// </summary>
        /// <param name="message">The error message that explains the reason for the exception.</param>
        /// <param name="innerException">The exception that is the cause of the current exception, or a null reference if no inner exception is specified.</param>
        public UnexpectedStatusWordException(string message, Exception innerException) : base(message, innerException) { }
    }
}


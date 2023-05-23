/// <summary>
/// The ServerSpi interface represents the server service provider interface (SPI).
/// It provides a method for transmitting requests to the server.
/// </summary>
namespace App.domain.spi {
    public interface ServerSpi {

        /// <summary>
        /// Transmits a JSON-formatted request string to the server.
        /// </summary>
        /// <param name="jsonRequest">The JSON-formatted string that contains the request to be sent to the server.</param>
        /// <returns>
        /// A JSON string representing the server's response.
        /// </returns>
        string transmitRequest ( string jsonRequest );
    }
}

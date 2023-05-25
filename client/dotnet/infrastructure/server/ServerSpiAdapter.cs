using System.Text;
using App.application;
using App.domain.spi;
using Serilog;

namespace App.infrastructure.server
{
    /// <summary>
    /// The ServerSpiAdapter class implements ServerSpi and provides functionality
    /// to transmit a JSON request to a specified server.
    /// </summary>
    internal class ServerSpiAdapter : ServerSpi
    {
        private readonly ILogger _logger;
        private readonly string _baseUrl;
        private readonly string _endPoint;

        /// <summary>
        /// Initializes a new instance of the ServerSpiAdapter class.
        /// </summary>
        /// <param name="baseUrl">The base URL of the server to connect to.</param>
        /// <param name="port">The port to connect on.</param>
        /// <param name="endPoint">The endpoint to send requests to.</param>
        public ServerSpiAdapter(string baseUrl, int port, string endPoint)
        {
            _logger = Log.ForContext<ServerSpiAdapter>();
            _baseUrl = $"{baseUrl}:{port}";
            _endPoint = endPoint;
        }

        /// <summary>
        /// Transmit a JSON request to the server and return the server's response.
        /// </summary>
        /// <param name="jsonRequest">The JSON request to transmit.</param>
        /// <returns>A JSON string containing the server's response.</returns>
        public string transmitRequest(string jsonRequest)
        {
            if (_logger.IsEnabled(Serilog.Events.LogEventLevel.Debug))
            {
                _logger.Debug($"Tx Json = {jsonRequest}");
            }
            string? result = null;
            try
            {
                // Initialize a new HttpClient with the base URL
                using (HttpClient httpClient = new HttpClient { BaseAddress = new Uri(_baseUrl) })
                {
                    // Create the content of the POST request
                    StringContent content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");

                    // Send the POST request and get the response
                    HttpResponseMessage response = httpClient.PostAsync(_endPoint, content).Result;

                    if (response.IsSuccessStatusCode)
                    {
                        // If the request was successful, read the content of the response
                        result = response.Content.ReadAsStringAsync().Result;

                        if (_logger.IsEnabled(Serilog.Events.LogEventLevel.Debug))
                        {
                            _logger.Debug($"Rx Json = {result}");
                        }
                    }
                    else
                    {
                        // If the request was not successful, throw an exception with the status code
                        throw new ServerIOException($"Error when calling the API: {response.StatusCode}");
                    }
                }
            }
            catch (Exception ex)
            {
                // If an exception occurred, throw a new exception with the message of the original exception
                throw new ServerIOException($"Exception when calling the API: {ex.Message}", ex);
            }

            // Return the result of the request
            return result;
        }
    }
}

using App.domain.spi;

namespace App.domain.api {
    /// <summary>
    /// Provider class for creating an instance of the MainServiceApi.
    /// </summary>
    internal class MainServiceApiProvider {
        /// <summary>
        /// Creates and returns an instance of the MainServiceApi.
        /// </summary>
        /// <param name="readerService">The ReaderServiceSpi implementation to use.</param>
        /// <param name="readerName">The name of the PC/SC reader to use.</param>
        /// <param name="server">The ServerSpi implementation to use.</param>
        /// <returns>An instance of the MainServiceApi.</returns>
        public static MainServiceApi getService ( ReaderServiceSpi readerService, string readerName, ServerSpi server )
        {
            return new MainServiceAdapter ( readerService, readerName, server );
        }
    }
}

using App.domain.spi;
using App.domain.utils;
using PCSC;
using PCSC.Exceptions;
using Serilog;
using Serilog.Events;
using System.Runtime.Versioning;
using System.ServiceProcess;

namespace App.infrastructure.pcscreader {
    /// <summary>
    /// Adapter class that implements the ReaderServiceSpi interface for PC/SC smart card readers.
    /// </summary>
    internal class PcscReaderServiceSpiAdapter : ReaderServiceSpi {
        private readonly ILogger _logger;
        private readonly IContextFactory _contextFactory;
        private ISCardContext _context;
        private string? _readerName;
        private byte[]? _cardAtr;
        private ICardReader? _reader;
        private const string SmartCardServiceName = "SCardSvr";
        private const int ReceiveBufferSize = 256;

        /// <summary>
        /// Creates a new instance of the <see cref="PcscReaderServiceSpiAdapter"/> class.
        /// </summary>
        public PcscReaderServiceSpiAdapter ( )
        {
            _logger = Log.ForContext<PcscReaderServiceSpiAdapter> ();
            _contextFactory = ContextFactory.Instance;
            _context = _contextFactory.Establish ( SCardScope.System );
        }

        /// <inheritdoc/>
        [SupportedOSPlatform ( "windows" )]
        public List<string> GetReaders ( )
        {
            var readerNames = new List<string> ();

            // Check if the service is running, if not, start it.
            ServiceController sc = new ServiceController ( SmartCardServiceName );

            if (sc.Status == ServiceControllerStatus.Stopped)
            {
                try
                {
                    // Start the service if the current status is stopped.
                    sc.Start ();
                }
                catch (InvalidOperationException ex)
                {
                    _logger.Error ( ex, "GetReaders: Unable to start SCardSvr." );
                    return readerNames;
                }
                sc.WaitForStatus ( desiredStatus: ServiceControllerStatus.Running );
                // Establish a new context.
                _context = _contextFactory.Establish ( SCardScope.System );
            }

            // Get the list of readers.
            readerNames.AddRange ( _context.GetReaders () );
            _logger.Information ( $"GetReaders: Found {readerNames.Count} readers." );

            return readerNames;
        }

        /// <inheritdoc/>
        public void SelectReader ( string readerName )
        {
            _readerName = readerName;
        }

        /// <inheritdoc/>
        public bool IsCardPresent ( )
        {
            // Implement the logic to check if a card is present using the PC/SC library
            throw new NotImplementedException ();
        }

        /// <inheritdoc/>
        public bool WaitForCardPresent ( )
        {
            try
            {
                var readerStates = new[]
                {
                    new SCardReaderState { ReaderName = _readerName, CurrentState = SCRState.Unknown }
                };

                while (true)
                {
                    var sc = _context.GetStatusChange ( timeout: 1000, readerStates );
                    if (sc == SCardError.Success)
                    {
                        if ((readerStates[0].EventState & SCRState.Present) == SCRState.Present)
                        {
                            return true;
                        }
                    }
                    else if (sc == SCardError.Timeout)
                    {
                        // Timeout occurred, you can break the loop if you want to stop waiting
                        // or continue to wait for the card
                    }
                    else
                    {
                        // Error occurred, handle it accordingly
                        break;
                    }
                }
            }
            catch (Exception)
            {
                // Handle exceptions if necessary
            }
            return false;
        }

        /// <inheritdoc/>
        public bool WaitForCardAbsent ( )
        {
            try
            {
                var readerStates = new[]
                {
                    new SCardReaderState { ReaderName = _readerName, CurrentState = SCRState.Unknown }
                };

                while (true)
                {
                    var sc = _context.GetStatusChange ( timeout: 1000, readerStates );
                    if (sc == SCardError.Success)
                    {
                        if ((readerStates[0].EventState & SCRState.Empty) == SCRState.Empty)
                        {
                            return true;
                        }
                    }
                    else if (sc == SCardError.Timeout)
                    {
                        // Timeout occurred, you can break the loop if you want to stop waiting
                        // or continue to wait for the card to be removed
                    }
                    else
                    {
                        // Error occurred, handle it accordingly
                        break;
                    }

                    // Update the current state for the next iteration
                    readerStates[0].CurrentState = readerStates[0].EventState;
                }
            }
            catch (Exception)
            {
                // Handle exceptions if necessary
            }
            return false;
        }

        /// <inheritdoc/>
        public void OpenPhysicalChannel ( )
        {
            try
            {
                _cardAtr = null;
                _reader = _context.ConnectReader ( _readerName, SCardShareMode.Shared, SCardProtocol.T0 | SCardProtocol.T1 );
                _logger.Debug ( $"OpenPhysicalChannel: Successfully connected to reader {_readerName}." );

                _cardAtr = _reader.GetAttrib ( SCardAttribute.AtrString );
                if (_logger.IsEnabled ( LogEventLevel.Debug ))
                {
                    _logger.Debug ( $"OpenPhysicalChannel: card ATR is {GetPowerOnData ()}." );
                }
            }
            catch (PCSCException ex) when (ex.SCardError == SCardError.CommunicationError)
            {
                throw new CardIOException ( $"OpenPhysicalChannel: Reader {_readerName} disconnected or card removed", ex );
            }
            catch (Exception ex)
            {
                throw new CardIOException ( $"OpenPhysicalChannel: An error occurred while connecting to reader {_readerName}", ex );
            }
        }

        /// <inheritdoc/>
        public void ClosePhysicalChannel ( )
        {
            if (_reader == null)
            {
                throw new InvalidOperationException ( "Reader not available" );
            }
            try
            {
                // Disconnect the reader 
                _reader.Disconnect ( SCardReaderDisposition.Leave );
            }
            catch (PCSCException ex) when (ex.SCardError == SCardError.CommunicationError)
            {
                throw new CardIOException ( $"ClosePhysicalChannel: Reader {_readerName} disconnected or card removed", ex );
            }
            catch (Exception ex)
            {
                throw new CardIOException ( $"ClosePhysicalChannel: An error occurred while disconnecting the reader {_readerName}", ex );
            }
        }

        /// <inheritdoc/>
        public string GetPowerOnData ( )
        {
            if (_cardAtr == null)
            {
                throw new InvalidOperationException ( "No ATR available" );
            }
            return HexUtil.ToHex ( _cardAtr );
        }

        /// <inheritdoc/>
        public byte[] TransmitApdu ( byte[] commandApdu )
        {
            if (_reader == null)
            {
                throw new InvalidOperationException ( "Reader not available" );
            }
            try
            {
                if (_logger.IsEnabled ( LogEventLevel.Debug ))
                {
                    _logger.Debug ( $"TransmitApdu: command = {HexUtil.ToHex ( commandApdu )}" );
                }
                var receiveBuffer = new byte[ReceiveBufferSize];
                var responseLength = _reader.Transmit ( commandApdu, receiveBuffer );
                byte[] responseApdu = new byte[responseLength];
                Array.Copy ( receiveBuffer, 0, responseApdu, 0, responseLength );
                if (_logger.IsEnabled ( LogEventLevel.Debug ))
                {
                    _logger.Debug ( $"TransmitApdu: response = {HexUtil.ToHex ( responseApdu )}" );
                }
                return responseApdu;
            }
            catch (PCSCException ex) when (ex.SCardError == SCardError.CommunicationError)
            {
                throw new CardIOException ( $"TransmitApdu: error while communicating with the card", ex );
            }
            catch (Exception ex)
            {
                throw new ReaderIOException ( $"TransmitApdu: error while communicating with the reader", ex );
            }
        }
    }
}

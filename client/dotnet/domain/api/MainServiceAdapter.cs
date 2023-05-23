using App.application;
using App.domain.data;
using App.domain.spi;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Serilog;
using Serilog.Events;

namespace App.domain.api
{


    class MainServiceAdapter : MainServiceApi {
        private readonly ILogger _logger;
        private readonly ReaderServiceSpi _readerService;
        private readonly ServerSpi _server;
        private readonly string _clientNodeId;
        private string _localReaderName;

        private const int SW_6100 = 0x6100;
        private const int SW_6C00 = 0x6C00;
        private const int SW1_MASK = 0xFF00;
        private const int SW2_MASK = 0x00FF;

        internal MainServiceAdapter ( ReaderServiceSpi readerService, string readerName, ServerSpi server )
        {
            _logger = Log.ForContext<MainServiceAdapter> ();

            _logger.Information ( "Creation of main service..." );

            _readerService = readerService;
            _server = server;

            _clientNodeId = Guid.NewGuid ().ToString ();

            List<string> readerNames = readerService.GetReaders ();
            
            if (readerNames.Count == 0)
            {
                Misc.DisplayAndLog ( "No reader found!", ConsoleColor.Red, LogEventLevel.Error, _logger );
                Environment.Exit ( 1 );
            }

            if (!readerNames.Contains ( readerName ))
            {
                Misc.DisplayAndLog ( $"Reader '{readerName}' is not found in the list of readers.", ConsoleColor.Red, LogEventLevel.Error, _logger );
                Environment.Exit ( 1 );
            }

            _localReaderName = readerName;
            _logger.Information ( $"Select reader {_localReaderName}" );
            readerService.SelectReader ( _localReaderName );
        }


        private bool IsCase4 ( byte[] apduCommand )
        {
            if (apduCommand != null && apduCommand.Length > 4)
            {
                return apduCommand[4] == apduCommand.Length - 6;
            }
            return false;
        }


        ApduResponse ProcessApduRequest ( ApduRequest apduRequest )
        {
            ApduResponse apduResponse = new ApduResponse ();

            apduResponse.Apdu = _readerService.TransmitApdu ( apduRequest.Apdu );
            apduResponse.StatusWord = (apduResponse.Apdu[apduResponse.Apdu.Length - 2] << 8) | apduResponse.Apdu[apduResponse.Apdu.Length - 1];

            if (apduResponse.Apdu.Length == 2)
            {
                if ((apduResponse.StatusWord & SW1_MASK) == SW_6100)
                {
                    byte[] getResponseApdu = {
                        0x00,
                        0xC0,
                        0x00,
                        0x00,
                        (byte)(apduResponse.StatusWord & SW2_MASK)
                        };
                    apduResponse = ProcessApduRequest ( new ApduRequest { Apdu = getResponseApdu, Info = "Internal Get Response" } );
                }
                else if ((apduResponse.StatusWord & SW1_MASK) == SW_6C00)
                {
                    apduRequest.Apdu[apduRequest.Apdu.Length - 1] =
                        (byte)(apduResponse.StatusWord & SW2_MASK);
                    apduResponse = ProcessApduRequest ( apduRequest );
                }
                else if (IsCase4 ( apduRequest.Apdu )
                    && apduRequest.SuccessfulStatusWords.Contains ( apduResponse.StatusWord ))
                {
                    byte[] getResponseApdu = {
                    0x00,
                    0xC0,
                    0x00,
                    0x00,
                    apduRequest.Apdu[apduRequest.Apdu.Length - 1]
                    };
                    apduResponse = ProcessApduRequest ( new ApduRequest { Apdu = getResponseApdu, Info = "Internal Get Response" } );
                }
            }

            return apduResponse;
        }

        private byte ComputeSelectApplicationP2 ( FileOccurrence fileOccurrence, FileControlInformation fileControlInformation )
        {
            byte p2;

            switch (fileOccurrence)
            {
                case FileOccurrence.FIRST:
                    p2 = 0x00;
                    break;
                case FileOccurrence.LAST:
                    p2 = 0x01;
                    break;
                case FileOccurrence.NEXT:
                    p2 = 0x02;
                    break;
                case FileOccurrence.PREVIOUS:
                    p2 = 0x03;
                    break;
                default:
                    throw new Exception ( "Unexpected value: " + fileOccurrence );
            }

            switch (fileControlInformation)
            {
                case FileControlInformation.FCI:
                    p2 |= 0x00;
                    break;
                case FileControlInformation.FCP:
                    p2 |= 0x04;
                    break;
                case FileControlInformation.FMD:
                    p2 |= 0x08;
                    break;
                case FileControlInformation.NO_RESPONSE:
                    p2 |= 0x0C;
                    break;
                default:
                    throw new Exception ( "Unexpected value: " + fileControlInformation );
            }

            return p2;
        }

        private ApduResponse SelectApplication ( CardSelector cardSelector )
        {
            byte[] selectApplicationCommand = new byte[6 + cardSelector.Aid.Length];
            selectApplicationCommand[0] = 0x00; // CLA
            selectApplicationCommand[1] = 0xA4; // INS
            selectApplicationCommand[2] = 0x04; // P1: select by name
                                                // P2: b0,b1 define the File occurrence, b2,b3 define the File control information
                                                // we use the bitmask defined in the respective enums
            selectApplicationCommand[3] =
                ComputeSelectApplicationP2 (
                    cardSelector.FileOccurrence, cardSelector.FileControlInformation );
            selectApplicationCommand[4] = (byte)(cardSelector.Aid.Length); // Lc
            Array.Copy ( cardSelector.Aid, 0, selectApplicationCommand, 5, cardSelector.Aid.Length ); // data
            selectApplicationCommand[5 + cardSelector.Aid.Length] = 0x00; // Le

            ApduRequest apduRequest = new ApduRequest
            {
                Apdu = selectApplicationCommand
            };

            if (_logger.IsEnabled ( Serilog.Events.LogEventLevel.Debug ))
            {
                apduRequest.Info = "Internal Select Application";
            }

            return ProcessApduRequest ( apduRequest );
        }

        private CardResponse ProcessCardRequest ( CardRequest cardRequest )
        {
            var apduResponses = new List<ApduResponse> ();

            foreach (var apduRequest in cardRequest.ApduRequests)
            {
                try
                {
                    var apduResponse = ProcessApduRequest ( apduRequest );
                    apduResponses.Add ( apduResponse );

                    if (!apduRequest.SuccessfulStatusWords.Contains ( apduResponse.StatusWord ))
                    {
                        throw new UnexpectedStatusWordException ( "Unexpected status word." );
                    }
                }
                catch (ServerIOException ex)
                {
                    // The process has been interrupted. We close the logical channel and throw a
                    // ReaderBrokenCommunicationException.
                    _readerService.ClosePhysicalChannel ();

                    throw new ReaderIOException ( "Reader communication failure while transmitting a card request.",
                        ex );
                }
                catch (UnexpectedStatusWordException ex)
                {
                    // The process has been interrupted. We close the logical channel and throw a
                    // CardBrokenCommunicationException.
                    _readerService.ClosePhysicalChannel ();

                    throw new CardIOException (
                        "Card communication failure while transmitting a card request.",
                        ex );
                }
            }

            return new CardResponse { IsLogicalChannelOpen = true, ApduResponses = apduResponses };
        }


        private CardSelectionResponse ProcessCardSelectionRequest ( CardSelectionRequest cardSelectionRequest )
        {
            _readerService.OpenPhysicalChannel ();
            ApduResponse selectAppResponse = SelectApplication ( cardSelectionRequest.CardSelector );
            CardResponse cardResponse = null;
            if (cardSelectionRequest.CardRequest != null)
            {
                cardResponse = ProcessCardRequest ( cardSelectionRequest.CardRequest );
            }
            CardSelectionResponse cardSelectionResponse = new CardSelectionResponse { HasMatched = true, PowerOnData = _readerService.GetPowerOnData (), SelectApplicationResponse = selectAppResponse, CardResponse = cardResponse };
            return cardSelectionResponse;
        }

        private MessageDto ProcessTransaction ( MessageDto message )
        {
            bool isServiceEnded = false;
            while (message.Action != "END_REMOTE_SERVICE")
            {
                _logger.Information ( $"Processing action {message.Action}" );
                var jsonObject = JObject.Parse ( message.Body );
                string service = jsonObject["service"].ToString ();
                JObject body = new JObject ();
                body["service"] = service;
                _logger.Information ( $"Service: {service}" );
                switch (service)
                {
                    case "IS_CONTACTLESS":
                        body["result"] = true;
                        break;
                    case "IS_CARD_PRESENT":
                        body["result"] = true;
                        break;
                    case "TRANSMIT_CARD_SELECTION_REQUESTS":
                        var transmitCardSelectionRequestsCmdBody = JsonConvert.DeserializeObject<TransmitCardSelectionRequestsCmdBody> ( message.Body );
                        var cardSelectionRequest = transmitCardSelectionRequestsCmdBody.Parameters.CardSelectionRequests[0];
                        var cardSelectionResponse = ProcessCardSelectionRequest ( cardSelectionRequest );
                        var cardSelectionResponses = new List<CardSelectionResponse> ();
                        cardSelectionResponses.Add ( cardSelectionResponse );
                        body["result"] = JArray.FromObject ( cardSelectionResponses );
                        break;
                    case "TRANSMIT_CARD_REQUEST":
                        var transmitCardRequestsCmdBody = JsonConvert.DeserializeObject<TransmitCardRequestCmdBody> ( message.Body );
                        var cardRequest = transmitCardRequestsCmdBody.Parameters.CardRequest;
                        var cardResponse = ProcessCardRequest ( cardRequest );
                        body["result"] = JObject.FromObject ( cardResponse );
                        break;
                }
                message.SetAction ( "RESP" );
                string jsonBodyString = JsonConvert.SerializeObject ( body, Formatting.None );
                message.SetBody ( jsonBodyString );
                var jsonResponse = _server.transmitRequest ( JsonConvert.SerializeObject ( message, Formatting.None ) );
                message = JsonConvert.DeserializeObject<List<MessageDto>> ( jsonResponse )[0];
            }
            return message;
        }

        private string ExecuteRemoteService ( string serviceId, InputData inputData )
        {
            string sessionId = Guid.NewGuid ().ToString ();


            // Create and fill ExecuteRemoteServiceBodyContent object
            ExecuteRemoteServiceBodyContent bodyContent = new ExecuteRemoteServiceBodyContent
            {
                ServiceId = serviceId,
                InputData = inputData
            };

            // Create and fill RemoteServiceDto object
            var message = new MessageDto ()
                .SetAction ( "EXECUTE_REMOTE_SERVICE" )
                .SetBody ( JsonConvert.SerializeObject ( bodyContent, Formatting.None ) )
                .SetClientNodeId ( _clientNodeId )
                .SetLocalReaderName ( _localReaderName )
                .SetSessionId ( sessionId );

            var jsonResponse = _server.transmitRequest ( JsonConvert.SerializeObject ( message ) );

            message = JsonConvert.DeserializeObject<List<MessageDto>> ( jsonResponse )[0];

            message = ProcessTransaction ( message );

            return message.Body;
        }
        public void WaitForCardInsertion ( )
        {
            _readerService.WaitForCardPresent();
        }

        public void WaitForCardRemoval ( )
        {
            _readerService.WaitForCardAbsent();
        }

        public string SelectAndReadContracts ( )
        {
            _logger.Information ( "Execute remote service to read the card content..." );

            return ExecuteRemoteService ( "SELECT_APP_AND_READ_CONTRACTS", new InputDataRead { } );
        }

        public string SelectAndIncreaseContractCounter ( int counterIncrement )
        {
            _logger.Information ( "Execute remote service to increase the contract counter..." );

            return ExecuteRemoteService ( "SELECT_APP_AND_INCREASE_CONTRACT_COUNTER", new InputDataWrite { CounterIncrement = counterIncrement.ToString () } );
        }

    }
}

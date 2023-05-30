// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

using App.application;
using App.domain.data;
using App.domain.spi;
using Newtonsoft.Json;
using Serilog;
using Serilog.Events;

namespace App.domain.api
{


    class MainServiceAdapter : MainServiceApi
    {
        private readonly ILogger _logger;
        private readonly ReaderServiceSpi _readerService;
        private readonly ServerSpi _server;
        private readonly string _clientNodeId;
        private string _localReaderName;

        private const int SW_9000 = 0x9000;
        private const int SW_6100 = 0x6100;
        private const int SW_6C00 = 0x6C00;
        private const int SW1_MASK = 0xFF00;
        private const int SW2_MASK = 0x00FF;

        private const string EXECUTE_REMOTE_SERVICE = "EXECUTE_REMOTE_SERVICE";
        private const string END_REMOTE_SERVICE = "END_REMOTE_SERVICE";
        private const string CMD = "CMD";
        private const string RESP = "RESP";

        private const string IS_CONTACTLESS = "IS_CONTACTLESS";
        private const string IS_CARD_PRESENT = "IS_CARD_PRESENT";
        private const string TRANSMIT_CARD_SELECTION_REQUESTS = "TRANSMIT_CARD_SELECTION_REQUESTS";
        private const string TRANSMIT_CARD_REQUEST = "TRANSMIT_CARD_REQUEST";

        internal MainServiceAdapter(ReaderServiceSpi readerService, string readerName, ServerSpi server)
        {
            _logger = Log.ForContext<MainServiceAdapter>();

            _logger.Information("Creation of main service...");

            _readerService = readerService;
            _server = server;

            _clientNodeId = Guid.NewGuid().ToString();

            List<string> readerNames = readerService.GetReaders();

            if (readerNames.Count == 0)
            {
                Misc.DisplayAndLog("No reader found!", ConsoleColor.Red, LogEventLevel.Error, _logger);
                Environment.Exit(1);
            }

            if (!readerNames.Contains(readerName))
            {
                Misc.DisplayAndLog($"Reader '{readerName}' is not found in the list of readers.", ConsoleColor.Red, LogEventLevel.Error, _logger);
                Environment.Exit(1);
            }

            _localReaderName = readerName;
            _logger.Information($"Select reader {_localReaderName}");
            readerService.SelectReader(_localReaderName);
        }


        private bool IsCase4(byte[] apduCommand)
        {
            if (apduCommand != null && apduCommand.Length > 4)
            {
                return apduCommand[4] == apduCommand.Length - 6;
            }
            return false;
        }


        ApduResponse ProcessApduRequest(ApduRequest apduRequest)
        {
            byte[] apdu = _readerService.TransmitApdu(apduRequest.Apdu);
            ApduResponse apduResponse = new ApduResponse
            {
                Apdu = apdu,
                StatusWord = (apdu[apdu.Length - 2] << 8) | apdu[apdu.Length - 1]
            };

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
                    apduResponse = ProcessApduRequest(new ApduRequest { Apdu = getResponseApdu, SuccessfulStatusWords = new HashSet<int>(), Info = "Internal Get Response" });
                }
                else if ((apduResponse.StatusWord & SW1_MASK) == SW_6C00)
                {
                    apduRequest.Apdu[apduRequest.Apdu.Length - 1] =
                        (byte)(apduResponse.StatusWord & SW2_MASK);
                    apduResponse = ProcessApduRequest(apduRequest);
                }
                else if (IsCase4(apduRequest.Apdu)
                    && apduRequest.SuccessfulStatusWords.Contains(apduResponse.StatusWord))
                {
                    byte[] getResponseApdu = {
                    0x00,
                    0xC0,
                    0x00,
                    0x00,
                    apduRequest.Apdu[apduRequest.Apdu.Length - 1]
                    };
                    apduResponse = ProcessApduRequest(new ApduRequest { Apdu = getResponseApdu, SuccessfulStatusWords = new HashSet<int>(), Info = "Internal Get Response" });
                }
            }

            return apduResponse;
        }

        private byte ComputeSelectApplicationP2(FileOccurrence fileOccurrence, FileControlInformation fileControlInformation)
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
                    throw new Exception("Unexpected value: " + fileOccurrence);
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
                    throw new Exception("Unexpected value: " + fileControlInformation);
            }

            return p2;
        }

        private ApduResponse SelectApplication(CardSelector cardSelector)
        {
            if (cardSelector != null && cardSelector.Aid != null)
            {
                byte[] selectApplicationCommand = new byte[6 + cardSelector.Aid.Length];
                selectApplicationCommand[0] = 0x00; // CLA
                selectApplicationCommand[1] = 0xA4; // INS
                selectApplicationCommand[2] = 0x04; // P1: select by name
                                                    // P2: b0,b1 define the File occurrence, b2,b3 define the File control information
                                                    // we use the bitmask defined in the respective enums
                selectApplicationCommand[3] =
                    ComputeSelectApplicationP2(
                        cardSelector.FileOccurrence, cardSelector.FileControlInformation);
                selectApplicationCommand[4] = (byte)(cardSelector.Aid.Length); // Lc
                Array.Copy(cardSelector.Aid, 0, selectApplicationCommand, 5, cardSelector.Aid.Length); // data
                selectApplicationCommand[5 + cardSelector.Aid.Length] = 0x00; // Le

                ApduRequest apduRequest = new ApduRequest
                {
                    Apdu = selectApplicationCommand,
                    SuccessfulStatusWords = new HashSet<int>()
                };

                if (_logger.IsEnabled(LogEventLevel.Debug))
                {
                    apduRequest.Info = "Internal Select Application";
                }

                return ProcessApduRequest(apduRequest);
            }

            // Handle the case where cardSelector or cardSelector.Aid is null.
            throw new ArgumentNullException("cardSelector or cardSelector.Aid is null.");
        }

        private CardResponse ProcessCardRequest(CardRequest cardRequest)
        {
            List<ApduResponse> apduResponses = new List<ApduResponse>();

            foreach (ApduRequest apduRequest in cardRequest.ApduRequests)
            {
                try
                {
                    ApduResponse apduResponse = ProcessApduRequest(apduRequest);
                    apduResponses.Add(apduResponse);

                    if (!apduRequest.SuccessfulStatusWords.Contains(apduResponse.StatusWord))
                    {
                        throw new UnexpectedStatusWordException($"Unexpected status word: {apduResponse.StatusWord:X}");
                    }
                }
                catch (ServerIOException ex)
                {
                    // The process has been interrupted. We close the logical channel and throw a
                    // ReaderBrokenCommunicationException.
                    _readerService.ClosePhysicalChannel();

                    throw new ReaderIOException("Reader communication failure while transmitting a card request.",
                        ex);
                }
                catch (UnexpectedStatusWordException ex)
                {
                    // The process has been interrupted. We close the logical channel and throw a
                    // CardBrokenCommunicationException.
                    _readerService.ClosePhysicalChannel();

                    throw new CardIOException(
                        "Card communication failure while transmitting a card request.",
                        ex);
                }
            }

            return new CardResponse { IsLogicalChannelOpen = true, ApduResponses = apduResponses };
        }


        private CardSelectionResponse ProcessCardSelectionRequest(CardSelectionRequest cardSelectionRequest)
        {
            _readerService.OpenPhysicalChannel();
            ApduResponse selectAppResponse = SelectApplication(cardSelectionRequest.CardSelector);
            CardResponse? cardResponse = null;

            if (cardSelectionRequest.CardRequest != null)
            {
                cardResponse = ProcessCardRequest(cardSelectionRequest.CardRequest);
            }

            CardSelectionResponse cardSelectionResponse = new CardSelectionResponse
            {
                HasMatched = cardSelectionRequest.CardSelector.SuccessfulSelectionStatusWords.Contains(selectAppResponse.StatusWord),
                PowerOnData = _readerService.GetPowerOnData(),
                SelectApplicationResponse = selectAppResponse,
                CardResponse = cardResponse
            };

            return cardSelectionResponse;
        }

        private MessageDto ProcessTransaction(MessageDto message)
        {
            while (message.Action != END_REMOTE_SERVICE)
            {
                _logger.Information($"Processing action {message.Action}");

                CmdBody command = JsonConvert.DeserializeObject<CmdBody>(message.Body)!;

                string service = command.Service;
                _logger.Information($"Service: {service}");
                string jsonBodyString = "";
                switch (service)
                {
                    case IS_CONTACTLESS:
                        jsonBodyString = IsContactless();
                        break;
                    case IS_CARD_PRESENT:
                        jsonBodyString = IsCardPresent();
                        break;
                    case TRANSMIT_CARD_SELECTION_REQUESTS:
                        jsonBodyString = TransmitCardSelectionRequest(message);
                        break;
                    case TRANSMIT_CARD_REQUEST:
                        jsonBodyString = TransmitCardRequest(message);
                        break;
                }
                message.SetAction(RESP);
                message.SetBody(jsonBodyString);
                string jsonResponse = _server.transmitRequest(JsonConvert.SerializeObject(message, Formatting.None));
                message = JsonConvert.DeserializeObject<List<MessageDto>>(jsonResponse)![0];
            }
            return message;
        }

        private static string IsContactless()
        {
            string jsonBodyString;
            IsContactlessRespBody isContactlessRespBody = new IsContactlessRespBody();
            isContactlessRespBody.Result = true;
            jsonBodyString = JsonConvert.SerializeObject(isContactlessRespBody, Formatting.None);
            return jsonBodyString;
        }

        private static string IsCardPresent()
        {
            string jsonBodyString;
            IsCardPresentRespBody isCardPresentRespBody = new IsCardPresentRespBody();
            isCardPresentRespBody.Result = true;
            jsonBodyString = JsonConvert.SerializeObject(isCardPresentRespBody, Formatting.None);
            return jsonBodyString;
        }

        private string TransmitCardSelectionRequest(MessageDto message)
        {
            string jsonBodyString;
            TransmitCardSelectionRequestsRespBody transmitCardSelectionRequestsRespBody = new TransmitCardSelectionRequestsRespBody();
            TransmitCardSelectionRequestsCmdBody transmitCardSelectionRequestsCmdBody = JsonConvert.DeserializeObject<TransmitCardSelectionRequestsCmdBody>(message.Body)!;
            List<CardSelectionResponse> cardSelectionResponses = new List<CardSelectionResponse>();

            foreach (CardSelectionRequest cardSelectionRequest in transmitCardSelectionRequestsCmdBody.Parameters.CardSelectionRequests)
            {
                try
                {
                    CardSelectionResponse cardSelectionResponse = ProcessCardSelectionRequest(cardSelectionRequest);
                    cardSelectionResponses.Add(cardSelectionResponse);
                    if (cardSelectionResponse.HasMatched)
                    {
                        break;
                    }
                }
                catch (CardIOException ex)
                {
                    transmitCardSelectionRequestsRespBody.Error = new Error { Code = ErrorCode.CARD_COMMUNICATION_ERROR, Message = ex.Message };
                    break;
                }
                catch (ReaderIOException ex)
                {
                    transmitCardSelectionRequestsRespBody.Error = new Error { Code = ErrorCode.READER_COMMUNICATION_ERROR, Message = ex.Message };
                    break;
                }
                catch (UnexpectedStatusWordException ex)
                {
                    transmitCardSelectionRequestsRespBody.Error = new Error { Code = ErrorCode.CARD_COMMAND_ERROR, Message = ex.Message };
                    break;
                }
            }

            transmitCardSelectionRequestsRespBody.Result = cardSelectionResponses;
            jsonBodyString = JsonConvert.SerializeObject(transmitCardSelectionRequestsRespBody, Formatting.None);
            return jsonBodyString;
        }

        private string TransmitCardRequest(MessageDto message)
        {
            string jsonBodyString;
            TransmitCardRequestsRespBody transmitCardRequestsRespBody = new TransmitCardRequestsRespBody();
            TransmitCardRequestCmdBody transmitCardRequestsCmdBody = JsonConvert.DeserializeObject<TransmitCardRequestCmdBody>(message.Body)!;
            CardRequest cardRequest = transmitCardRequestsCmdBody.Parameters.CardRequest;
            try
            {
                CardResponse cardResponse = ProcessCardRequest(cardRequest);
                transmitCardRequestsRespBody.Result = cardResponse;
            }
            catch (CardIOException ex)
            {
                transmitCardRequestsRespBody.Error = new Error { Code = ErrorCode.CARD_COMMUNICATION_ERROR, Message = ex.Message };
            }
            catch (ReaderIOException ex)
            {
                transmitCardRequestsRespBody.Error = new Error { Code = ErrorCode.READER_COMMUNICATION_ERROR, Message = ex.Message };
            }
            catch (UnexpectedStatusWordException ex)
            {
                transmitCardRequestsRespBody.Error = new Error { Code = ErrorCode.CARD_COMMAND_ERROR, Message = ex.Message };
            }
            jsonBodyString = JsonConvert.SerializeObject(transmitCardRequestsRespBody, Formatting.None);
            return jsonBodyString;
        }

        private string ExecuteRemoteService(string serviceId, InputData inputData)
        {
            string sessionId = Guid.NewGuid().ToString();


            // Create and fill ExecuteRemoteServiceBodyContent object
            ExecuteRemoteServiceBody bodyContent = new ExecuteRemoteServiceBody
            {
                ServiceId = serviceId,
                InputData = inputData
            };

            // Create and fill RemoteServiceDto object
            MessageDto message = new MessageDto { Action = EXECUTE_REMOTE_SERVICE, Body = JsonConvert.SerializeObject(bodyContent, Formatting.None), ClientNodeId = _clientNodeId, SessionId = sessionId };
            message.SetLocalReaderName(_localReaderName);

            string jsonResponse = _server.transmitRequest(JsonConvert.SerializeObject(message));

            message = JsonConvert.DeserializeObject<List<MessageDto>>(jsonResponse)![0];

            message = ProcessTransaction(message);

            return message.Body;
        }
        /// <inheritdoc/>
        public void WaitForCardInsertion()
        {
            _readerService.WaitForCardPresent();
        }

        /// <inheritdoc/>
        public void WaitForCardRemoval()
        {
            _readerService.WaitForCardAbsent();
        }

        /// <inheritdoc/>
        public string SelectAndReadContracts()
        {
            _logger.Information("Execute remote service to read the card content...");

            return ExecuteRemoteService("SELECT_APP_AND_READ_CONTRACTS", new InputDataRead { });
        }

        /// <inheritdoc/>
        public string SelectAndIncreaseContractCounter(int counterIncrement)
        {
            _logger.Information("Execute remote service to increase the contract counter...");

            return ExecuteRemoteService("SELECT_APP_AND_INCREASE_CONTRACT_COUNTER", new InputDataWrite { CounterIncrement = counterIncrement.ToString() });
        }

    }
}

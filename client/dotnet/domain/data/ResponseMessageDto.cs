// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

using App.domain.utils;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace App.domain.data
{

    /// <summary>
    /// Error codes.
    /// </summary>
    public enum ErrorCode
    {
        /// <summary>
        /// Reader communication error.
        /// </summary>
        READER_COMMUNICATION_ERROR,

        /// <summary>
        /// Card communication error.
        /// </summary>
        CARD_COMMUNICATION_ERROR,

        /// <summary>
        /// Card command error.
        /// </summary>
        CARD_COMMAND_ERROR,
    }

    /// <summary>
    /// Represents an error.
    /// </summary>
    public class Error
    {
        /// <summary>
        /// Error code.
        /// </summary>
        [JsonConverter(typeof(StringEnumConverter))]
        [JsonProperty("code")]
        public required ErrorCode Code { get; set; }

        /// <summary>
        /// Error message.
        /// </summary>
        [JsonProperty("message")]
        public required string Message { get; set; }
    }

    /// <summary>
    /// Represents an APDU response.
    /// </summary>
    public class ApduResponse
    {
        /// <summary>
        /// APDU data.
        /// </summary>
        [JsonConverter(typeof(HexStringToByteArrayConverter))]
        [JsonProperty("apdu")]
        public required byte[] Apdu { get; set; }

        /// <summary>
        /// Status word.
        /// </summary>
        [JsonConverter(typeof(HexStringToIntConverter))]
        [JsonProperty("statusWord")]
        public int StatusWord { get; set; }
    }

    /// <summary>
    /// Represents a card response.
    /// </summary>
    public class CardResponse
    {
        /// <summary>
        /// Z value indicating whether the logical channel is open.
        /// </summary>
        [JsonProperty("isLogicalChannelOpen")]
        public bool IsLogicalChannelOpen { get; set; }

        /// <summary>
        /// List of APDU responses.
        /// </summary>
        [JsonProperty("apduResponses")]
        public required List<ApduResponse> ApduResponses { get; set; }
    }

    /// <summary>
    /// Represents a card selection response.
    /// </summary>
    public class CardSelectionResponse
    {
        /// <summary>
        /// A value indicating whether a card has been matched.
        /// </summary>
        [JsonProperty("hasMatched")]
        public bool HasMatched { get; set; }

        /// <summary>
        /// The power-on data.
        /// </summary>
        [JsonProperty("powerOnData")]
        public string? PowerOnData { get; set; }

        /// <summary>
        /// Response of the selection application command.
        /// </summary>
        [JsonProperty("selectApplicationResponse")]
        public ApduResponse? SelectApplicationResponse { get; set; }

        /// <summary>
        /// Card response.
        /// </summary>
        [JsonProperty("cardResponse")]
        public CardResponse? CardResponse { get; set; }
    }

    /// <summary>
    /// Represents the body of a response for the "IS_CONTACTLESS" service.
    /// </summary>
    public class IsContactlessRespBody
    {
        /// <summary>
        /// Service name.
        /// </summary>
        [JsonProperty("service")]
        public const string Service = "IS_CONTACTLESS";

        /// <summary>
        /// Contactless mode flag.
        /// </summary>
        [JsonProperty("result", NullValueHandling = NullValueHandling.Ignore)]
        public bool? Result { get; set; }

        /// <summary>
        /// Error information, if any.
        /// </summary>
        [JsonProperty("error", NullValueHandling = NullValueHandling.Ignore)]
        public Error? Error { get; set; }
    }

    /// <summary>
    /// Represents the body of a response for the "IS_CARD_PRESENT" service.
    /// </summary>
    public class IsCardPresentRespBody
    {
        /// <summary>
        /// Service name.
        /// </summary>
        [JsonProperty("service")]
        public const string Service = "IS_CARD_PRESENT";

        /// <summary>
        /// Result of the card presence check.
        /// </summary>
        [JsonProperty("result", NullValueHandling = NullValueHandling.Ignore)]
        public bool? Result { get; set; }

        /// <summary>
        /// Error information, if any.
        /// </summary>
        [JsonProperty("error", NullValueHandling = NullValueHandling.Ignore)]
        public Error? Error { get; set; }
    }

    /// <summary>
    /// Represents the body of a response for the "TRANSMIT_CARD_SELECTION_REQUESTS" service.
    /// </summary>
    public class TransmitCardSelectionRequestsRespBody
    {
        /// <summary>
        /// Service name.
        /// </summary>
        [JsonProperty("service")]
        public const string Service = "TRANSMIT_CARD_SELECTION_REQUESTS";

        /// <summary>
        /// Result of the card selection requests.
        /// </summary>
        [JsonProperty("result", NullValueHandling = NullValueHandling.Ignore)]
        public List<CardSelectionResponse>? Result { get; set; }

        /// <summary>
        /// Error information, if any.
        /// </summary>
        [JsonProperty("error", NullValueHandling = NullValueHandling.Ignore)]
        public Error? Error { get; set; }
    }

    /// <summary>
    /// Represents the body of a response for the "TRANSMIT_CARD_REQUEST" service.
    /// </summary>
    public class TransmitCardRequestsRespBody
    {
        /// <summary>
        /// Service name.
        /// </summary>
        [JsonProperty("service")]
        public const string Service = "TRANSMIT_CARD_REQUEST";

        /// <summary>
        /// Result of the card request.
        /// </summary>
        [JsonProperty("result", NullValueHandling = NullValueHandling.Ignore)]
        public CardResponse? Result { get; set; }

        /// <summary>
        /// Error information, if any.
        /// </summary>
        [JsonProperty("error", NullValueHandling = NullValueHandling.Ignore)]
        public Error? Error { get; set; }
    }
}

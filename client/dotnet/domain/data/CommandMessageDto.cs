using System.Collections.Generic;
using App.domain.utils;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace App.domain.data
{
    /// <summary>
    /// File occurrence selection strategy.
    /// </summary>
    public enum FileOccurrence
    {
        /// <summary>
        /// Select the first occurrence.
        /// </summary>
        FIRST,

        /// <summary>
        /// Select the last occurrence.
        /// </summary>
        LAST,

        /// <summary>
        /// Select the next occurrence.
        /// </summary>
        NEXT,

        /// <summary>
        /// Select the previous occurrence.
        /// </summary>
        PREVIOUS,
    }

    /// <summary>
    /// File information output format.
    /// </summary>
    public enum FileControlInformation
    {
        /// <summary>
        /// File Control Information (FCI) format.
        /// </summary>
        FCI,

        /// <summary>
        /// File Control Parameters (FCP) format.
        /// </summary>
        FCP,

        /// <summary>
        /// File Management Data (FMD) format.
        /// </summary>
        FMD,

        /// <summary>
        /// No response format.
        /// </summary>
        NO_RESPONSE,
    }

    /// <summary>
    /// Selection mode.
    /// </summary>
    public enum MultiSelectionProcessing
    {
        /// <summary>
        /// Select only the first match.
        /// </summary>
        FIRST_MATCH,

        /// <summary>
        /// Process all matches.
        /// </summary>
        PROCESS_ALL,
    }

    /// <summary>
    /// Channel control mode.
    /// </summary>
    public enum ChannelControl
    {
        /// <summary>
        /// Keep the channel open.
        /// </summary>
        KEEP_OPEN,

        /// <summary>
        /// Close the channel after the operation.
        /// </summary>
        CLOSE_AFTER,
    }

    /// <summary>
    /// Card selector used for card selection.
    /// </summary>
    public class CardSelector
    {
        /// <summary>
        /// Card protocol.
        /// </summary>
        [JsonProperty("cardProtocol")]
        public string? CardProtocol { get; set; }

        /// <summary>
        /// Power On Data regular expression filter.
        /// </summary>
        [JsonProperty("powerOnDataRegex")]
        public string? PowerOnDataRegex { get; set; }

        /// <summary>
        /// Application Identifier (AID) of the card.
        /// </summary>
        [JsonConverter(typeof(HexStringToByteArrayConverter))]
        [JsonProperty("aid")]
        public byte[]? Aid { get; set; }

        /// <summary>
        /// File occurrence.
        /// </summary>
        [JsonConverter(typeof(FileOccurrenceConverter))]
        [JsonProperty("fileOccurrence")]
        public FileOccurrence FileOccurrence { get; set; }

        /// <summary>
        /// File control information.
        /// </summary>
        [JsonConverter(typeof(FileControlInformationConverter))]
        [JsonProperty("fileControlInformation")]
        public FileControlInformation FileControlInformation { get; set; }

        /// <summary>
        /// Successful status words of the selection application command.
        /// </summary>
        [JsonConverter(typeof(HexStringToSetToIntHashSetConverter))]
        [JsonProperty("successfulSelectionStatusWords")]
        public required HashSet<int> SuccessfulSelectionStatusWords { get; set; }
    }

    /// <summary>
    /// Represents an APDU request.
    /// </summary>
    public class ApduRequest
    {
        /// <summary>
        /// APDU data.
        /// </summary>
        [JsonConverter(typeof(HexStringToByteArrayConverter))]
        [JsonProperty("apdu")]
        public required byte[] Apdu { get; set; }

        /// <summary>
        /// Successful status words for the APDU.
        /// </summary>
        [JsonConverter(typeof(HexStringToSetToIntHashSetConverter))]
        [JsonProperty("successfulStatusWords")]
        public required HashSet<int> SuccessfulStatusWords { get; set; }

        /// <summary>
        /// Extra information about the APDU.
        /// </summary>
        [JsonProperty("info")]
        public string? Info { get; set; }
    }

    /// <summary>
    /// Represents a card request containing a list of APDU requests.
    /// </summary>
    public class CardRequest
    {
        /// <summary>
        /// List of APDU requests.
        /// </summary>
        [JsonProperty("apduRequests")]
        public required List<ApduRequest> ApduRequests { get; set; }

        /// <summary>
        /// A value indicating whether status codes verification is enabled.
        /// </summary>
        [JsonProperty("isStatusCodesVerificationEnabled")]
        public bool IsStatusCodesVerificationEnabled { get; set; }
    }

    /// <summary>
    /// Represents a card selection request.
    /// </summary>
    public class CardSelectionRequest
    {
        /// <summary>
        /// Card selector for the card selection.
        /// </summary>
        [JsonProperty("cardSelector")]
        public required CardSelector CardSelector { get; set; }

        /// <summary>
        /// Card request associated with the card selection.
        /// </summary>
        [JsonProperty("cardRequest")]
        public CardRequest? CardRequest { get; set; }
    }

    /// <summary>
    /// Represents parameters for transmitting card selection requests.
    /// </summary>
    public class TransmitCardSelectionRequestsParameters
    {
        /// <summary>
        /// Array of card selection requests.
        /// </summary>
        [JsonProperty("cardSelectionRequests")]
        public required CardSelectionRequest[] CardSelectionRequests { get; set; }

        /// <summary>
        /// Multi-selection processing mode.
        /// </summary>
        [JsonConverter(typeof(MultiSelectionProcessingConverter))]
        [JsonProperty("multiSelectionProcessing")]
        public required MultiSelectionProcessing MultiSelectionProcessing { get; set; }

        /// <summary>
        /// Channel control mode.
        /// </summary>
        [JsonConverter(typeof(ChannelControlConverter))]
        [JsonProperty("channelControl")]
        public required ChannelControl ChannelControl { get; set; }
    }

    /// <summary>
    /// Represents the body of a command. It is dedicated to the message identification step.
    /// </summary>
    public class CommandBody
    {
        /// <summary>
        /// Service name.
        /// </summary>
        [JsonProperty("service")]
        public required string Service { get; set; }
    }

    /// <summary>
    /// Represents the body of a transmit card selection requests command.
    /// </summary>
    public class TransmitCardSelectionRequestsCmdBody
    {
        /// <summary>
        /// Service name.
        /// </summary>
        [JsonProperty("service")]
        public required string Service { get; set; }

        /// <summary>
        /// Parameters for transmitting card selection requests.
        /// </summary>
        [JsonProperty("parameters")]
        public new required TransmitCardSelectionRequestsParameters Parameters { get; set; }
    }

    /// <summary>
    /// Represents the body of a transmit card request command.
    /// </summary>
    public class TransmitCardRequestCmdBody
    {
        /// <summary>
        /// Service name.
        /// </summary>
        [JsonProperty("service")]
        public required string Service { get; set; }

        /// <summary>
        /// Parameters for transmitting a card request.
        /// </summary>
        [JsonProperty("parameters")]
        public required TransmitCardRequestParameters Parameters { get; set; }
    }

    /// <summary>
    /// Represents the parameters for transmitting a card request.
    /// </summary>
    public class TransmitCardRequestParameters
    {
        /// <summary>
        /// Card request.
        /// </summary>
        [JsonProperty("cardRequest")]
        public required CardRequest CardRequest { get; set; }

        /// <summary>
        /// Channel control mode.
        /// </summary>
        [JsonConverter(typeof(ChannelControlConverter))]
        [JsonProperty("channelControl")]
        public required ChannelControl ChannelControl { get; set; }
    }
}

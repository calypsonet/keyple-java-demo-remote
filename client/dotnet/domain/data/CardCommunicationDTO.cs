using App.domain.utils;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace App.domain.data {
    public enum FileOccurrence {
        FIRST,
        LAST,
        NEXT,
        PREVIOUS,
    }

    public enum FileControlInformation {
        FCI,
        FCP,
        FMD,
        NO_RESPONSE,
    }
    public enum MultiSelectionProcessing {
        FIRST_MATCH,
        PROCESS_ALL,
    }

    public enum ChannelControl {
        KEEP_OPEN,
        CLOSE_AFTER,
    }

    public enum ErrorCode {
        READER_COMMUNICATION_ERROR,
        CARD_COMMUNICATION_ERROR,
        CARD_COMMAND_ERROR,
    }

    public class CardSelector {
        [JsonConverter ( typeof ( HexStringByteArrayConverter ) )]
        [JsonProperty ( "aid" )]
        public byte[] Aid { get; set; }

        [JsonConverter ( typeof ( FileOccurrenceConverter ) )]
        [JsonProperty ( "fileOccurrence" )]
        public FileOccurrence FileOccurrence { get; set; }

        [JsonConverter ( typeof ( FileControlInformationConverter ) )]
        [JsonProperty ( "fileControlInformation" )]
        public FileControlInformation FileControlInformation { get; set; }

        [JsonConverter ( typeof ( HexStringSetToIntHashSetConverter ) )]
        [JsonProperty ( "successfulSelectionStatusWords" )]
        public HashSet<int> SuccessfulSelectionStatusWords { get; set; }
    }

    public class ApduRequest {
        [JsonConverter ( typeof ( HexStringByteArrayConverter ) )]
        [JsonProperty ( "apdu" )]
        public byte[] Apdu { get; set; }

        [JsonConverter ( typeof ( HexStringSetToIntHashSetConverter ) )]
        [JsonProperty ( "successfulStatusWords" )]
        public HashSet<int> SuccessfulStatusWords { get; set; }

        [JsonProperty ( "info" )]
        public string Info { get; set; }
    }

    public class CardRequest {
        [JsonProperty ( "apduRequests" )]
        public List<ApduRequest> ApduRequests { get; set; }

        [JsonProperty ( "isStatusCodesVerificationEnabled" )]
        public bool IsStatusCodesVerificationEnabled { get; set; }
    }

    public class CardSelectionRequest {
        [JsonProperty ( "cardSelector" )]
        public CardSelector CardSelector { get; set; }

        [JsonProperty ( "cardRequest" )]
        public CardRequest CardRequest { get; set; }
    }

    public class TransmitCardSelectionRequestsParameters {
        [JsonProperty ( "cardSelectionRequests" )]
        public CardSelectionRequest[] CardSelectionRequests { get; set; }

        [JsonConverter ( typeof ( MultiSelectionProcessingConverter ) )]
        [JsonProperty ( "multiSelectionProcessing" )]
        public MultiSelectionProcessing MultiSelectionProcessing { get; set; }

        [JsonConverter ( typeof ( ChannelControlConverter ) )]
        [JsonProperty ( "channelControl" )]
        public ChannelControl ChannelControl { get; set; }
    }

    public class TransmitCardSelectionRequestsCmdBody {
        [JsonProperty ( "service" )]
        public string Service { get; set; }

        [JsonProperty ( "parameters" )]
        public TransmitCardSelectionRequestsParameters Parameters { get; set; }
    }

    public class ApduResponse {
        [JsonConverter ( typeof ( HexStringByteArrayConverter ) )]
        [JsonProperty ( "apdu" )]
        public byte[] Apdu { get; set; }

        [JsonConverter ( typeof ( HexStringToIntConverter ) )]
        [JsonProperty ( "statusWord" )]
        public int StatusWord { get; set; }
    }

    public class CardResponse {
        [JsonProperty ( "isLogicalChannelOpen" )]
        public bool IsLogicalChannelOpen { get; set; }

        [JsonProperty ( "apduResponses" )]
        public List<ApduResponse> ApduResponses { get; set; }
    }

    public class CardSelectionResponse {
        [JsonProperty ( "hasMatched" )]
        public bool HasMatched { get; set; }

        [JsonProperty ( "powerOnData" )]
        public string PowerOnData { get; set; }

        [JsonProperty ( "selectApplicationResponse" )]
        public ApduResponse SelectApplicationResponse { get; set; }

        [JsonProperty ( "cardResponse" )]
        public CardResponse CardResponse { get; set; }
    }

    public class Error {
        [JsonConverter ( typeof ( StringEnumConverter ) )]
        [JsonProperty ( "code" )]
        public ErrorCode Code { get; set; }

        [JsonProperty ( "message" )]
        public string Message { get; set; }
    }

    public class TransmitCardRequestParameters {
        [JsonProperty ( "cardRequest" )]
        public CardRequest CardRequest { get; set; }

        [JsonConverter ( typeof ( ChannelControlConverter ) )]
        [JsonProperty ( "channelControl" )]
        public ChannelControl ChannelControl { get; set; }
    }

    public class TransmitCardRequestCmdBody {
        [JsonProperty ( "service" )]
        public string Service { get; set; }

        [JsonProperty ( "parameters" )]
        public TransmitCardRequestParameters Parameters { get; set; }
    }

    public class TransmitCardRequestsRespBody {
        [JsonProperty ( "service" )]
        public string Service { get; set; }

        [JsonProperty ( "result" )]
        public CardResponse Result { get; set; }
    }

    public class TransmitCardRequestRespBodyError {
        [JsonProperty ( "service" )]
        public string Service { get; set; }

        [JsonProperty ( "error" )]
        public Error Error { get; set; }
    }

}

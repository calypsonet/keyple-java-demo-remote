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

namespace App.domain.data.command
{
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
    }
}

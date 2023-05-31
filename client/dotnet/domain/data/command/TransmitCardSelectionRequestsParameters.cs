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
}

// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

using Newtonsoft.Json;

namespace App.domain.data.response
{
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
}

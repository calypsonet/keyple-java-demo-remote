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
}

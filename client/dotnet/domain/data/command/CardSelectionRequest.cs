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

namespace App.domain.data.command
{
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
}

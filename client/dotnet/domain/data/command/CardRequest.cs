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
        [JsonProperty("stopOnUnsuccessfulStatusWord")]
        public bool StopOnUnsuccessfulStatusWord { get; set; }
    }
}

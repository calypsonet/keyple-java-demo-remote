﻿// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
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
}
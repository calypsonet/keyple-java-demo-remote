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
}

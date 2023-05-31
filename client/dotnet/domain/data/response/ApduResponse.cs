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

namespace App.domain.data.response
{
    /// <summary>
    /// Represents an APDU response.
    /// </summary>
    public class ApduResponse
    {
        /// <summary>
        /// APDU data.
        /// </summary>
        [JsonConverter(typeof(HexStringToByteArrayConverter))]
        [JsonProperty("apdu")]
        public required byte[] Apdu { get; set; }

        /// <summary>
        /// Status word.
        /// </summary>
        [JsonConverter(typeof(HexStringToIntConverter))]
        [JsonProperty("statusWord")]
        public int StatusWord { get; set; }
    }
}

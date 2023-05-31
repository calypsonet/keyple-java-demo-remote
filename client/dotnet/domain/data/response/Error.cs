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
using Newtonsoft.Json.Converters;

namespace App.domain.data.response
{
    /// <summary>
    /// Represents an error.
    /// </summary>
    public class Error
    {
        /// <summary>
        /// Error code.
        /// </summary>
        [JsonConverter(typeof(StringEnumConverter))]
        [JsonProperty("code")]
        public required ErrorCode Code { get; set; }

        /// <summary>
        /// Error message.
        /// </summary>
        [JsonProperty("message")]
        public required string Message { get; set; }
    }
}

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
    /// Represents the body of a transmit card selection requests command.
    /// </summary>
    public class TransmitCardSelectionRequestsCmdBody
    {
        /// <summary>
        /// Service name.
        /// </summary>
        [JsonProperty("service")]
        public required string Service { get; set; }

        /// <summary>
        /// Parameters for transmitting card selection requests.
        /// </summary>
        [JsonProperty("parameters")]
        public required TransmitCardSelectionRequestsParameters Parameters { get; set; }
    }
}

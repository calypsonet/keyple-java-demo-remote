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

namespace App.domain.data.executeremoteservice
{
    /// <summary>
    /// The body content of the EXECUTE_REMOTE_SERVICE message.
    /// </summary>
    class ExecuteRemoteServiceBody
    {
        /// <summary>
        /// Core API level.
        /// </summary>
        [JsonProperty("coreApiLevel")]
        public const int CoreApiLevel = ApiInfo.CORE_API_LEVEL;

        /// <summary>
        /// Service ID.
        /// </summary>
        [JsonProperty("serviceId")]
        public required string ServiceId { get; set; }

        /// <summary>
        /// Input data.
        /// </summary>
        [JsonProperty("inputData")]
        public InputData? InputData { get; set; }
    }
}

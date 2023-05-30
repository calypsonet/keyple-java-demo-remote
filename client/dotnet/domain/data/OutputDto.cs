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
/// <summary>
/// Output data used for the server operation result.
/// </summary>
class OutputData
{
    /// <summary>
    /// Gets or sets the contracts.
    /// </summary>
    [JsonProperty("items")]
    public required List<string> Items { get; set; }

    /// <summary>
    /// Gets or sets the status code.
    /// </summary>
    [JsonProperty("statusCode")]
    public int StatusCode { get; set; }

    /// <summary>
    /// Gets or sets the message.
    /// </summary>
    [JsonProperty("message")]
    public string Message{ get; set; }
}

class OutputDto
{
    /// <summary>
    /// Gets or sets the OutputData.
    /// </summary>
    [JsonProperty("outputData")]
    public required OutputData OutputData { get; set; }
}

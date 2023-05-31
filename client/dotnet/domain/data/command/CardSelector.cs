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
    /// Card selector used for card selection.
    /// </summary>
    /// <param name="CardProtocol"> Card protocol. </param>
    /// <param name="PowerOnDataRegex"> Power On Data regular expression filter. </param>
    /// <param name="Aid"> Application Identifier (AID) of the card. </param>
    /// <param name="FileOccurrence"> File occurrence. </param>
    /// <param name="FileControlInformation"> File control information. </param>
    /// <param name="SuccessfulSelectionStatusWords"> Successful status words of the selection application command. </param>
    public record CardSelector([property: JsonProperty("cardProtocol")] string? CardProtocol, [property: JsonProperty("powerOnDataRegex")] string? PowerOnDataRegex, [property: JsonConverter(typeof(HexStringToByteArrayConverter))][property: JsonProperty("aid")] byte[]? Aid, [property: JsonConverter(typeof(FileOccurrenceConverter))][property: JsonProperty("fileOccurrence")] FileOccurrence FileOccurrence, [property: JsonConverter(typeof(FileControlInformationConverter))][property: JsonProperty("fileControlInformation")] FileControlInformation FileControlInformation, [property: JsonConverter(typeof(HexStringToSetToIntHashSetConverter))][property: JsonProperty("successfulSelectionStatusWords")] HashSet<int> SuccessfulSelectionStatusWords);
}

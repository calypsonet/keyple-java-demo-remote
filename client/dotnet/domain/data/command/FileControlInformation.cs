// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

namespace App.domain.data.command
{
    /// <summary>
    /// File information output format.
    /// </summary>
    public enum FileControlInformation
    {
        /// <summary>
        /// File Control Information (FCI) format.
        /// </summary>
        FCI,

        /// <summary>
        /// File Control Parameters (FCP) format.
        /// </summary>
        FCP,

        /// <summary>
        /// File Management Data (FMD) format.
        /// </summary>
        FMD,

        /// <summary>
        /// No response format.
        /// </summary>
        NO_RESPONSE,
    }
}

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
    /// File occurrence selection strategy.
    /// </summary>
    public enum FileOccurrence
    {
        /// <summary>
        /// Select the first occurrence.
        /// </summary>
        FIRST,

        /// <summary>
        /// Select the last occurrence.
        /// </summary>
        LAST,

        /// <summary>
        /// Select the next occurrence.
        /// </summary>
        NEXT,

        /// <summary>
        /// Select the previous occurrence.
        /// </summary>
        PREVIOUS,
    }
}

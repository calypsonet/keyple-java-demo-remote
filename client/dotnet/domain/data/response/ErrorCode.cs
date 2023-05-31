// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

namespace App.domain.data.response
{

    /// <summary>
    /// Error codes.
    /// </summary>
    public enum ErrorCode
    {
        /// <summary>
        /// Reader communication error.
        /// </summary>
        READER_COMMUNICATION_ERROR,

        /// <summary>
        /// Card communication error.
        /// </summary>
        CARD_COMMUNICATION_ERROR,

        /// <summary>
        /// Card command error.
        /// </summary>
        CARD_COMMAND_ERROR,
    }
}

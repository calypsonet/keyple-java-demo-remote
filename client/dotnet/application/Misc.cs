// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

using Serilog;
using Serilog.Events;

namespace App.application
{
    /// <summary>
    /// The Misc class provides utility methods that are used across the application.
    /// </summary>
    public class Misc
    {
        /// <summary>
        /// Displays the text in the specified color on the console and logs it with the specified log level using the provided logger.
        /// </summary>
        /// <param name="text">The text to be displayed and logged.</param>
        /// <param name="color">The color in which the text will be displayed on the console.</param>
        /// <param name="logLevel">The log level at which the text will be logged.</param>
        /// <param name="logger">The logger instance to use for logging.</param>
        public static void DisplayAndLog(string text, ConsoleColor color, LogEventLevel logLevel, ILogger logger)
        {
            Console.ForegroundColor = color;
            Console.WriteLine(text);
            Console.ResetColor();
            logger.Write(logLevel, text);
        }
    }
}

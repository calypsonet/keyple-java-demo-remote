// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

using App.domain.api;
using Newtonsoft.Json;
using Serilog;
using Serilog.Events;

namespace App.application
{

    /// <summary>
    /// The Application class coordinates the main operations of the application.
    /// </summary>
    public class Application
    {
        private readonly ILogger _logger;

        // Service API to interact with the main service
        private readonly MainServiceApi _mainService;

        /// <summary>
        /// Application constructor.
        /// </summary>
        /// <param name="mainService">Main service API</param>
        public Application(MainServiceApi mainService)
        {
            // Initialize logger for this context
            _logger = Log.ForContext<Application>();

            // Assign main service instance
            _mainService = mainService;
        }

        /// <summary>
        /// Start the main execution of the application.
        /// </summary>
        public void Start()
        {
            DisplayAndLog("Waiting for a card...", ConsoleColor.DarkBlue, LogEventLevel.Information, _logger);

            _mainService.WaitForCardInsertion();

            DisplayAndLog("Card found!\nContacting server...", ConsoleColor.Green, LogEventLevel.Information, _logger);

            // Select the card and read its content
            string output = _mainService.SelectCardAndReadContracts();

            // Deserialize the output into a dynamic object
            EndRemoteServiceBody outputDto = JsonConvert.DeserializeObject<EndRemoteServiceBody>(output)!;

            if (outputDto.OutputData.StatusCode != 0)
            {
                DisplayAndLog("Reading failed, status code: " + outputDto.OutputData.StatusCode, ConsoleColor.DarkRed, LogEventLevel.Information, _logger);
                DisplayAndLog(outputDto.OutputData.Message, ConsoleColor.DarkRed, LogEventLevel.Information, _logger);
                return;
            }

            DisplayAndLog("Reading successful", ConsoleColor.DarkGreen, LogEventLevel.Information, _logger);

            List<string> contracts = outputDto.OutputData.Items;
            int i = 1;
            bool multitripContractPresent = false;
            foreach (string contract in contracts)
            {
                // Print each item to the console
                DisplayAndLog($"Contract #{i++}:\r\n" + contract, ConsoleColor.DarkGreen, LogEventLevel.Information, _logger);
                if (contract.Contains("MULTI_TRIP"))
                {
                    multitripContractPresent = true;
                }
            }

            if (!multitripContractPresent)
            {
                DisplayAndLog("No MULTI_TRIP contract found.", ConsoleColor.DarkRed, LogEventLevel.Information, _logger);
            }

            int nbUnits;
            do
            {
                Console.Write("Please enter a number of units between 1 and 20: ");
                string input = Console.ReadLine()!;

                if (!int.TryParse(input, out nbUnits) || nbUnits < 1 || nbUnits > 20)
                {
                    Console.WriteLine("Error: please enter a valid number of units between 1 and 20.");
                }
            } while (nbUnits < 1 || nbUnits > 20);

            DisplayAndLog($"The card will now be (re)loaded with {nbUnits} unit(s).", ConsoleColor.DarkCyan, LogEventLevel.Information, _logger);
            DisplayAndLog("Waiting for a card...", ConsoleColor.DarkBlue, LogEventLevel.Information, _logger);

            _mainService.WaitForCardInsertion();

            DisplayAndLog("Card found!\nContacting server...", ConsoleColor.Green, LogEventLevel.Information, _logger);

            // Select the card and increase the contract counter by one unit
            output = _mainService.SelectCardAndIncreaseContractCounter(nbUnits);

            outputDto = JsonConvert.DeserializeObject<EndRemoteServiceBody>(output)!;

            if (outputDto.OutputData.StatusCode == 0)
            {
                DisplayAndLog("Reloading successful", ConsoleColor.DarkGreen, LogEventLevel.Information, _logger);
            }
            else
            {
                DisplayAndLog("Reloading failed, status code: " + outputDto.OutputData.StatusCode, ConsoleColor.DarkRed, LogEventLevel.Information, _logger);
                DisplayAndLog(outputDto.OutputData.Message, ConsoleColor.DarkRed, LogEventLevel.Information, _logger);
            }
        }

        /// <summary>
        /// Displays the text in the specified color on the console and logs it with the specified log level using the provided logger.
        /// </summary>
        /// <param name="text">The text to be displayed and logged.</param>
        /// <param name="color">The color in which the text will be displayed on the console.</param>
        /// <param name="logLevel">The log level at which the text will be logged.</param>
        /// <param name="logger">The logger instance to use for logging.</param>
        private void DisplayAndLog(string text, ConsoleColor color, LogEventLevel logLevel, ILogger logger)
        {
            Console.ForegroundColor = color;
            Console.WriteLine(text);
            Console.ResetColor();
            logger.Write(logLevel, text);
        }
    }
}

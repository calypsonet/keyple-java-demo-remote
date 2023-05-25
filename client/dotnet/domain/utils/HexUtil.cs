// Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
//
// See the NOTICE file(s) distributed with this work for additional information
// regarding copyright ownership.
//
// This program and the accompanying materials are made available under the terms of the
// Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
//
// SPDX-License-Identifier: EPL-2.0

using System.Text;

namespace App.domain.utils
{
    /// <summary>
    /// Provides a utility to convert a byte array to a hexadecimal string.
    /// </summary>
    public static class HexUtil
    {
        /// <summary>
        /// A precomputed table of hexadecimal representations of bytes.
        /// </summary>
        private static readonly string[] s_byteToHexTable = new string[]
        {
        "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
        "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
        "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
        "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
        "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
        "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
        "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
        "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
        "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
        "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
        "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"
        };

        /// <summary>
        /// A readonly byte array used for converting hexadecimal characters to their corresponding
        /// nibble (half-byte) values.
        /// </summary>
        private static readonly byte[] s_hexToNibble = InitializeHexToNibble();

        /// <summary>
        /// Converts a byte array to a hexadecimal string.
        /// </summary>
        /// <param name="byteArray">The byte array to convert.</param>
        /// <returns>A hexadecimal string representing the byte array.</returns>
        public static string ToHex(byte[] byteArray)
        {
            StringBuilder hexBuilder = new StringBuilder(byteArray.Length * 2);
            foreach (byte b in byteArray)
            {
                hexBuilder.Append(s_byteToHexTable[b]);
            }
            return hexBuilder.ToString();
        }

        /// <summary>
        /// Initializes the hexToNibble byte array with the appropriate nibble values for each
        /// hexadecimal character.
        /// </summary>
        /// <returns>A byte array with the nibble values for each hexadecimal character.</returns>
        private static byte[] InitializeHexToNibble()
        {
            byte[] hexToNibble = new byte[256];
            Array.Fill(hexToNibble, (byte)0xFF);
            hexToNibble['0'] = 0x0;
            hexToNibble['1'] = 0x1;
            hexToNibble['2'] = 0x2;
            hexToNibble['3'] = 0x3;
            hexToNibble['4'] = 0x4;
            hexToNibble['5'] = 0x5;
            hexToNibble['6'] = 0x6;
            hexToNibble['7'] = 0x7;
            hexToNibble['8'] = 0x8;
            hexToNibble['9'] = 0x9;
            hexToNibble['A'] = 0xA;
            hexToNibble['a'] = 0xA;
            hexToNibble['B'] = 0xB;
            hexToNibble['b'] = 0xB;
            hexToNibble['C'] = 0xC;
            hexToNibble['c'] = 0xC;
            hexToNibble['D'] = 0xD;
            hexToNibble['d'] = 0xD;
            hexToNibble['E'] = 0xE;
            hexToNibble['e'] = 0xE;
            hexToNibble['F'] = 0xF;
            hexToNibble['f'] = 0xF;
            return hexToNibble;
        }

        /// <summary>
        /// Converts a hexadecimal string to a byte array.
        /// </summary>
        /// <remarks>
        /// Caution: the result may be erroneous if the string does not contain only hexadecimal characters.
        /// </remarks>
        /// <param name="hex">The hexadecimal string to convert.</param>
        /// <returns>An empty byte array if the input string is null or empty.</returns>
        /// <exception cref="ArgumentOutOfRangeException">If the input string is made of an odd number of characters.</exception>
        public static byte[] ToByteArray(string hex)
        {
            if (string.IsNullOrEmpty(hex))
            {
                return new byte[0];
            }

            if (hex.Length % 2 != 0)
            {
                throw new ArgumentOutOfRangeException("hex", "Input string must have an even number of characters.");
            }

            byte[] byteArray = new byte[hex.Length / 2];
            for (int i = 0; i < hex.Length; i += 2)
            {
                byteArray[i / 2] = (byte)((s_hexToNibble[hex[i]] << 4) + (s_hexToNibble[hex[i + 1]] & 0xFF));
            }
            return byteArray;
        }
    }
}

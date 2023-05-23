using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Versioning;
using System.Text;
using System.Threading.Tasks;

namespace App.domain.spi {
    public interface ReaderServiceSpi {

        /// <summary>
        /// Retrieves a list of available smart card readers.
        /// </summary>
        /// <returns>A list of reader names.</returns>
        public List<string> GetReaders ( );

        /// <summary>
        /// Selects the reader to work with.
        /// </summary>
        /// <param name="readerName">The name of the reader.</param>
        void SelectReader ( string readerName );

        /// <summary>
        /// Checks if a card is present in the reader.
        /// </summary>
        /// <returns>True if a card is present, otherwise false.</returns>
        bool IsCardPresent ( );

        /// <summary>
        /// Waits for a card to be inserted in the reader.
        /// </summary>
        /// <returns>True if a card is detected, otherwise false.</returns>
        bool WaitForCardPresent ( );

        /// <summary>
        /// Waits for thr card to be removed in the reader.
        /// </summary>
        /// <returns>True if a card is removed, otherwise false.</returns>
        bool WaitForCardAbsent ( );

        /// <summary>
        /// Attempts to open the physical channel (to establish communication with the card).
        /// </summary>
        /// <exception cref="ReaderIOException">If the communication with the reader has failed.</exception>
        /// <exception cref="CardIOException">If the communication with the card has failed.</exception>
        void OpenPhysicalChannel ( );

        /// <summary>
        /// Attempts to close the current physical channel.
        ///
        /// The physical channel may have been implicitly closed previously by a card withdrawal.
        /// </summary>
        /// <exception cref="ReaderIOException">If the communication with the reader has failed.</exception>
        void ClosePhysicalChannel ( );

        /// <summary>
        /// Gets the power-on data.
        /// 
        /// The power-on data is defined as the data retrieved by the reader when the card is inserted.
        /// 
        /// In the case of a contact reader, this is the Answer To Reset data (ATR) defined by ISO7816.
        /// 
        /// In the case of a contactless reader, the reader decides what this data is. Contactless
        /// readers provide a virtual ATR (partially standardized by the PC/SC standard), but other devices
        /// can have their own definition, including for example elements from the anti-collision stage of
        /// the ISO14443 protocol (ATQA, ATQB, ATS, SAK, etc).
        /// 
        /// These data being variable from one reader to another, they are defined here in string format
        /// which can be either a hexadecimal string or any other relevant information.
        /// </summary>
        /// <returns>A not empty string.</returns>
        string GetPowerOnData ( );


        /// <summary>
        /// Transmits an Application Protocol Data Unit (APDU) command to the smart card and receives the response.
        /// </summary>
        /// <param name="commandApdu">The command APDU to be transmitted.</param>
        /// <returns>The response APDU received from the smart card.</returns>
        byte[] TransmitApdu ( byte[] commandApdu );
    }
}

/// <summary>
/// Defines the operations that the main service API must support. 
/// These operations primarily involve interacting with a card, where the card transaction
/// is driven by the server provided to the implementation of this interface.
/// </summary>
namespace App.domain.api {
    public interface MainServiceApi {

        /// <summary>
        /// Blocks until a card is inserted. 
        /// </summary>
        void WaitForCardInsertion ( );

        /// <summary>
        /// Blocks until a card is removed. 
        /// </summary>
        void WaitForCardRemoval ( );

        /// <summary>
        /// Selects the card and reads contracts.
        /// </summary>
        /// <returns>A string representation of the contracts read from the card.</returns>
        string SelectAndReadContracts ( );

        /// <summary>
        /// Selects the card and increases the counter of its MULTI_TRIP contract if it exists.
        /// </summary>
        /// <param name="counterIncrement">The amount by which to increase the contract's counter.</param>
        /// <returns>A string representation of the result of the operation.</returns>
        string SelectAndIncreaseContractCounter ( int counterIncrement );
    }
}

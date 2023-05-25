using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace App.domain.data
{
    /// <summary>
    /// Abstract base class for input data.
    /// </summary>
    abstract class InputData
    {
    }

    /// <summary>
    /// Input data used for the read step.
    /// Currently empty.
    /// </summary>
    class InputDataRead : InputData
    {
    }


    /// <summary>
    /// Input data used for the write step.
    /// Currently empty.
    /// </summary>
    class InputDataWrite : InputData
    {
        [JsonProperty("counterIncrement")]
        public required string CounterIncrement { get; set; }
    }

    /// <summary>
    /// The body content of the EXECUTE_REMOTE_SERVICE message.
    /// </summary>
    class ExecuteRemoteServiceBody
    {
        [JsonProperty("serviceId")]
        public required string ServiceId { get; set; }

        [JsonProperty("inputData")]
        public InputData? InputData { get; set; }
    }
}

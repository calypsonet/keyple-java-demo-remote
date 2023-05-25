using Newtonsoft.Json;
/// <summary>
/// Output data used for the server operation result.
/// </summary>
class OutputData
{
    /// <summary>
    /// Gets or sets the contracts.
    /// </summary>
    [JsonProperty("items")]
    public required List<string> Items { get; set; }

    /// <summary>
    /// Gets or sets the status code.
    /// </summary>
    [JsonProperty("statusCode")]
    public int StatusCode { get; set; }
}

class OutputDto
{
    /// <summary>
    /// Gets or sets the OutputData.
    /// </summary>
    [JsonProperty("outputData")]
    public required OutputData OutputData { get; set; }
}

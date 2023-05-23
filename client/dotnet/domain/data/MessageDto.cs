using Newtonsoft.Json;

/// <summary>
/// MessageDto is a data transfer object for representing messages exchanges with the Keyple ticketing server.
/// </summary>
public class MessageDto {

    [JsonProperty ( "action" )]
    public string Action { get; private set; }

    [JsonProperty ( "body" )]
    public string Body { get; private set; }

    [JsonProperty ( "clientNodeId" )]
    public string ClientNodeId { get; private set; }

    [JsonProperty ( "localReaderName" )]
    public string LocalReaderName { get; private set; }

    [JsonProperty ( "remoteReaderName" )]
    public string RemoteReaderName { get; private set; }

    [JsonProperty ( "serverNodeId" )]
    public string ServerNodeId { get; private set; }

    [JsonProperty ( "sessionId" )]
    public string SessionId { get; private set; }

    public MessageDto SetAction ( string action )
    {
        this.Action = action;
        return this;
    }

    public MessageDto SetBody ( string body )
    {
        this.Body = body;
        return this;
    }

    public MessageDto SetClientNodeId ( string clientNodeId )
    {
        this.ClientNodeId = clientNodeId;
        return this;
    }

    public MessageDto SetLocalReaderName ( string localReaderName )
    {
        this.LocalReaderName = localReaderName;
        return this;
    }

    public MessageDto SetRemoteReaderName ( string remoteReaderName )
    {
        this.RemoteReaderName = remoteReaderName;
        return this;
    }

    public MessageDto SetServerNodeId ( string serverNodeId )
    {
        this.ServerNodeId = serverNodeId;
        return this;
    }

    public MessageDto SetSessionId ( string sessionId )
    {
        this.SessionId = sessionId;
        return this;
    }
}

/// <summary>
/// Abstract base class for input data.
/// </summary>
abstract class InputData {
}

/// <summary>
/// Input data used for the read step.
/// Currently empty.
/// </summary>
class InputDataRead : InputData {
}


/// <summary>
/// Input data used for the write step.
/// Currently empty.
/// </summary>
class InputDataWrite : InputData {
    [JsonProperty ( "counterIncrement" )]
    public string CounterIncrement { get; set; }
}

/// <summary>
/// Output data used for the server operation result.
/// </summary>
class OutputData {
    /// <summary>
    /// Gets or sets the contracts.
    /// </summary>
    [JsonProperty ( "items" )]
    public List<string> Items { get; set; }

    /// <summary>
    /// Gets or sets the status code.
    /// </summary>
    [JsonProperty ( "statusCode" )]
    public int StatusCode { get; set; }
}

class OutputDto {
    /// <summary>
    /// Gets or sets the OutputData.
    /// </summary>
    [JsonProperty ( "outputData" )]
    public OutputData OutputData { get; set; }
}


/// <summary>
/// Represents the body content of the EXECUTE_REMOTE_SERVICE message.
/// </summary>
class ExecuteRemoteServiceBodyContent {
    [JsonProperty ( "serviceId" )]
    public string ServiceId { get; set; }

    [JsonProperty ( "inputData" )]
    public InputData InputData { get; set; }
}
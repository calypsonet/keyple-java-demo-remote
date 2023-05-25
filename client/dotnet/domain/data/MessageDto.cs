using Newtonsoft.Json;

/// <summary>
/// MessageDto is a data transfer object for representing messages exchanged with the Keyple ticketing server.
/// </summary>
public class MessageDto
{

    /// <summary>
    /// Action associated with the message.
    /// </summary>
    [JsonProperty("action")]
    public string Action { get; set; }

    /// <summary>
    /// Body of the message.
    /// </summary>
    [JsonProperty("body")]
    public string Body { get; set; }

    /// <summary>
    /// Client node ID.
    /// </summary>
    [JsonProperty("clientNodeId")]
    public string ClientNodeId { get; set; }

    /// <summary>
    /// Name of the local reader.
    /// </summary>
    [JsonProperty("localReaderName")]
    public string? LocalReaderName { get; set; }

    /// <summary>
    /// Name of the remote reader.
    /// </summary>
    [JsonProperty("remoteReaderName")]
    public string? RemoteReaderName { get; set; }

    /// <summary>
    /// Server node ID.
    /// </summary>
    [JsonProperty("serverNodeId")]
    public string? ServerNodeId { get; set; }

    /// <summary>
    /// Session ID.
    /// </summary>
    [JsonProperty("sessionId")]
    public string SessionId { get; set; }

    /// <summary>
    /// Sets the action associated with the message.
    /// </summary>
    /// <param name="action">The action to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetAction(string action)
    {
        this.Action = action;
        return this;
    }

    /// <summary>
    /// Sets the body of the message.
    /// </summary>
    /// <param name="body">The body to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetBody(string body)
    {
        this.Body = body;
        return this;
    }

    /// <summary>
    /// Sets the client node ID.
    /// </summary>
    /// <param name="clientNodeId">The client node ID to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetClientNodeId(string clientNodeId)
    {
        this.ClientNodeId = clientNodeId;
        return this;
    }

    /// <summary>
    /// Sets the name of the local reader.
    /// </summary>
    /// <param name="localReaderName">The name of the local reader to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetLocalReaderName(string localReaderName)
    {
        this.LocalReaderName = localReaderName;
        return this;
    }

    /// <summary>
    /// Sets the name of the remote reader.
    /// </summary>
    /// <param name="remoteReaderName">The name of the remote reader to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetRemoteReaderName(string remoteReaderName)
    {
        this.RemoteReaderName = remoteReaderName;
        return this;
    }

    /// <summary>
    /// Sets the server node ID.
    /// </summary>
    /// <param name="serverNodeId">The server node ID to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetServerNodeId(string serverNodeId)
    {
        this.ServerNodeId = serverNodeId;
        return this;
    }

    /// <summary>
    /// Sets the session ID.
    /// </summary>
    /// <param name="sessionId">The session ID to set.</param>
    /// <returns>The updated <see cref="MessageDto"/> instance.</returns>
    public MessageDto SetSessionId(string sessionId)
    {
        this.SessionId = sessionId;
        return this;
    }
}

// Bootstrap
using System.Configuration;
using System.IO;
using App.application;
using App.domain.api;
using App.domain.spi;
using App.infrastructure.pcscreader;
using App.infrastructure.server;
using Microsoft.Extensions.Configuration;
using Serilog;


// Create and configure the logger
Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Debug()
    .Enrich.FromLogContext()
    .WriteTo.File("logs/demo-keyple-less-.log", rollingInterval: RollingInterval.Day, retainedFileCountLimit: 7)
    .CreateLogger();

Log.Information("Starting the application");
try
{
    Log.Information("Loading paramters...");

    IConfigurationBuilder builder = new ConfigurationBuilder()
        .AddJsonFile(Path.Combine(Directory.GetCurrentDirectory(), "appsettings.json"), optional: false, reloadOnChange: false);

    // Load configuration data
    IConfigurationRoot configuration = builder.Build();

    string? serverHost = configuration.GetSection("Server")?["host"];
    int serverPort = int.TryParse(configuration.GetSection("Server")?["port"], out int parsedPort) ? parsedPort : -1;
    string? serverEndpoint = configuration.GetSection("Server")?["endpoint"];
    string? readerName = configuration.GetSection("Reader")?["name"];

    // Handle null or missing values as needed
    if (serverHost == null)
    {
        throw new ConfigurationErrorsException("Server host is missing or null.");
    }

    if (serverPort == -1)
    {
        throw new ConfigurationErrorsException("Server port is missing or not a valid integer.");
    }

    if (serverEndpoint == null)
    {
        throw new ConfigurationErrorsException("Server endpoint is missing or null.");
    }

    if (readerName == null)
    {
        throw new ConfigurationErrorsException("Reader name is missing or null.");
    }


    Log.Information("Retrieve reader and server connectors...");

    // Get an instance of the ReaderServiceSpi implementation
    ReaderServiceSpi cardService = PcscReaderServiceSpiProvider.getInstance();

    // Get an instance of the ServerSpi implementation
    ServerSpi server = ServerSpiProvider.getInstance(serverHost, serverPort, serverEndpoint);

    // Create an instance of the MainServiceApi using the reader service and server
    MainServiceApi mainService = MainServiceApiProvider.getService(cardService, readerName, server);

    Log.Information("Create and start application...");

    // Create an instance of the application and start it
    Application app = new Application(mainService);
    app.Start();
}
catch (Exception ex)
{
    Console.Error.WriteLine("Error while running the application (see log file for details).");
    Console.Error.WriteLine(ex.Message);
    Log.Fatal(ex, "The application failed.");
}
finally
{
    Log.Information("Closing the application");
    Log.CloseAndFlush();
}

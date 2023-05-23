using App.domain.data;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace App.domain.utils {
    /// <summary>
    /// Provides a converter for JSON serialization and deserialization between hexadecimal strings and byte arrays.
    /// </summary>
    public class HexStringByteArrayConverter : JsonConverter {
        /// <summary>
        /// Checks if the provided type can be converted. In this case, it checks if the type is byte[].
        /// </summary>
        /// <param name="objectType">Type of the object to check.</param>
        /// <returns>True if the type can be converted; otherwise, false.</returns>
        public override bool CanConvert ( Type objectType )
        {
            return objectType == typeof ( byte[] );
        }

        /// <summary>
        /// Reads a JSON object and converts it into a byte array.
        /// </summary>
        /// <param name="reader">The JsonReader to read from.</param>
        /// <param name="objectType">Type of the object to convert.</param>
        /// <param name="existingValue">The existing value of the object being read.</param>
        /// <param name="serializer">The calling JsonSerializer.</param>
        /// <returns>A byte array that represents the JSON object.</returns>
        public override object ReadJson ( JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer )
        {
            var hexString = (string)reader.Value;
            return StringToByteArray ( hexString );
        }

        /// <summary>
        /// Writes a byte array as a hexadecimal string into a JSON object.
        /// </summary>
        /// <param name="writer">The JsonWriter to write to.</param>
        /// <param name="value">The value to convert.</param>
        /// <param name="serializer">The calling JsonSerializer.</param>
        public override void WriteJson ( JsonWriter writer, object value, JsonSerializer serializer )
        {
            byte[] bytes = (byte[])value;
            writer.WriteValue ( BitConverter.ToString ( bytes ).Replace ( "-", "" ) );
        }

        /// <summary>
        /// Converts a hexadecimal string into a byte array.
        /// </summary>
        /// <param name="hex">The hexadecimal string to convert.</param>
        /// <returns>A byte array that represents the hexadecimal string.</returns>
        public static byte[] StringToByteArray ( string hex )
        {
            return HexUtil.ToByteArray ( hex );
        }
    }


    /// <summary>
    /// Provides a converter for JSON serialization and deserialization between hexadecimal strings and HashSet of integers.
    /// </summary>
    public class HexStringSetToIntHashSetConverter : JsonConverter {
        /// <summary>
        /// Checks if the provided type can be converted. In this case, it checks if the type is HashSetlt;int&gt;.
        /// </summary>
        /// <param name="objectType">Type of the object to check.</param>
        /// <returns>True if the type can be converted; otherwise, false.</returns>
        public override bool CanConvert ( Type objectType )
        {
            return objectType == typeof ( HashSet<int> );
        }

        /// <summary>
        /// Reads a JSON object and converts it into a HashSet of integers.
        /// </summary>
        /// <param name="reader">The JsonReader to read from.</param>
        /// <param name="objectType">Type of the object to convert.</param>
        /// <param name="existingValue">The existing value of the object being read.</param>
        /// <param name="serializer">The calling JsonSerializer.</param>
        /// <returns>A HashSet of integers that represents the JSON object.</returns>
        public override object ReadJson ( JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer )
        {
            var hexStrings = serializer.Deserialize<List<string>> ( reader );
            return new HashSet<int> ( hexStrings.Select ( s => Convert.ToInt32 ( s, 16 ) ) );
        }

        /// <summary>
        /// Writes a HashSet of integers as a list of hexadecimal strings into a JSON object.
        /// </summary>
        /// <param name="writer">The JsonWriter to write to.</param>
        /// <param name="value">The value to convert.</param>
        /// <param name="serializer">The calling JsonSerializer.</param>
        public override void WriteJson ( JsonWriter writer, object value, JsonSerializer serializer )
        {
            var ints = (HashSet<int>)value;
            var hexStrings = ints.Select ( i => i.ToString ( "X4" ) ).ToList ();
            serializer.Serialize ( writer, hexStrings );
        }
    }

    /// <summary>
    /// A converter class to handle JSON serialization and deserialization for FileOccurrence enum.
    /// </summary>
    public class FileOccurrenceConverter : JsonConverter {

        /// <summary>
        /// Checks if the provided type can be converted by this converter.
        /// </summary>
        /// <returns>
        /// true if the objectType is of type FileOccurrence, otherwise false.
        /// </returns>
        public override bool CanConvert ( Type objectType )
        {
            return objectType == typeof ( FileOccurrence );
        }

        /// <summary>
        /// Converts the JSON string to a FileOccurrence enum.
        /// </summary>
        /// <returns>
        /// A FileOccurrence enum parsed from the string value.
        /// </returns>
        public override object ReadJson ( JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer )
        {
            var str = (string)reader.Value;
            return Enum.Parse ( typeof ( FileOccurrence ), str, true );
        }

        /// <summary>
        /// Converts the FileOccurrence enum to a JSON string.
        /// </summary>
        public override void WriteJson ( JsonWriter writer, object value, JsonSerializer serializer )
        {
            writer.WriteValue ( value.ToString () );
        }
    }

    /// <summary>
    /// A converter class to handle JSON serialization and deserialization for FileControlInformation enum.
    /// </summary>
    public class FileControlInformationConverter : JsonConverter {

        /// <summary>
        /// Checks if the provided type can be converted by this converter.
        /// </summary>
        /// <returns>
        /// true if the objectType is of type FileControlInformation, otherwise false.
        /// </returns>
        public override bool CanConvert ( Type objectType )
        {
            return objectType == typeof ( FileControlInformation );
        }

        /// <summary>
        /// Converts the JSON string to a FileControlInformation enum.
        /// </summary>
        /// <returns>
        /// A FileControlInformation enum parsed from the string value.
        /// </returns>
        public override object ReadJson ( JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer )
        {
            var str = (string)reader.Value;
            return Enum.Parse ( typeof ( FileControlInformation ), str, true );
        }

        /// <summary>
        /// Converts the FileControlInformation enum to a JSON string.
        /// </summary>
        public override void WriteJson ( JsonWriter writer, object value, JsonSerializer serializer )
        {
            writer.WriteValue ( value.ToString () );
        }
    }

    /// <summary>
    /// A converter class that handles JSON serialization and deserialization for MultiSelectionProcessing enumeration.
    /// </summary>
    public class MultiSelectionProcessingConverter : JsonConverter {

        /// <summary>
        /// Determines whether the current converter instance can convert the specified object type.
        /// </summary>
        /// <returns>
        /// true if the objectType is of type MultiSelectionProcessing, otherwise false.
        /// </returns>
        public override bool CanConvert ( Type objectType )
        {
            return objectType == typeof ( MultiSelectionProcessing );
        }

        /// <summary>
        /// Reads the JSON representation of the object and converts it into a MultiSelectionProcessing enumeration.
        /// </summary>
        /// <returns>
        /// A MultiSelectionProcessing enumeration instance parsed from the provided string value.
        /// </returns>
        public override object ReadJson ( JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer )
        {
            var str = (string)reader.Value;
            return Enum.Parse ( typeof ( MultiSelectionProcessing ), str, true );
        }

        /// <summary>
        /// Writes the JSON representation of the object by converting the MultiSelectionProcessing enumeration into a string.
        /// </summary>
        public override void WriteJson ( JsonWriter writer, object value, JsonSerializer serializer )
        {
            writer.WriteValue ( value.ToString () );
        }
    }

    /// <summary>
    /// A converter class that handles JSON serialization and deserialization for ChannelControl enumeration.
    /// </summary>
    public class ChannelControlConverter : JsonConverter {

        /// <summary>
        /// Determines whether the current converter instance can convert the specified object type.
        /// </summary>
        /// <param name="objectType">The type of the object to be checked.</param>
        /// <returns>
        /// true if the objectType is of type ChannelControl, otherwise false.
        /// </returns>
        public override bool CanConvert ( Type objectType )
        {
            return objectType == typeof ( ChannelControl );
        }

        /// <summary>
        /// Reads the JSON representation of the object and converts it into a ChannelControl enumeration.
        /// </summary>
        /// <param name="reader">The JsonReader to read from.</param>
        /// <param name="objectType">Type of the object.</param>
        /// <param name="existingValue">The existing value of object being read.</param>
        /// <param name="serializer">The JsonSerializer that is calling this method.</param>
        /// <returns>
        /// A ChannelControl enumeration instance parsed from the provided string value.
        /// </returns>
        public override object ReadJson ( JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer )
        {
            var str = (string)reader.Value;
            return Enum.Parse ( typeof ( ChannelControl ), str, true );
        }

        /// <summary>
        /// Writes the JSON representation of the object by converting the ChannelControl enumeration into a string.
        /// </summary>
        /// <param name="writer">The JsonWriter to write to.</param>
        /// <param name="value">The object value to convert and write as JSON.</param>
        /// <param name="serializer">The JsonSerializer that is calling this method.</param>
        public override void WriteJson ( JsonWriter writer, object value, JsonSerializer serializer )
        {
            writer.WriteValue ( value.ToString () );
        }
    }


    /// <summary>
    /// A converter class that handles JSON serialization and deserialization between hexadecimal strings and integers.
    /// </summary>
    public class HexStringToIntConverter : JsonConverter {

        /// <summary>
        /// Determines whether the current converter instance can convert the specified object type.
        /// </summary>
        /// <param name="objectType">The type of the object to be checked.</param>
        /// <returns>
        /// true if the objectType is of type int, otherwise false.
        /// </returns>
        public override bool CanConvert ( Type objectType )
        {
            return objectType == typeof ( int );
        }

        /// <summary>
        /// Reads the JSON representation of the hexadecimal string and converts it into an integer.
        /// </summary>
        /// <param name="reader">The JsonReader to read from.</param>
        /// <param name="objectType">Type of the object.</param>
        /// <param name="existingValue">The existing value of object being read.</param>
        /// <param name="serializer">The JsonSerializer that is calling this method.</param>
        /// <returns>
        /// An integer parsed from the provided hexadecimal string value.
        /// </returns>
        public override object ReadJson ( JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer )
        {
            string hexString = (string)reader.Value;
            return Convert.ToInt32 ( hexString, 16 );
        }

        /// <summary>
        /// Writes the JSON representation of the object by converting the integer into a hexadecimal string.
        /// </summary>
        /// <param name="writer">The JsonWriter to write to.</param>
        /// <param name="value">The object value to convert and write as JSON.</param>
        /// <param name="serializer">The JsonSerializer that is calling this method.</param>
        public override void WriteJson ( JsonWriter writer, object value, JsonSerializer serializer )
        {
            int intValue = (int)value;
            // Writes the integer as a 2-byte hexadecimal string.
            writer.WriteValue ( intValue.ToString ( "X4" ) );
        }
    }
}

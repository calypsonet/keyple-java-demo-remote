using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using App.domain.data;
using Newtonsoft.Json;

namespace App.domain.utils
{
    /// <summary>
    /// Provides a converter for JSON serialization and deserialization between hexadecimal strings and byte arrays.
    /// </summary>
    public class HexStringToByteArrayConverter : JsonConverter
    {
        /// <summary>
        /// Checks if the provided type can be converted. In this case, it checks if the type is byte[].
        /// </summary>
        /// <param name="objectType">Type of the object to check.</param>
        /// <returns>True if the type can be converted; otherwise, false.</returns>
        public override bool CanConvert(Type objectType)
        {
            return objectType == typeof(byte[]);
        }

        /// <summary>
        /// Reads a JSON object and converts it into a byte array.
        /// </summary>
        /// <param name="reader">The JsonReader to read from.</param>
        /// <param name="objectType">Type of the object to convert.</param>
        /// <param name="existingValue">The existing value of the object being read.</param>
        /// <param name="serializer">The calling JsonSerializer.</param>
        /// <returns>A byte array that represents the JSON object.</returns>
        public override object ReadJson(JsonReader reader, Type objectType, object? existingValue, JsonSerializer serializer)
        {
            if (reader.Value is string hexString)
            {
                return StringToByteArray(hexString);
            }
            else
            {
                // Handle the case where the value is not a string, or it is null.
                throw new JsonSerializationException("Invalid value type for byte array conversion.");
            }
        }

        /// <summary>
        /// Writes a byte array as a hexadecimal string into a JSON object.
        /// </summary>
        /// <param name="writer">The JsonWriter to write to.</param>
        /// <param name="value">The value to convert.</param>
        /// <param name="serializer">The calling JsonSerializer.</param>
        public override void WriteJson(JsonWriter writer, object? value, JsonSerializer serializer)
        {
            if (value is byte[] bytes)
            {
                writer.WriteValue(BitConverter.ToString(bytes).Replace("-", ""));
            }
            else
            {
                // Handle the case where the value is not a byte array or it is null.
                throw new JsonSerializationException("Invalid value type for byte array serialization.");
            }
        }

        /// <summary>
        /// Converts a hexadecimal string into a byte array.
        /// </summary>
        /// <param name="hex">The hexadecimal string to convert.</param>
        /// <returns>A byte array that represents the hexadecimal string.</returns>
        public static byte[] StringToByteArray(string hex)
        {
            return HexUtil.ToByteArray(hex);
        }
    }


    /// <summary>
    /// Provides a converter for JSON serialization and deserialization between hexadecimal strings and HashSet of integers.
    /// </summary>
    public class HexStringToSetToIntHashSetConverter : JsonConverter
    {
        /// <summary>
        /// Checks if the provided type can be converted. In this case, it checks if the type is HashSetlt;int&gt;.
        /// </summary>
        /// <param name="objectType">Type of the object to check.</param>
        /// <returns>True if the type can be converted; otherwise, false.</returns>
        public override bool CanConvert(Type objectType)
        {
            return objectType == typeof(HashSet<int>);
        }

        /// <summary>
        /// Reads a JSON object and converts it into a HashSet of integers.
        /// </summary>
        /// <param name="reader">The JsonReader to read from.</param>
        /// <param name="objectType">Type of the object to convert.</param>
        /// <param name="existingValue">The existing value of the object being read.</param>
        /// <param name="serializer">The calling JsonSerializer.</param>
        /// <returns>A HashSet of integers that represents the JSON object.</returns>
        public override object ReadJson(JsonReader reader, Type objectType, object? existingValue, JsonSerializer serializer)
        {
            List<string>? hexStrings = serializer.Deserialize<List<string>>(reader);
            if (hexStrings != null)
            {
                return new HashSet<int>(hexStrings.Select(s => Convert.ToInt32(s, 16)));
            }

            // Handle the case where hexStrings is null.
            throw new JsonSerializationException("Invalid null value for hexStrings conversion.");
        }

        /// <summary>
        /// Writes a HashSet of integers as a list of hexadecimal strings into a JSON object.
        /// </summary>
        /// <param name="writer">The JsonWriter to write to.</param>
        /// <param name="value">The value to convert.</param>
        /// <param name="serializer">The calling JsonSerializer.</param>
        public override void WriteJson(JsonWriter writer, object? value, JsonSerializer serializer)
        {
            if (value is HashSet<int> ints)
            {
                List<string> hexStrings = ints.Select(i => i.ToString("X4")).ToList();
                serializer.Serialize(writer, hexStrings);
            }
            else
            {
                // Handle the case where the value is not a HashSet<int> or it is null.
                // You can throw an exception or handle it according to your requirements.
                throw new JsonSerializationException("Invalid value type for HashSet<int> serialization.");
            }
        }
    }

    /// <summary>
    /// A converter class to handle JSON serialization and deserialization for FileOccurrence enum.
    /// </summary>
    public class FileOccurrenceConverter : JsonConverter
    {

        /// <summary>
        /// Checks if the provided type can be converted by this converter.
        /// </summary>
        /// <returns>
        /// true if the objectType is of type FileOccurrence, otherwise false.
        /// </returns>
        public override bool CanConvert(Type objectType)
        {
            return objectType == typeof(FileOccurrence);
        }

        /// <summary>
        /// Converts the JSON string to a FileOccurrence enum.
        /// </summary>
        /// <returns>
        /// A FileOccurrence enum parsed from the string value.
        /// </returns>
        public override object ReadJson(JsonReader reader, Type objectType, object? existingValue, JsonSerializer serializer)
        {
            if (reader.Value is string str)
            {
                if (Enum.TryParse(typeof(FileOccurrence), str, true, out object? enumValue))
                {
                    return enumValue;
                }
                else
                {
                    // Handle the case where the enum value is not valid.
                    throw new JsonSerializationException($"Invalid value '{str}' for FileOccurrence.");
                }
            }

            // Handle the case where the value is not a string or it is null.
            throw new JsonSerializationException("Invalid value type for FileOccurrence conversion.");
        }


        /// <summary>
        /// Converts the FileOccurrence enum to a JSON string.
        /// </summary>
        public override void WriteJson(JsonWriter writer, object? value, JsonSerializer serializer)
        {
            writer.WriteValue(value?.ToString());
        }
    }

    /// <summary>
    /// A converter class to handle JSON serialization and deserialization for FileControlInformation enum.
    /// </summary>
    public class FileControlInformationConverter : JsonConverter
    {

        /// <summary>
        /// Checks if the provided type can be converted by this converter.
        /// </summary>
        /// <returns>
        /// true if the objectType is of type FileControlInformation, otherwise false.
        /// </returns>
        public override bool CanConvert(Type objectType)
        {
            return objectType == typeof(FileControlInformation);
        }

        /// <summary>
        /// Converts the JSON string to a FileControlInformation enum.
        /// </summary>
        /// <returns>
        /// A FileControlInformation enum parsed from the string value.
        /// </returns>
        public override object ReadJson(JsonReader reader, Type objectType, object? existingValue, JsonSerializer serializer)
        {
            if (reader.Value is string str)
            {
                if (Enum.TryParse(typeof(FileControlInformation), str, true, out object? enumValue))
                {
                    return enumValue;
                }
                else
                {
                    // Handle the case where the enum value is not valid.
                    throw new JsonSerializationException($"Invalid value '{str}' for FileControlInformation.");
                }
            }

            // Handle the case where the value is not a string or it is null.
            throw new JsonSerializationException("Invalid value type for FileControlInformation conversion.");
        }


        /// <summary>
        /// Converts the FileControlInformation enum to a JSON string.
        /// </summary>
        public override void WriteJson(JsonWriter writer, object? value, JsonSerializer serializer)
        {
            writer.WriteValue(value?.ToString());
        }
    }

    /// <summary>
    /// A converter class that handles JSON serialization and deserialization for MultiSelectionProcessing enumeration.
    /// </summary>
    public class MultiSelectionProcessingConverter : JsonConverter
    {

        /// <summary>
        /// Determines whether the current converter instance can convert the specified object type.
        /// </summary>
        /// <returns>
        /// true if the objectType is of type MultiSelectionProcessing, otherwise false.
        /// </returns>
        public override bool CanConvert(Type objectType)
        {
            return objectType == typeof(MultiSelectionProcessing);
        }

        /// <summary>
        /// Reads the JSON representation of the object and converts it into a MultiSelectionProcessing enumeration.
        /// </summary>
        /// <returns>
        /// A MultiSelectionProcessing enumeration instance parsed from the provided string value.
        /// </returns>
        public override object ReadJson(JsonReader reader, Type objectType, object? existingValue, JsonSerializer serializer)
        {
            if (reader.Value is string str)
            {
                if (Enum.TryParse(typeof(MultiSelectionProcessing), str, true, out object? enumValue))
                {
                    return enumValue;
                }
                else
                {
                    // Handle the case where the enum value is not valid.
                    throw new JsonSerializationException($"Invalid value '{str}' for MultiSelectionProcessing.");
                }
            }

            // Handle the case where the value is not a string or it is null.
            throw new JsonSerializationException("Invalid value type for MultiSelectionProcessing conversion.");
        }

        /// <summary>
        /// Writes the JSON representation of the object by converting the MultiSelectionProcessing enumeration into a string.
        /// </summary>
        public override void WriteJson(JsonWriter writer, object? value, JsonSerializer serializer)
        {
            if (value != null)
            {
                writer.WriteValue(value.ToString());
            }
            else
            {
                // Handle the case where the value is null.
                throw new JsonSerializationException("Invalid null value for serialization.");
            }
        }
    }

    /// <summary>
    /// A converter class that handles JSON serialization and deserialization for ChannelControl enumeration.
    /// </summary>
    public class ChannelControlConverter : JsonConverter
    {

        /// <summary>
        /// Determines whether the current converter instance can convert the specified object type.
        /// </summary>
        /// <param name="objectType">The type of the object to be checked.</param>
        /// <returns>
        /// true if the objectType is of type ChannelControl, otherwise false.
        /// </returns>
        public override bool CanConvert(Type objectType)
        {
            return objectType == typeof(ChannelControl);
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
        public override object ReadJson(JsonReader reader, Type objectType, object? existingValue, JsonSerializer serializer)
        {
            if (reader.Value is string str)
            {
                if (Enum.TryParse(typeof(ChannelControl), str, true, out object? enumValue))
                {
                    return enumValue;
                }
                else
                {
                    // Handle the case where the enum value is not valid.
                    throw new JsonSerializationException($"Invalid value '{str}' for ChannelControl.");
                }
            }

            // Handle the case where the value is not a string or it is null.
            throw new JsonSerializationException("Invalid value type for ChannelControl conversion.");
        }

        /// <summary>
        /// Writes the JSON representation of the object by converting the ChannelControl enumeration into a string.
        /// </summary>
        /// <param name="writer">The JsonWriter to write to.</param>
        /// <param name="value">The object value to convert and write as JSON.</param>
        /// <param name="serializer">The JsonSerializer that is calling this method.</param>
        public override void WriteJson(JsonWriter writer, object? value, JsonSerializer serializer)
        {
            if (value != null)
            {
                writer.WriteValue(value.ToString());
            }
            else
            {
                // Handle the case where the value is null.
                throw new JsonSerializationException("Invalid null value for serialization.");
            }
        }
    }


    /// <summary>
    /// A converter class that handles JSON serialization and deserialization between hexadecimal strings and integers.
    /// </summary>
    public class HexStringToIntConverter : JsonConverter
    {

        /// <summary>
        /// Determines whether the current converter instance can convert the specified object type.
        /// </summary>
        /// <param name="objectType">The type of the object to be checked.</param>
        /// <returns>
        /// true if the objectType is of type int, otherwise false.
        /// </returns>
        public override bool CanConvert(Type objectType)
        {
            return objectType == typeof(int);
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
        public override object ReadJson(JsonReader reader, Type objectType, object? existingValue, JsonSerializer serializer)
        {
            if (reader.Value is string hexString)
            {
                if (hexString != null)
                {
                    return Convert.ToInt32(hexString, 16);
                }
                else
                {
                    // Handle the case where the hexString is null.
                    throw new JsonSerializationException("Invalid null value for hexString conversion.");
                }
            }

            // Handle the case where the value is not a string.
            throw new JsonSerializationException("Invalid value type for hexString conversion.");
        }


        /// <summary>
        /// Writes the JSON representation of the object by converting the integer into a hexadecimal string.
        /// </summary>
        /// <param name="writer">The JsonWriter to write to.</param>
        /// <param name="value">The object value to convert and write as JSON.</param>
        /// <param name="serializer">The JsonSerializer that is calling this method.</param>
        public override void WriteJson(JsonWriter writer, object? value, JsonSerializer serializer)
        {
            if (value != null)
            {
                int intValue = (int)value;
                // Writes the integer as a 2-byte hexadecimal string.
                writer.WriteValue(intValue.ToString("X4"));
            }
            else
            {
                // Handle the case where the value is null.
                throw new JsonSerializationException("Invalid null value for serialization.");
            }
        }
    }
}

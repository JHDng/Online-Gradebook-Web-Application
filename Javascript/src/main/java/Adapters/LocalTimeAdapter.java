package Adapters;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

	  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

	  @Override
	  public JsonElement serialize(final LocalTime time, final Type typeOfSrc,
	      final JsonSerializationContext context) {
	    return new JsonPrimitive(time.format(formatter));
	  }

	  @Override
	  public LocalTime deserialize(final JsonElement json, final Type typeOfT,
	      final JsonDeserializationContext context) throws JsonParseException {
	    return LocalTime.parse(json.getAsString(), formatter);
	  }
}
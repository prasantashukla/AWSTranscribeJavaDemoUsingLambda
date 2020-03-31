package com.serverless.voicetranscribe.function;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.transcribestreaming.TranscribeStreamingAsyncClient;
import software.amazon.awssdk.services.transcribestreaming.model.LanguageCode;
import software.amazon.awssdk.services.transcribestreaming.model.MediaEncoding;
import software.amazon.awssdk.services.transcribestreaming.model.StartStreamTranscriptionRequest;
import software.amazon.awssdk.services.transcribestreaming.model.StartStreamTranscriptionResponseHandler;
import software.amazon.awssdk.services.transcribestreaming.model.TranscriptEvent;

/**
 * Lambda function that simply prints "Hello World" if the input String is not
 * provided, otherwise, print "Hello " with the provided input String.
 */
public class HelloWorld implements RequestHandler<EventData, String> {

	public TranscribeStreamingAsyncClient client;
	public AWSIotMqttClient mqttClient;
	
	private static Map<String, Integer> numberMap = new HashMap<String, Integer>();
	static {
		numberMap.put("one", 1);
		numberMap.put("two", 2);
		numberMap.put("three", 3);
		numberMap.put("four", 4);
		numberMap.put("five", 5);
		numberMap.put("six", 6);
		numberMap.put("seven", 7);
		numberMap.put("eight", 8);
		numberMap.put("nine", 9);
	}

	@Override
	public String handleRequest(EventData input, Context context) {
		if (client == null) {
			client = TranscribeStreamingAsyncClient.create();
		}

		StartStreamTranscriptionRequest request = StartStreamTranscriptionRequest.builder()
				.mediaEncoding(MediaEncoding.PCM).languageCode(LanguageCode.EN_US).mediaSampleRateHertz(48000).build();

		List<String> finalTranscript = new ArrayList<String>();
		StartStreamTranscriptionResponseHandler response = StartStreamTranscriptionResponseHandler.builder()
				.subscriber(e -> {
					TranscriptEvent event = (TranscriptEvent) e;
					// context.getLogger().log("Got some result" + event);

					event.transcript().results().forEach(r -> r.alternatives().forEach(a -> {
						if (!r.isPartial()) {
							finalTranscript.add(a.transcript());
						}
						context.getLogger().log(a.transcript());
					}));
				}).build();

		byte[] initialArray = Base64.getDecoder().decode(input.getVoiceData());
		// byte[] initialArray = input.getRawData();
		AudioStreamPublisher publisher = new AudioStreamPublisher(new ByteArrayInputStream(initialArray));
		client.startStreamTranscription(request, publisher, response).join();

		String returnValue = "";
		if (finalTranscript.size() > 0) {
			Integer value = getValue(finalTranscript.get(0), context);

			if (value > 64) {
				returnValue = returnValue + " Out of range " + value;
				value = -1;
			}
			// Send to IOT
			try {
				publishToIOTTopic(value);
				returnValue = returnValue + " Sent to device " + value;
			} catch (AWSIotException e1) {
				returnValue = returnValue + " " + e1.getMessage();
			}
			returnValue = returnValue + " Heard - " + finalTranscript.get(0);
		} else {
			returnValue = returnValue + " No transcript found";
		}
		context.getLogger().log("Final Status - " + returnValue);
		return returnValue;

	}

	private String getCommand(String transcript) {
		Pattern pattern = Pattern.compile("S.*\\snumber\\s(\\d+|one|two|three|four|five|six|seven|eight|nine)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(transcript);
		if (matcher.find()) {
			String command = matcher.group();
			return command;
		}
		return "";
	}

	public Integer getValue(String transcript, Context c) {
		String command = getCommand(transcript);

		if (command != "") {
			Pattern pattern = Pattern.compile("(\\d+|one|two|three|four|five|six|seven|eight|nine)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(command);
			if (matcher.find()) {
				String value = matcher.group();
				Pattern wordNumber = Pattern.compile("\\d+", Pattern.CASE_INSENSITIVE);
				Matcher wordNumberMatcher = wordNumber.matcher(value);
				
				if(wordNumberMatcher.find()) {
					return parseNumber(wordNumberMatcher.group());
				} else {
					return numberMap.get(value);
				}
			}
		} else {
			c.getLogger().log("No valid command");
		}
		return -1;
	}

	private Integer parseNumber(String nStr) {
		Integer number = Integer.parseInt(nStr);
		return number;
	}

	public void publishToIOTTopic(Integer value) throws AWSIotException {
		String clientEndpoint = "a15qvpjiub5r5c-ats.iot.us-west-2.amazonaws.com";
		String clientId = UUID.randomUUID().toString();
		// AWSIotMqttClient client = new AWSIotMqttClient(clientEndpoint, clientId,
		// awsAccessKeyId, awsSecretAccessKey, sessionToken);

		if (mqttClient == null) {
			mqttClient = new AWSIotMqttClient(clientEndpoint, clientId, System.getenv("awsAccessKeyId"),
					System.getenv("awsSecretAccessKey"), null);
			// optional parameters can be set before connect()
			mqttClient.connect();
		}

		DeviceData data = new DeviceData();
		data.setValue(value.toString());
		mqttClient.publish("hello/world/counter/trigger", com.amazonaws.util.json.Jackson.toJsonString(data));
	}
}
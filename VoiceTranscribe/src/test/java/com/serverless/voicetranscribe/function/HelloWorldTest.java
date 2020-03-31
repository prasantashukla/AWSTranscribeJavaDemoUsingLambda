package com.serverless.voicetranscribe.function;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.lambda.runtime.Context;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.transcribestreaming.TranscribeStreamingAsyncClient;

public class HelloWorldTest {

	public HelloWorld handler;
	public Context ctx;

	@Before
	public void init() {
		handler = new HelloWorld();
		ctx = new TestContext();
		handler.client = TranscribeStreamingAsyncClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create("<ACCESS_KEY>", "SECRET_KEY")))
				.build();


	}

	@Test
	public void testHandler() {
		
		handler.mqttClient = new AWSIotMqttClient("<account-end-point>-ats.iot.us-west-2.amazonaws.com",
				UUID.randomUUID().toString(), "<ACCESS_KEY>", "SECRET_KEY",
				null);
		try {
			handler.mqttClient.connect();
		} catch (AWSIotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] data = readfile();
		String s = Base64.getEncoder().withoutPadding().encodeToString(data);
		EventData event = new EventData();
		event.setVoiceData(s);
		// event.setRawData(data);
		System.out.print(com.amazonaws.util.json.Jackson.toJsonString(event));
		handler.handleRequest(event, ctx);
	}

	private byte[] readfile() {

		File file = new File("C:\\Users\\prshukla\\Downloads\\show1.wav");
		FileInputStream fin = null;
		try {
			// create FileInputStream object
			fin = new FileInputStream(file);

			byte fileContent[] = new byte[(int) file.length()];
			System.out.println("File length " + (int) file.length());

			// Reads up to certain bytes of data from this input stream into an array of
			// bytes.
			fin.read(fileContent);

			return fileContent;
		} catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
			throw new RuntimeException(e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading file " + ioe);
			throw new RuntimeException(ioe);
		} finally {
			// close the streams using close method
			try {
				if (fin != null) {
					fin.close();
				}
			} catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
			}
		}
	}

	@Test
	public void testIOT() throws AWSIotException {
		handler.publishToIOTTopic(3);

	}

	@Test
	public void regExTest() {
		Assert.assertEquals((Integer)35, (Integer)handler.getValue("Show number 35", ctx));
		Assert.assertEquals((Integer)45, (Integer)handler.getValue("So number 45", ctx));
		Assert.assertEquals((Integer)1, handler.getValue("Show number one", ctx));
		Assert.assertEquals((Integer)2, handler.getValue("Show number two", ctx));
		Assert.assertEquals((Integer)3, handler.getValue("Show number three", ctx));
		Assert.assertEquals((Integer)4, handler.getValue("Show number four", ctx));
		Assert.assertEquals((Integer)5, handler.getValue("Show number five", ctx));
		Assert.assertEquals((Integer)6, handler.getValue("Show number six", ctx));
		Assert.assertEquals((Integer)7, handler.getValue("Show number seven", ctx));
		Assert.assertEquals((Integer)8, handler.getValue("Show number eight", ctx));
		Assert.assertEquals((Integer)9, handler.getValue("Show number nine", ctx));
		Assert.assertEquals((Integer)10, handler.getValue("Show number 10", ctx));

	
		Assert.assertEquals((Integer)1, handler.getValue("Sure number one", ctx));

	}
}

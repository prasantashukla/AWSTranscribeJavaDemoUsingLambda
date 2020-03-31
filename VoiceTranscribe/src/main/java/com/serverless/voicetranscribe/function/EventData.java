package com.serverless.voicetranscribe.function;

public class EventData {
	private String voiceData;
	private byte[] rawData;
	
	public byte[] getRawData() {
		return rawData;
	}

	public void setRawData(byte[] rawData) {
		this.rawData = rawData;
	}

	public String getVoiceData() {
		return voiceData;
	}

	public void setVoiceData(String voiceData) {
		this.voiceData = voiceData;
	}

}

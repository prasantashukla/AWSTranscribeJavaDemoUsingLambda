# AWSTranscribeJavaDemoUsingLambda
This is a simple demo application which is using AWS Transcribe service to convert voice to text. This demo is to parse out command "Show number &lt;number>"  and then sends the parsed out &lt;number> to AWS IOT. Then IOT forwards the payload to a Raspberry device. The Raspberry Pi is using AWS Greengrass to manage IOT and it's setup. Finally Raspberry Pi is turning on few LEDs to represent the binary representation of the <number>

The Greengrass Daemon code and Raspbery Pi code link will be updated soon.

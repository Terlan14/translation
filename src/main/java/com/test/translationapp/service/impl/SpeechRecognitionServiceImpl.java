package com.test.translationapp.service.impl;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.protobuf.ByteString;
import com.test.translationapp.service.SpeechRecognitionService;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class SpeechRecognitionServiceImpl implements SpeechRecognitionService {
    @Override
    public String transcribeAudio(byte[] audioBytes, String languageCode) throws IOException {
        try (SpeechClient speechClient = SpeechClient.create()) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setLanguageCode(languageCode)
                    //.setSampleRateHertz(48000)
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioBytes))
                    .build();

            RecognizeResponse response = speechClient.recognize(config, audio);
            StringBuilder transcription = new StringBuilder();

            for (SpeechRecognitionResult result : response.getResultsList()) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                transcription.append(alternative.getTranscript());
            }

            return transcription.toString();
        }
    }
    public String translateText(String text, String targetLang) throws IOException {
        try (TranslationServiceClient translationClient = TranslationServiceClient.create()) {
            LocationName parent = LocationName.of("amazing-modem-434508-m0", "global");

            TranslateTextRequest request = TranslateTextRequest.newBuilder()
                    .setParent(parent.toString())
                    .setMimeType("text/plain")
                    .setTargetLanguageCode(targetLang)
                    .addContents(text)
                    .build();

            TranslateTextResponse response = translationClient.translateText(request);
            return response.getTranslations(0).getTranslatedText();
        }
    }

    public void startRealtimeTranscription(String languageCode) throws Exception {
        SpeechClient speechClient = SpeechClient.create();

        ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<>() {
            @Override
            public void onStart(StreamController controller) {}

            @Override
            public void onResponse(StreamingRecognizeResponse response) {
                for (StreamingRecognitionResult result : response.getResultsList()) {
                    if (result.getAlternativesCount() > 0) {
                        String transcript = result.getAlternatives(0).getTranscript();
                        System.out.println("Transcribed (live): " + transcript);
                    }
                }
            }

            @Override
            public void onComplete() {
                System.out.println("Streaming completed.");
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }
        };

        ClientStream<StreamingRecognizeRequest> clientStream =
                speechClient.streamingRecognizeCallable().splitCall(responseObserver);

        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setLanguageCode(languageCode)
                .setSampleRateHertz(16000)
                .build();

        StreamingRecognitionConfig streamingRecognitionConfig =
                StreamingRecognitionConfig.newBuilder()
                        .setConfig(recognitionConfig)
                        .setInterimResults(true)
                        .build();

        StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingRecognitionConfig)
                .build();

        clientStream.send(request);

        AudioFormat audioFormat = new AudioFormat(48000, 16, 1, true, false);
        TargetDataLine microphone = AudioSystem.getTargetDataLine(audioFormat);
        microphone.open(audioFormat);
        microphone.start();

        byte[] buffer = new byte[4096];
        System.out.println("Mikrofon aktivdir. Danışmağa başlaya bilərsiniz.");

        while (true) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                StreamingRecognizeRequest audioRequest = StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(buffer, 0, bytesRead))
                        .build();
                clientStream.send(audioRequest);
            }
        }
    }
    public String realtimeTranscription(String languageCode, int durationInSeconds) throws Exception {
        AudioFormat format = new AudioFormat(48000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        long end = System.currentTimeMillis() + durationInSeconds * 1000;
        while (System.currentTimeMillis() < end) {
            int count = microphone.read(buffer, 0, buffer.length);
            if (count > 0) {
                out.write(buffer, 0, count);
            }
        }

        microphone.stop();
        microphone.close();
        byte[] audioBytes = out.toByteArray();

        // Transcribe
        return transcribeAudio(audioBytes, languageCode);
    }
}

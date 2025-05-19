package com.example.segiii.vozSegi.flujoVoz;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.example.segiii.R;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import java.io.InputStream;
import java.util.UUID;
import java.util.Map;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

public class DialogFlowManager {
    private SessionsClient sessionsClient;
    private SessionName session;
    private final Context context;
    private OnDialogflowResultListener dialogflowResultListener;
    private static  DialogFlowManager instance;
    private String projectId;

    public interface OnDialogflowResultListener {
        void onRegisterIntentDetected();
        void onOtherIntentDetected(String fulfillmentText);
        void onParametersReceived(Map<String, Value> parameters, String intentName);

    }

    public void setOnDialogflowResultListener(OnDialogflowResultListener listener) {
        this.dialogflowResultListener = listener;
    }
    public DialogFlowManager(Context context){
        this.context= context;
        iniciarDialogFlow();
    }
    private void iniciarDialogFlow(){
        try {
            InputStream credentialStream = context.getResources().openRawResource(R.raw.voice_segi);
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialStream);
            SessionsSettings sessionsSettings = SessionsSettings.newBuilder().setCredentialsProvider(()-> credentials).build();
            sessionsClient = sessionsClient.create(sessionsSettings);
            projectId = "segivoiceassistant";
            session = SessionName.of(projectId, UUID.randomUUID().toString());

        }catch (Exception e){
            e.printStackTrace();
            //Toast.makeText(context, "Error al inicializar Dialogflow", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendToDialogFlow (String text, TextToSpeech textToSpeech, SpeechManager speechManager){
        try {
            TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode("es-ES");
            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
            DetectIntentRequest request = DetectIntentRequest.newBuilder().setSession(session.toString()).setQueryInput(queryInput).build();
            DetectIntentResponse response = sessionsClient.detectIntent(request);
            String fulfillmentetx = response.getQueryResult().getFulfillmentText();
            String intName = response.getQueryResult().getIntent().getDisplayName();

            if (response.getQueryResult().hasParameters()){
                Struct parametres = response.getQueryResult().getParameters();
                Map<String,Value> paramsMap = parametres.getFieldsMap();
                if (dialogflowResultListener != null){
                    dialogflowResultListener.onParametersReceived(paramsMap,intName);

                }
                Value respuestaConfirmacion = paramsMap.get("respuesta_confirmacion");
                if (respuestaConfirmacion != null) {
                    String respuestaValue = respuestaConfirmacion.getStringValue();
                    if (respuestaValue.equals("no") || respuestaValue.equals("no tengo") || respuestaValue.equals("no tengo cuenta")) {
                        Log.d("DialogFlowManager", "Negative response detected, skipping speech: " + respuestaValue);
                        speechManager.stopSpeaking(); // Stop any ongoing speech
                        return; // Skip speaking the fulfillmentText
                    }
                }

            }
            Log.d("DialogFlowManager", "Intent detectado: " + intName + ", Respuesta: " + fulfillmentetx);

            if (intName.equals("Iniciaregistro") && fulfillmentetx.equals("abrir_registro")) {
                if (dialogflowResultListener != null) {
                    dialogflowResultListener.onRegisterIntentDetected();
                }
            } else {
                textToSpeech.speak(fulfillmentetx, TextToSpeech.QUEUE_FLUSH, null, "dialogflow_response");
                if (dialogflowResultListener != null) {
                    dialogflowResultListener.onOtherIntentDetected(fulfillmentetx);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            textToSpeech.speak("Lo siento, hubo un error. Intenta de nuevo.", TextToSpeech.QUEUE_FLUSH, null, "error_message");

        }
    }
    public void shutdow(){
        if(sessionsClient != null){
            sessionsClient.close();
        }
    }

}

package com.jlabarca.poc_school_assistant.controller;

import com.google.actions.api.response.ResponseBuilder;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dialogflow.v2.model.*;
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse;
import com.jlabarca.poc_school_assistant.SchoolAssistantApp;
import com.jlabarca.poc_school_assistant.alias.Student;
import com.jlabarca.poc_school_assistant.service.StudentService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@CommonsLog
public class DialogflowWebhookController {

    private static JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
    @Autowired
    private StudentService studentService;
    @Autowired
    private SchoolAssistantApp schoolAssistantApp;

    @PostMapping("/dialogflow")
    public Mono<String> webhook(@RequestBody String rawRequest) throws IOException {
        log.info(rawRequest);
        GoogleCloudDialogflowV2WebhookRequest request = jacksonFactory.createJsonParser(rawRequest)
                .parse(GoogleCloudDialogflowV2WebhookRequest.class);
        log.info(request.getOriginalDetectIntentRequest().getPayload());
        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonGenerator = jacksonFactory.createJsonGenerator(stringWriter);
        WebhookResponse response = handleIntent(request);
        jsonGenerator.serialize(response);
        jsonGenerator.flush();
        return Mono.just(stringWriter.toString());
    }

    private Student currentStudent;
    public WebhookResponse handleIntent(GoogleCloudDialogflowV2WebhookRequest request){
        GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();
        log.info(request.getQueryResult().getIntent().getName());
        switch (request.getQueryResult().getIntent().getDisplayName()) {
            case "sa.agent.student":
                currentStudent = studentService.detectStudent(request.getQueryResult().getQueryText());
                if(currentStudent != null) {
                    String answer = MessageFormat.format("Te refieres a {0} Rut {1}", currentStudent.getName(), currentStudent.getRut());
                    log.info(answer);
                    response.setFulfillmentText(answer);
                    /*GoogleCloudDialogflowV2Context next = new GoogleCloudDialogflowV2Context();
                    log.info(response.getOutputContexts());
                    next.setName("saagentstudent-confirmar");
                    next.set("field1", "1");
                    next.setLifespanCount(60);
                    List<GoogleCloudDialogflowV2Context> contexts = new ArrayList<>();
                    contexts.add(next);
                    response.setOutputContexts(contexts);*/
                }
                else
                    response.setFulfillmentText("Por favor, dame el nombre o rut del alumno");
                break;
            case "sa.agent.student - confirmar":
                response.setFulfillmentText(MessageFormat.format("Te refieres a {0} Rut {1}", currentStudent.getName(), currentStudent.getRut()));
                GoogleCloudDialogflowV2Context next = new GoogleCloudDialogflowV2Context();
                String query = request.getQueryResult().getQueryText().toLowerCase();
                switch (query){
                    case "sí":
                    case "si":
                    case "afirmativo":
                        String notas = MessageFormat.format("La últimas notas de {0} son: ", currentStudent.getName());
                        notas +=" 6,4 en Matemáticas";
                        notas +=" 4,1 en Lenguaje";
                        notas +=" 6,9 en Química";
                        response.setFulfillmentText(notas);
                        break;
                    default:
                        response.setFulfillmentText("Necesito que me confirmes con un si.");
                        break;
                }
                break;
            default:

                //return responseBuilder.build().getWebhookResponse().;
                break;
        }


        //ResponseBuilder responseBuilder = getResponseBuilder(request);
        //responseBuilder.add("Welcome to my app");

        return new WebhookResponse();
    }


    @RequestMapping(value = "/", method = RequestMethod.POST, produces = { "application/json" })
    String serveAction(@RequestBody String body, @RequestHeader Map<String, String> headers) {
        try {
            return schoolAssistantApp.handleRequest(body, headers).get();
        } catch (InterruptedException | ExecutionException e) {
            return handleError(e);
        }
    }

    private String handleError(Exception e) {
        e.printStackTrace();
        log.error("Error in App.handleRequest ", e);
        return "Error handling the intent - " + e.getMessage();
    }


}
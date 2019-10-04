package com.jlabarca.poc_school_assistant;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.jlabarca.poc_school_assistant.alias.Student;
import com.jlabarca.poc_school_assistant.service.StudentService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@CommonsLog
@Component
public class SchoolAssistantApp extends DialogflowApp {

    @Autowired
    private StudentService studentService;

    private Student currentStudent;

    @ForIntent("sa.agent.student")
    public ActionResponse student(ActionRequest request) {

        //Double number = (Double) request.getParameter("number");
        currentStudent = studentService.detectStudent(request.getWebhookRequest().getQueryResult().getQueryText());

        String answer;
        if(currentStudent != null) {
            answer = MessageFormat.format("Te refieres a {0} Rut {1}", currentStudent.getName(), currentStudent.getRut());
            log.info(answer);
        } else {
            answer = "Por favor, dame el nombre o rut del alumno";
        }

        ResponseBuilder responseBuilder = getResponseBuilder(request).add(answer).endConversation();
        ActionResponse actionResponse = responseBuilder.build();
        log.info(actionResponse.toString());
        return actionResponse;
    }

    @ForIntent("sa.agent.student - confirmar")
    public ActionResponse studentConfirmar(ActionRequest request) {
        log.info(request.getIntent());
        String answer;
        switch (request.getWebhookRequest().getQueryResult().getQueryText()){
            case "sí":
            case "si":
            case "afirmativo":
                String notas = MessageFormat.format("La últimas notas de {0} son: ", currentStudent.getName());
                notas +=" 6,4 en Matemáticas";
                notas +=" 4,1 en Lenguaje";
                notas +=" 6,9 en Química";
                answer = notas;
                break;

            case "no":
                answer = "Lo siento, no sé a que alumno te refieres";
                break;
            default:
                answer = "Necesito que me confirmes con un si o un no";
                break;
        }

        ResponseBuilder responseBuilder = getResponseBuilder(request);
        responseBuilder.add(answer);
        responseBuilder.add("Necesitas más información?");
        responseBuilder = responseBuilder.endConversation();
        ActionResponse actionResponse = responseBuilder.build();
        log.info(actionResponse.toString());
        return actionResponse;
    }

}
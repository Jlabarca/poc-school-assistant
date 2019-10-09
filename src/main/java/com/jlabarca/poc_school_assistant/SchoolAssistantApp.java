package com.jlabarca.poc_school_assistant;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.api.client.util.Lists;
import com.google.api.services.actions_fulfillment.v2.model.*;
import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessage;
import com.google.api.services.dialogflow_fulfillment.v2.model.IntentMessageQuickReplies;
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse;
import com.jlabarca.poc_school_assistant.alias.Student;
import com.jlabarca.poc_school_assistant.service.StudentService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.actions.api.Capability;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * Usuario profesor:
 * 	- ingresar notas alumno
 * 	- ingresar anotaciones
 * 	- consultar notas de alumno
 * 	- consultar asistencia
 * 	- consultar estadisticas/anotaciones
 *

 * Usuario apoderado
 * 	- consultar notas de alumno
 * 	- consultar asistencia
 * 	- consultar estadisticas/tips
 *
 */
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
            answer = MessageFormat.format("Te refieres a {0} de segundo medio A", currentStudent.getName(), currentStudent.getRut());
            log.info(answer);
        } else {
            answer = "Por favor, repite el nombre o rut del alumno";
        }

        ResponseBuilder responseBuilder = getResponseBuilder(request).add(answer);
        ActionResponse actionResponse = responseBuilder.build();
        log.info(actionResponse.toString());
        return actionResponse;
    }


    DecimalFormat df = new DecimalFormat("#,#");

    @ForIntent("sa.agent.student - confirmar")
    public ActionResponse studentConfirmar(ActionRequest request) {
        ResponseBuilder responseBuilder = getResponseBuilder(request);
        log.info(request.getIntent());
        String answer = "";
        switch (request.getWebhookRequest().getQueryResult().getQueryText()){
            case "sí":
            case "si":
            case "afirmativo":
                String notas = MessageFormat.format("La últimas notas de {0} son: ", currentStudent.getName());
                notas += df.format((Math.random() * ((3) + 1)) + 3)+","+df.format((Math.random() * ((3) + 1)) + 3)+" en Matemáticas ";
                notas += df.format((Math.random() * ((3) + 1)) + 3)+","+df.format((Math.random() * ((3) + 1)) + 3)+" en Lenguaje ";
                notas += df.format((Math.random() * ((3) + 1)) + 3)+","+df.format((Math.random() * ((3) + 1)) + 3)+" en Química";
                answer = notas;
                break;
            case "no":
                answer = "Lo siento, no sé a que alumno te refieres";
                break;
            default:
                answer = "Necesito que me confirmes con un si o un no";
                break;
        }

        return responseBuilder
                .add(answer)
                .add("Necesitas saber algo más?")
                .build();

    }

    @ForIntent("sa.agent.student - more")
    public ActionResponse studentMore(ActionRequest request) {
        ResponseBuilder responseBuilder = getResponseBuilder(request);

        return responseBuilder
                .add(currentStudent.getName()+", porcentaje de asistencia :")
                .add("Necesitas saber algo más?")
                .build();

    }

    @ForIntent("sa.agent.student - confirmar - yes")
    public ActionResponse studentConfirmarYes(ActionRequest request) {
        ResponseBuilder responseBuilder = getResponseBuilder(request);

        return responseBuilder
                .add( "Perfecto, dime que necesitas saber de "+currentStudent.getName())
                .add("Tengo información sobre notas, asistencia, anotaciones y estadisticas")
                .build();

    }

    @ForIntent("sa.agent.student - confirmar - no")
    public ActionResponse studentConfirmarNo(ActionRequest request) {
        ResponseBuilder responseBuilder = getResponseBuilder(request);

        return responseBuilder
                .add( "Vaya, dime el nombre de nuevo por favor")
                .build();

    }


    @ForIntent("sa.info.url")
        public ActionResponse infoUrl(ActionRequest request) {

        ResponseBuilder responseBuilder = getResponseBuilder(request);
        if (!request.hasCapability(Capability.SCREEN_OUTPUT.getValue())) {
            return responseBuilder
                    .add("Sorry, try ths on a screen device or select the phone surface in the simulator.")
                    .add("Which response would you like to see next?")
                    .build();
        }

        // Prepare formatted text for card
        String text =
                "This is a basic card.  Text in a basic card can include \"quotes\" and\n"
                        + "  most other unicode characters including emoji \uD83D\uDCF1. Basic cards also support\n"
                        + "  some markdown formatting like *emphasis* or _italics_, **strong** or\n"
                        + "  __bold__, and ***bold itallic*** or ___strong emphasis___ as well as other\n"
                        + "  things like line  \\nbreaks"; // Note the two spaces before '\n' required for
        // a line break to be rendered in the card.
        responseBuilder
                .add("Here's an example of a basic card.")
                .add(
                        new BasicCard()
                                .setTitle("Title: this is a title")
                                .setSubtitle("This is a subtitle")
                                .setFormattedText(text)
                                .setImage(
                                        new Image()
                                                .setUrl(
                                                        "https://storage.googleapis.com/actionsresources/logo_assistant_2x_64dp.png")
                                                .setAccessibilityText("Image alternate text"))
                                .setImageDisplayOptions("CROPPED")
                                .setButtons(
                                        new ArrayList<Button>(
                                                Arrays.asList(
                                                        new Button()
                                                                .setTitle("This is a Button")
                                                                .setOpenUrlAction(
                                                                        new OpenUrlAction().setUrl("https://assistant.google.com"))))))
                .add("Which response would you like to see next?");

        return responseBuilder.build();

    }


    @ForIntent("sa.agent.student - agregar")
    public ActionResponse studentAgregar(ActionRequest request) {
        ResponseBuilder responseBuilder = getResponseBuilder(request);
        responseBuilder.add(
                new SimpleResponse()
                        .setTextToSpeech(
                                "Here's an example of a simple response. "
                                        + "Which type of response would you like to see next?")
                        .setDisplayText(
                                "Here's a simple response. Which response would you like to see next?"));
        return responseBuilder.build();
    }

}
package slackbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;


public class SlackbotSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(SlackbotSpeechlet.class);

    private static final String SESSION_STAGE = "stage";
    private static final String SESSION_JOKE_ID = "jokeid";

    private static final int THE_MESSAGE_STAGE = 1;
    private static final int READY_FOR_DELIVERY = 2;

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        return handleCanYouSendSlackMessageIntent(session);
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        // Ready for delivery? Listen
        if (session.getAttributes().containsKey(SESSION_STAGE)) {
            if ((Integer) session.getAttribute(SESSION_STAGE) == READY_FOR_DELIVERY) {
                String message = (intent != null) ? intent.getSlot("TheMessage").getValue() : "";
                return handleSetupTheMessageIntent(session, message);
            }
        }

        if ("CanYouSendSlackMessageIntent".equals(intentName)) {
            return handleCanYouSendSlackMessageIntent(session);
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("As you wish. Bye bye");
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Whatever you want, mate. Goodbye");
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any session cleanup logic would go here
    }

    /**
     * Selects a joke randomly and starts it off by saying "Knock knock".
     *
     * @param session
     *            the session object
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse handleCanYouSendSlackMessageIntent(final Session session) {
        // Reprompt speech will be triggered if the user doesn't respond.
        String repromptText = "Can't hear you, mate. What is it again?";

        // The stage variable tracks the phase of the dialogue.
        // When this function completes, it will be on stage 1.
        session.setAttribute(SESSION_STAGE, READY_FOR_DELIVERY);
        String speechOutput = "Yeah, as you wish. What's the message to be slacked?";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Slack Bot");
        card.setContent(speechOutput);

        SpeechletResponse response = newAskResponse(speechOutput, false, repromptText, false);
        response.setCard(card);
        return response;
    }

    private SpeechletResponse handleSetupTheMessageIntent(final Session session, final String message) {
        String speechOutput = "OK. Your message is " + message + ". Slack message sent now. Happy day!";

        // TODO: Just do slack integration here! `message` variable and just send maybe to a channel

        SimpleCard card = new SimpleCard();
        card.setTitle("Slack Bot");
        card.setContent(speechOutput);

        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");
        return SpeechletResponse.newTellResponse(outputSpeech, card);
    }

    private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
            String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }
}
